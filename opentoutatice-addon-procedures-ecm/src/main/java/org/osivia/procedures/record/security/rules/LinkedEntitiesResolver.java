/**
 * 
 */
package org.osivia.procedures.record.security.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.security.rules.helper.RecordHelper;
import org.osivia.procedures.record.security.rules.helper.RecordModelHelper;
import org.osivia.procedures.record.security.rules.helper.RelationHelper;
import org.osivia.procedures.record.security.rules.model.SecurityRules;
import org.osivia.procedures.record.security.rules.model.relation.Relation;
import org.osivia.procedures.record.security.rules.model.relation.RelationModel;
import org.osivia.procedures.record.security.rules.model.type.Entity;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.query.helper.ToutaticeEsQueryHelper;

/**
 * @author david
 *
 */
public class LinkedEntitiesResolver {

	private static final Log log = LogFactory.getLog(LinkedEntitiesResolver.class);

	public static final String LINKED_N_TO_N_ENTITIES_QUERY = "select * from Record where rcd:type = '%s' and ttc:webid in %s ";
	public static final String LINKED_N_TO_ONE_ENTITY_QUERY = "select * from Record where rcd:type = '%s' and rcd:data.%s = '%s' ";

	protected static final String ONE_TO_ONE_LINK_VALUE_QUERY = "";

	public static final Pattern RECORDS_WEBIDS_PATTERN = Pattern.compile("\\\"[a-zA-Z0-9]+\\\":\\\"([0-9a-zA-Z]{6}){1}\\\"");

	private static LinkedEntitiesResolver instance;

	private LinkedEntitiesResolver() {
		super();
	}

	public static synchronized LinkedEntitiesResolver getInstance() {
		if (instance == null) {
			instance = new LinkedEntitiesResolver();
		}
		return instance;
	}

	/**
	 * Here,
	 * 
	 * @param session
	 * @param entity
	 * @return
	 */
	public Map<String, Entity> getLinkedEntitiesTo(CoreSession session, SecurityRules rules,
			DocumentModelList models, Entity entity) {
		Map<String, Entity> linkedEntities = new HashMap<>(0);

		if (models != null && models.size() > 0) {

			for (DocumentModel model : models) {
				ListProperty fieldsDefs = (ListProperty) model
						.getProperty(ProceduresConstants.PROCEDURE_DEFINITIONS_XPATH);

				for (Property fieldDef : fieldsDefs) {
					MapProperty field = (MapProperty) fieldDef;

					if (StringUtils.equals("RECORD", (String) field.get("type").getValue())) {
						String linkedType = RecordModelHelper.getModelType(field);
						String fieldName = field.get("name").getValue(String.class);

						if (StringUtils.equals(entity.getType(), linkedType)) {
							// Linked: store Entity
							String modelType = model
									.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID)
									.getValue(String.class);
							Entity linkedEntity = new Entity(modelType);
							
							if (RelationHelper.isNToOneRelation(model, fieldName)) {
								// Relation Model infos
								RelationModel relationModel = new RelationModel(RelationModel.Type.NtoOne);
								
								relationModel.setSourceType(modelType);
								relationModel.setTargetKey(fieldName);
								relationModel.setTargetType(entity.getType());
								
								// Relation
								Relation relation = new Relation(relationModel);
								if(!entity.isFetched()) {
									linkedEntity = setNtoOneLinkedEntityRecords(session, entity, model, linkedType, fieldName, linkedEntity);
								}
								relation.setTargetIds(entity.getIds());

								linkedEntity.setRelationTo(relation);

								// Store rules
								rules.addModelIfNotPresent(modelType);
								rules.addRelationIfNotPresent(relation);

							} else if (RelationHelper.isNToNRelation(model, fieldName)) {
								// linkedEntity.setRelationType(RelationModel.Type.oneToN);
								// TODO
							} 

							// Store
							linkedEntities.put(modelType, linkedEntity);
						}
					}
				}
			}
		}

