/**
 * 
 */
package org.osivia.procedures.record.es.customizer.writer.denormalization;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.BaseJsonNode;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.schema.utils.DateParser;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.es.customizer.writer.helper.DenormalizationJsonESWriterHelper;
import org.osivia.procedures.record.RecordsConstants;
import org.osivia.procedures.record.model.FieldTypes;
import org.osivia.procedures.record.model.RecordModelAnalyzer;
import org.osivia.procedures.record.model.relation.RelationsModelResolver;
import org.osivia.procedures.record.security.rules.RecordsRelationsResolver;
import org.osivia.procedures.record.security.rules.helper.RecordHelper;
import org.osivia.procedures.record.security.rules.helper.RecordsFetcherHelper;

import fr.toutatice.ecm.es.customizer.writers.denormalization.AbstractDenormalizationJsonESWriter;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

/**
 * @author david
 *
 */
public class RecordJsonESDenormalizationWriter extends AbstractDenormalizationJsonESWriter {

	private static final String GET_RECORD_TARGET_N_TO_ONE_RELATION_QUERY = "select * from Record where ttc:webid ='%s' "
			+ RecordsConstants.DEFAULT_FILTER;

	private static final String GET_LINKED_N_TO_N_ENTITIES_QUERY = "select * from Record where rcd:procedureModelWebId = '%s' and ttc:webid in %s ";

	@Override
	public boolean accept(DocumentModel doc) {
		return doc != null && ProceduresConstants.RECORD_TYPE.equals(doc.getType());
	}

	@Override
	protected void denormalizeDoc(JsonGenerator jg, DocumentModel record, String[] schemas,
			Map<String, String> contextParameters) throws IOException {
		// Set model type on Record
		DocumentModel recordModel = super.session.getParentDocument(record.getRef());
		if (recordModel != null) {
			jg.writeStringField("rcd:type",
					(String) recordModel.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID));
		}

		// Custom name /value
		// FIXME: to remove later
		DenormalizationJsonESWriterHelper.mapKeyValue(jg, record, ProceduresConstants.RCD_VALUES_XPATH,
				ProceduresConstants.PI_ENTRY_KEY, ProceduresConstants.ENTRY_VALUE);

		// Custom name / value as Json
		denormalize(jg, recordModel, record, ProceduresConstants.RCD_VALUES_XPATH, ProceduresConstants.PI_ENTRY_KEY,
				ProceduresConstants.ENTRY_VALUE, schemas, contextParameters);

	}

	public JsonGenerator denormalize(JsonGenerator jg, DocumentModel recordModel, DocumentModel record,
			String listPropXPath, String entryKey, String entryValue, String[] schemas,
			Map<String, String> contextParameters) throws JsonGenerationException, IOException {

		ListProperty valuesProp = (ListProperty) record.getProperty(listPropXPath);

		if (CollectionUtils.isNotEmpty(valuesProp)) {

			jg.writeFieldName(RecordsConstants.CUSTOM_RECORD_XPATH);
			jg.writeStartObject();

			for (Property valueProp : valuesProp) {
				String name = (String) valueProp.get(entryKey).getValue();
				String value = (String) valueProp.get(entryValue).getValue();

				if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {

					Matcher matcher = DenormalizationJsonESWriterHelper.COMPLEX_PROPERTY_PATTERN.matcher(value);
					if (matcher.matches()) {
						// We denormalize only first level target of N-N relations
						// Check if record is source of N-N relation
						String type = RelationsModelResolver.getInstance().getTypeOfSourcesOfNToOneRelation(recordModel,
								name);
						if (StringUtils.isNotBlank(type)) {
							DocumentModelList targets = getTargetsOfNToNRelation(type, value);
							// targets == null in case of PERSON relation
							if (targets != null) {
								// Write
								jg.writeFieldName(name);
								jg.writeStartArray();

								for (DocumentModel target : targets) {
									// Keep direct id link relation
									this.jsonESWriter.writeNativeESDocument(jg, target, target.getSchemas(), null);
								}

								jg.writeEndArray();
							} else {
								jg.writeStringField(name, value);
							}
						} else {
							String fieldType = RecordModelAnalyzer.getInstance().getFieldType(recordModel, name);
							if (isAttachedBinary(fieldType)) {
								value = org.apache.commons.lang.StringUtils.substringBeforeLast(value, "}")
										.concat(",\"type\":\"" + fieldType + "\"}\"");

								ObjectMapper mapper = new ObjectMapper();
								BaseJsonNode jsonNode = mapper.readValue(value, BaseJsonNode.class);

								jg.writeFieldName(name);
								jsonNode.serialize(jg, null);

							} else {
								ObjectMapper mapper = new ObjectMapper();
								BaseJsonNode jsonNode = mapper.readValue(value, BaseJsonNode.class);

								jg.writeFieldName(name);
								jsonNode.serialize(jg, null);
							}
						}
					} else {
						// We denormalize only first level target of N-1 relations
						if (RelationsModelResolver.getInstance().isSourceOfNToOneRelation(recordModel, name)) {
							// Get target: value is webId
							DocumentModel targetRecord = getTargetOfNToOnefRelation(value);
							// target == null in case of PERSON relation
							if (targetRecord != null) {
								// Write
								jg.writeFieldName(name);
								this.jsonESWriter.writeNativeESDocument(jg, targetRecord, targetRecord.getSchemas(),
										null);
							} else {
								jg.writeStringField(name, value);
							}
						} else if (isDate(value)) {
							jg.writeFieldName(name);
							String initialValue = value;
							try {
								value = DateParser
										.formatW3CDateTime(DenormalizationJsonESWriterHelper.format.parse(value));
							} catch (ParseException e) {
								value = initialValue;
							}
							jg.writeString(value);
						} else {
							// Scalar property
							jg.writeStringField(name, value);
						}
					}
				}
			}

			jg.writeEndObject();

		}

		return jg;
	}

	private boolean isDate(String value) {
		Matcher dateMatcher = DenormalizationJsonESWriterHelper.DATE_PATTERN.matcher(value);
		return dateMatcher.matches();
	}

	private boolean isAttachedBinary(String fieldType) {
		return ArrayUtils.contains(FieldTypes.types(),fieldType);
	}

	private DocumentModelList getTargetsOfNToNRelation(String type, String idsAsString) {
		// Result
		DocumentModelList targets = null;

		// Get ids as list
		List<String> ids = new ArrayList<>(0);
		ids = RecordHelper.getIds(ids, idsAsString, RecordsRelationsResolver.RECORDS_WEBIDS_PATTERN);

		if (CollectionUtils.isNotEmpty(ids)) {
			String query = String.format(GET_LINKED_N_TO_N_ENTITIES_QUERY, type,
					RecordsFetcherHelper.getInOperand(ids));
			targets = super.session.query(query, -1);
		}

		return targets;
	}

	private DocumentModel getTargetOfNToOnefRelation(String id) {
		DocumentModel target = null;

		DocumentModelList targets = super.session.query(String.format(GET_RECORD_TARGET_N_TO_ONE_RELATION_QUERY, id),
				1);
		if (targets.size() == 1) {
			target = targets.get(0);
		}

		return target;
	}

}
