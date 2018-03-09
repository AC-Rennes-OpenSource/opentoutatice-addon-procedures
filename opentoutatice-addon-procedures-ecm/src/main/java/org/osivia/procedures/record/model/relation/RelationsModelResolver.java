/**
 * 
 */
package org.osivia.procedures.record.model.relation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.security.rules.SecurityRulesBuilder;
import org.osivia.procedures.record.security.rules.helper.RecordHelper;
import org.osivia.procedures.record.security.rules.helper.RecordModelHelper;
import org.osivia.procedures.record.security.rules.model.SecurityRelations;
import org.osivia.procedures.record.security.rules.model.type.FieldType;

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
	
	// TODO !!!
	public boolean isTargetOfRelation(DocumentModel recordModel) {
//		SecurityRelations rules = SecurityRulesBuilder.getInstance().build(record, principal);
//		
		return false;
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
										if(matcher.find()) {
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

}