		return linkedEntities;
	}

	/**
	 * 
	 * 
	 * @param session
	 * @param currentEntity
	 * @return
	 */
	public Map<String, Entity> getLinkedEntitiesFrom(CoreSession session, SecurityRules rules,
			DocumentModelList models, Entity currentEntity) {
		Map<String, Entity> linkedEntities = new HashMap<>(0);

		// Entity relations
		DocumentModel currentModel = RecordModelHelper.getModelOf(models, currentEntity);

		if (currentModel != null) {

			ListProperty fieldsDefs = (ListProperty) currentModel
					.getProperty(ProceduresConstants.PROCEDURE_DEFINITIONS_XPATH);

			for (Property fieldDef : fieldsDefs) {
				MapProperty field = (MapProperty) fieldDef;

				if (StringUtils.equals("RECORD", (String) field.get("type").getValue())) {
					String linkedType = RecordModelHelper.getModelType(field);
					String fieldName = field.get("name").getValue(String.class);

					for (DocumentModel model : models) {
						String modelType = model.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID)
								.getValue(String.class);

						if (StringUtils.equals(linkedType, modelType)) {
							// Linked: store Entity
							Entity linkedEntity = new Entity(modelType);

							if (RelationHelper.isNToNRelation(currentModel, fieldName)) {
								// Relation Model infos
								RelationModel relationModel = new RelationModel(RelationModel.Type.NtoN);
								
								relationModel.setSourceType(modelType);
								relationModel.setTargetKey(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
								relationModel.setTargetType(currentEntity.getType());
								
								// // Get linked Records 
								linkedEntity = setNtoNLinkedEntityRecords(session, currentEntity, currentModel,
										linkedType, fieldName, linkedEntity);
								
								// Relation
								Relation relation = new Relation(relationModel);
								relation.setTargetIds(linkedEntity.getIds());
								
								linkedEntity.setRelationFrom(relation);

								// Store rules
								rules.addModelIfNotPresent(modelType);
								rules.addRelationIfNotPresent(relation);

							} else if (RelationHelper.isNToOneRelation(currentModel, fieldName)) {
								// linkedEntity.setRelationType(RelationModel.Type.oneToOne);
								// TODO
							}

							// Store
							linkedEntities.put(linkedType, linkedEntity);

						}

					}
				}
			}

		}

		return linkedEntities;
	}
	
	private Entity setNtoOneLinkedEntityRecords(CoreSession session, Entity currentEntity, DocumentModel currentModel,
			String linkedType, String fieldName, Entity linkedEntity) {
		List<String> linkedIds = getLinkedIds(session, currentModel, currentEntity, fieldName);
		
		String query = String.format(LINKED_N_TO_ONE_ENTITY_QUERY, linkedType, getInOperand(linkedIds));
		DocumentModelList linkedRecords = ToutaticeEsQueryHelper.query(session, query, -1);

		// Complete linked Entity build
		linkedEntity.setRecords(linkedRecords);
		linkedEntity.setIds(linkedIds);
		
		// Mark entity
		linkedEntity.setFetched(true);
		
		return linkedEntity;
	}

	private Entity setNtoNLinkedEntityRecords(CoreSession session, Entity currentEntity, DocumentModel currentModel,
			String linkedType, String fieldName, Entity linkedEntity) {
		List<String> linkedIds = getLinkedIds(session, currentModel, currentEntity, fieldName);
		
		String query = String.format(LINKED_N_TO_N_ENTITIES_QUERY, linkedType, getInOperand(linkedIds));
		DocumentModelList linkedRecords = ToutaticeEsQueryHelper.query(session, query, -1);

		// Complete linked Entity build
		linkedEntity.setRecords(linkedRecords);
		linkedEntity.setIds(linkedIds);
		
		// Mark entity
		linkedEntity.setFetched(true);
		
		return linkedEntity;
	}

	public String prepareInOperand(CoreSession session, DocumentModel model, Entity entity, String fieldName) {
		return getInOperand(getLinkedIds(session, model, entity, fieldName));
	}

	protected List<String> getLinkedIds(CoreSession session, DocumentModel model, Entity entity, String fieldName) {
		// Result
		List<String> webIds = null;

		// Get linked webIds:
		// Get webIds property (in model)
		String linkedProp = RelationHelper.getNToNRelationKey(model, fieldName);
		if (StringUtils.isNotBlank(linkedProp)) {
			// Flat value stored as global String (not denormalized)
			String webIdsAsString = null;
			webIds = new ArrayList<>(0);

			for (DocumentModel record : entity.getRecords()) {
				ListProperty fieldsValues = (ListProperty) record.getProperty(ProceduresConstants.RCD_VALUES_XPATH);
				for (Property fieldValue : fieldsValues) {
					MapProperty fV = (MapProperty) fieldValue;

					if (StringUtils.equals(linkedProp, fV.get("name").getValue(String.class))) {
						webIdsAsString = fV.get("value").getValue(String.class);
					}
				}

				if (StringUtils.isNotBlank(webIdsAsString)) {
					// Extract webIds
					webIds = RecordHelper.getIds(webIds, webIdsAsString, RECORDS_WEBIDS_PATTERN);

				}
			}
		}
		return webIds;
	}

	public String getInOperand(List<String> elements) {
		StringBuffer operand = new StringBuffer();
		
		if(elements != null) {
			operand.append("('");
			operand.append(StringUtils.join(elements, "','"));
			operand.append("')");
		}

		return operand.toString();
	}

}
