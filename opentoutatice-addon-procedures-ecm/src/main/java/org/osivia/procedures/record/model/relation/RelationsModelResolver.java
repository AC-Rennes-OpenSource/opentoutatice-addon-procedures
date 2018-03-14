/**
 * 
 */
package org.osivia.procedures.record.model.relation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.RecordsConstants;
import org.osivia.procedures.record.model.RecordAnalyzer;
import org.osivia.procedures.record.model.RecordModelAnalyzer;
import org.osivia.procedures.record.security.rules.SecurityRulesBuilder;
import org.osivia.procedures.record.security.rules.helper.RecordHelper;
import org.osivia.procedures.record.security.rules.helper.RecordModelHelper;
import org.osivia.procedures.record.security.rules.helper.RelationModelHelper;
import org.osivia.procedures.record.security.rules.model.SecurityRelations;
import org.osivia.procedures.record.security.rules.model.type.FieldType;
import org.osivia.procedures.record.security.rules.model.type.FieldsConstants;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.query.helper.ToutaticeEsQueryHelper;

/**
 * @author david
 *
 */
public class RelationsModelResolver {

	private static RelationsModelResolver instance;

	private RelationsModelResolver() {
		super();
	}

	public static synchronized RelationsModelResolver getInstance() {
		if (instance == null) {
			instance = new RelationsModelResolver();
		}
		return instance;
	}

	public boolean isSourceOfNToOneRelation(DocumentModel recordModel, String fieldDefName) {
		boolean isRecordType = false;

		if (recordModel != null) {
			ListProperty fieldsDefs = (ListProperty) recordModel
					.getProperty(ProceduresConstants.PROCEDURE_DEFINITIONS_XPATH);

			Iterator<Property> iterator = fieldsDefs.iterator();

			while (iterator.hasNext() && !isRecordType) {
				MapProperty fieldDef = (MapProperty) iterator.next();

				if (StringUtils.equals(fieldDefName, fieldDef.get("name").getValue(String.class))) {
					// Check field type
					String fieldDefType = fieldDef.get("type").getValue(String.class);
					isRecordType = StringUtils.equals(fieldDefType, FieldType.Record.getType());
				}
			}
		}

		return isRecordType;
	}

	public String getTypeOfSourcesOfNToOneRelation(DocumentModel recordModel, String fieldDefName) {
		String type = null;

		List<String> possibleTargetKeys = null;

		if (recordModel != null) {
			ListProperty fieldsDefs = (ListProperty) recordModel
					.getProperty(ProceduresConstants.PROCEDURE_DEFINITIONS_XPATH);

			Iterator<Property> iteratorD = fieldsDefs.iterator();

			boolean isFieldList = false;
			while (iteratorD.hasNext() && !isFieldList) {
				MapProperty fieldDef = (MapProperty) iteratorD.next();

				if (StringUtils.equals(fieldDefName, fieldDef.get("name").getValue(String.class))) {
					// Check field type
					String fieldDefType = fieldDef.get("type").getValue(String.class);
					isFieldList = StringUtils.equals(fieldDefType, "FIELDLIST");
				}
			}

			if (isFieldList) {
				ListProperty fieldsRefs = (ListProperty) recordModel
						.getProperty("pcd:steps/0/globalVariablesReferences");

				Iterator<Property> iteratorR = fieldsRefs.iterator();

				String fieldPath = null;
				while (iteratorR.hasNext() && fieldPath == null) {
					MapProperty fieldRef = (MapProperty) iteratorR.next();

					if (StringUtils.equals(fieldDefName, fieldRef.get("variableName").getValue(String.class))) {
						// Get path
						fieldPath = fieldRef.get("path").getValue(String.class);
					}
				}

				if (fieldPath != null) {
					Iterator<Property> iteratorRp = fieldsRefs.iterator();

					while (iteratorRp.hasNext()) {
						MapProperty fieldRef = (MapProperty) iteratorRp.next();
						String path = fieldRef.get("path").getValue(String.class);

						if (StringUtils.contains(path, fieldPath)) {
							String[] pathLevels = StringUtils.split(path, ",");
							// N-N relation:
							if (pathLevels != null && pathLevels.length > 1) {
								if (possibleTargetKeys == null) {
									possibleTargetKeys = new ArrayList<>(1);
								}
								possibleTargetKeys.add(fieldRef.get("variableName").getValue(String.class));
							}
						}
					}

					if (possibleTargetKeys != null && possibleTargetKeys.size() > 0) {
						Iterator<String> it = possibleTargetKeys.iterator();

						while (it.hasNext() && type == null) {
							String possibleTgtKey = it.next();

							Iterator<Property> itD = fieldsDefs.iterator();
							while (itD.hasNext() && type == null) {
								MapProperty fieldDef = (MapProperty) itD.next();

								String fieldName = fieldDef.get("name").getValue(String.class);
								if (StringUtils.equals(possibleTgtKey, fieldName)) {
									// Check field type
									String fieldDefType = fieldDef.get("type").getValue(String.class);
									if (StringUtils.equals(fieldDefType, FieldType.Record.getType())) {
										// Get Model type
										String typeAsFlatStr = fieldDef.get("varOptions").getValue(String.class);

										Matcher matcher = RecordModelHelper.MODEL_WEBID_PATTERN.matcher(typeAsFlatStr);
										if (matcher.find()) {
											type = matcher.group(1);
										}
									}
								}
							}
						}
					}
				}

			}
		}

		return type;
	}

