/**
 * 
 */
package org.osivia.procedures.record.security.rules;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.osivia.procedures.record.security.rules.helper.RecordModelHelper;
import org.osivia.procedures.record.security.rules.helper.RelationModelHelper;
import org.osivia.procedures.record.security.rules.model.SecurityRelations;
import org.osivia.procedures.record.security.rules.model.relation.RecordsRelation;
import org.osivia.procedures.record.security.rules.model.relation.RelationModel;
import org.osivia.procedures.record.security.rules.model.relation.RelationModelType;
import org.osivia.procedures.record.security.rules.model.type.Entity;
import org.osivia.procedures.record.security.rules.model.type.FieldType;
import org.osivia.procedures.record.security.rules.model.type.FieldsConstants;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

/**
 * @author david
 *
 */
public class RecordsRelationsResolver {

	public static final String LINKED_ONE_TO_N_ENTITIES_QUERY = "select * from Record where rcd:type = '%s' and ttc:webid in %s ";
	public static final String LINKED_ONE_TO_ONE_ENTITY_QUERY = "select * from Record where rcd:type = '%s' and rcd:data.%s = '%s' ";

	private static RecordsRelationsResolver instance;

	private RecordsRelationsResolver() {
		super();
	}

	public static synchronized RecordsRelationsResolver getInstance() {
		if (instance == null) {
			instance = new RecordsRelationsResolver();
		}
		return instance;
	}

	/**
	 * Gets RecordModels pointing TO Entity's RecordModel and builds corresponding
	 * SecurityRelations.
	 * 
	 * @param session
	 * @param targetEntity
	 * @return SecurityRelations
	 */
	public SecurityRelations getInComingRelations(CoreSession session, DocumentModelList srcModels,
			Entity targetEntity, Map<String, Entity> treatedEntities, Set<RecordsRelation> treteadRelations) {

		SecurityRelations inComingRelations = new SecurityRelations();
		// Target type
		String targetType = targetEntity.getType();

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
							// A type is pointing to currentType
							String targetKey = RelationModelHelper.getTargetKey(field);

							// Relation Model
							RelationModelType relationType = RelationModelHelper.isOneToOneRelation(srcModel, targetKey)
									? RelationModelType.oneToOne
									: RelationModelType.oneToN;
							RelationModel relationModel = RelationModelHelper.buildModel(relationType, RecordModelHelper.getType(srcModel),
									RelationModelHelper.getTargetKey(relationType, srcModel, targetKey), targetType);

							// Relation
							RecordsRelation relation = RecordsRelationFactory.getInstance(session)
									.create(Context.inComingRelation, relationModel, srcModel, targetEntity, treatedEntities, treteadRelations);
							if(relation != null) {
								inComingRelations.add(SecurityRelationsAdapter.getInstance().adapt(session, Context.inComingRelation, relation));
							}

						}
					}
				}
			}
		}

		return inComingRelations;
	}

	/**
	 * Gets RecordModels pointing FROM Entity's RecordModel and builds corresponding
	 * SecurityRelations.
	 * 
	 * @param session
	 * @param srcEntity
	 * @return
	 */
	public SecurityRelations getOutComingRelations(CoreSession session, DocumentModelList targetModels,
			Entity srcEntity, Map<String, Entity> treatedEntities, Set<RecordsRelation> treteadRelations) {
		SecurityRelations outComingRelations = new SecurityRelations();

		DocumentModel srcModel = RecordModelHelper.getModelOf(targetModels, srcEntity);

		if (srcModel != null) {

			ListProperty fieldsDefs = RecordModelHelper.getFieldsDefinitions(srcModel);

			for (Property fieldDef : fieldsDefs) {
				MapProperty field = (MapProperty) fieldDef;

				if (StringUtils.equals(FieldType.Record.getType(),
						(String) field.get(FieldsConstants.TYPE).getValue())) {
					String srcPointingType = RecordModelHelper.getModelType(field);

					for (DocumentModel targetModel : targetModels) {
						String targetType = targetModel
								.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID)
								.getValue(String.class);

						if (StringUtils.equals(srcPointingType, targetType)) {
							// Type is pointing to another
							String targetKey = RelationModelHelper.getTargetKey(field);

							// Relation Model
							RelationModelType relationType = RelationModelHelper.isOneToOneRelation(srcModel, targetKey)
									? RelationModelType.oneToOne
									: RelationModelType.oneToN;
							RelationModel relationModel = RelationModelHelper.buildModel(relationType, RecordModelHelper.getType(srcModel),
									RelationModelHelper.getTargetKey(relationType, srcModel, targetKey), targetType);

							// Relation
							RecordsRelation relation = RecordsRelationFactory.getInstance(session)
									.create(Context.outComingRelation, relationModel, targetModel, srcEntity, treatedEntities, treteadRelations);
							if(relation != null) {
								outComingRelations.add(SecurityRelationsAdapter.getInstance().adapt(session, Context.outComingRelation, relation));
							}
							
						}

					}
				}
			}

		}

		return outComingRelations;
	}

//	private Entity setNtoOneLinkedEntityRecords(CoreSession session, Entity currentEntity, DocumentModel currentModel,
//			String linkedType, String fieldName, Entity linkedEntity) {
//		List<String> linkedIds = getLinkedIds(session, currentModel, currentEntity, fieldName);
//
//		String query = String.format(LINKED_ONE_TO_ONE_ENTITY_QUERY, linkedType, getInOperand(linkedIds));
//		DocumentModelList linkedRecords = ToutaticeEsQueryHelper.query(session, query, -1);
//
//		// Complete linked Entity build
//		linkedEntity.setRecords(linkedRecords);
//		linkedEntity.setIds(linkedIds);
//
//		// Mark entity
//		linkedEntity.setFetched(true);
//
//		return linkedEntity;
//	}
//
//	private Entity setNtoNLinkedEntityRecords(CoreSession session, Entity currentEntity, DocumentModel currentModel,
//			String linkedType, String fieldName, Entity linkedEntity) {
//		List<String> linkedIds = getLinkedIds(session, currentModel, currentEntity, fieldName);
//
//		String query = String.format(LINKED_ONE_TO_N_ENTITIES_QUERY, linkedType, getInOperand(linkedIds));
//		DocumentModelList linkedRecords = ToutaticeEsQueryHelper.query(session, query, -1);
//
//		// Complete linked Entity build
//		linkedEntity.setRecords(linkedRecords);
//		linkedEntity.setIds(linkedIds);
//
//		// Mark entity
//		linkedEntity.setFetched(true);
//
//		return linkedEntity;
//	}


}