	// ========================================================================================
	public static String SRC_RECORDS_QUERY = "select * from Record where rcd:type = '%s' and rcd:data.%s.ttc:webid = '%s' "
			+ RecordsConstants.DEFAULT_FILTER;

	public DocumentModelList getSourcesOfRelationWithTarget(DocumentModel tgtRecord) {
		// Result
		DocumentModelList sources = null;

		// Get Record model
		DocumentModel tgtModel = RecordAnalyzer.getRecordModelOf(tgtRecord);
		if (tgtModel != null) {
			// Get sources Record models pointing to tgtModel
			// Need existing models:
			DocumentModelList srcModels = ToutaticeEsQueryHelper.unrestrictedQuery(tgtRecord.getCoreSession(),
					RecordModelHelper.RECORD_MODELS_QUERY, -1);

			// Target type
			String targetType = RecordModelHelper.getType(tgtModel);

			if (srcModels != null && srcModels.size() > 0) {
				// Check all recordModels to see which ones are pointing to model associated
				// with given Entity
				for (DocumentModel srcModel : srcModels) {
					ListProperty fieldsDefinitions = RecordModelHelper.getFieldsDefinitions(srcModel);

					for (Property fieldDef : fieldsDefinitions) {
						MapProperty field = (MapProperty) fieldDef;

						if (StringUtils.equals(FieldType.Record.getType(),
								(String) field.get(FieldsConstants.TYPE).getValue())) {
							String srcPointingType = RecordModelHelper.getModelType(field);

							if (StringUtils.equals(srcPointingType, targetType)) {
								// A type is pointing to tgtRecordType
								String targetKey = getTargetKey(srcModel, field);
								// Target Record webId
								String tgtWebId = (String) ToutaticeDocumentHelper.getUnrestrictedProperty(
										tgtRecord.getCoreSession(), tgtRecord.getId(),
										ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);

								String query = String.format(SRC_RECORDS_QUERY, RecordModelHelper.getType(srcModel), targetKey, tgtWebId);
								DocumentModelList sourcesRecords = ToutaticeEsQueryHelper
										.unrestrictedQuery(tgtRecord.getCoreSession(), query, -1);

								if (CollectionUtils.isNotEmpty(sourcesRecords)) {
									if (sources == null) {
										sources = new DocumentModelListImpl(sourcesRecords.size());
									}
									for (DocumentModel source : sourcesRecords) {
										if (!sources.contains(source)) {
											sources.add(source);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return sources;
	}
	
	public static String getTargetKey(DocumentModel srcModel, MapProperty recordField) {
		String targetKey = recordField.get(FieldsConstants.NAME).getValue(String.class);
		
		// Check if multivalued
		ListProperty references = (ListProperty) srcModel.getProperty(FieldsConstants.REFERENCES);
		String nToNBaseLevel = RelationModelHelper.getOneToNRelationIndicator(references, targetKey);

		if (StringUtils.isNotBlank(nToNBaseLevel)) {
			Iterator<Property> iterator = references.iterator();
			boolean parentFound = false;

			while (iterator.hasNext() && !parentFound) {
				MapProperty ref = (MapProperty) iterator.next();
				String path = ref.get(FieldsConstants.PATH).getValue(String.class);

				if (StringUtils.equals(nToNBaseLevel, path)) {
					parentFound = true;
					targetKey = ref.get(FieldsConstants.VARIABLE_NAME).getValue(String.class);
				}
			}
		}
		
		
		return targetKey;
	}
	
	public static String getOneToNRelationKey(DocumentModel srcModel, String targetKey) {
		String relationKey = StringUtils.EMPTY;

		ListProperty references = (ListProperty) srcModel.getProperty(FieldsConstants.REFERENCES);

		String nToNBaseLevel = RelationModelHelper.getOneToNRelationIndicator(references, targetKey);

		if (StringUtils.isNotBlank(nToNBaseLevel)) {
			Iterator<Property> iterator = references.iterator();
			boolean parentFound = false;

			while (iterator.hasNext() && !parentFound) {
				MapProperty ref = (MapProperty) iterator.next();
				String path = ref.get(FieldsConstants.PATH).getValue(String.class);

				if (StringUtils.equals(nToNBaseLevel, path)) {
					parentFound = true;
					relationKey = ref.get(FieldsConstants.VARIABLE_NAME).getValue(String.class);
				}
			}
		}

		return relationKey;
	}

}
