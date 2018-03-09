/**
 * 
 */
package org.osivia.procedures.record.security.rules;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.osivia.procedures.record.security.rules.helper.RecordsFetcherHelper;
import org.osivia.procedures.record.security.rules.model.relation.RecordsRelation;
import org.osivia.procedures.record.security.rules.model.relation.RelationModel;
import org.osivia.procedures.record.security.rules.model.relation.RelationModelType;
import org.osivia.procedures.record.security.rules.model.type.Entity;
import org.osivia.procedures.record.security.rules.model.type.RecordModel;

/**
 * @author david
 *
 */
public class RecordsRelationFactory {

	private CoreSession session;

	private static RecordsRelationFactory instance;

	private RecordsRelationFactory(CoreSession session) {
		super();
		this.session = session;
	}

	public static synchronized RecordsRelationFactory getInstance(CoreSession session) {
		if (instance == null) {
			instance = new RecordsRelationFactory(session);
		}
		return instance;
	}

	public RecordsRelation create(Context context, RelationModel relationModel, DocumentModel model, Entity entity,
			Map<String, Entity> treatedEntities, Set<RecordsRelation> treatedRelations) {
		RecordsRelation relation = new RecordsRelation(relationModel);

		if (!treatedRelations.contains(relation)) {

			Entity srcEntity = null;
			Entity tgtEntity = null;

			switch (context) {
			case inComingRelation:
				// Yet treated?
				tgtEntity = entity;

				srcEntity = new Entity(relationModel.getSourceType());
				srcEntity.setRecordModel(new RecordModel(model));
				
				switch (relationModel.getType()) {
				case oneToOne:
					srcEntity.setRecords(tgtEntity.getRecords());
					break;
					
				case oneToN:
					srcEntity.setRecords(RecordsFetcherHelper.fetchSourceRecords(this.session, relationModel, tgtEntity));
					break;

				default:
					break;
				}

				treatedEntities.put(relationModel.getSourceType(), srcEntity);

				break;

			case outComingRelation:
				srcEntity = entity;

				tgtEntity = new Entity(relationModel.getTargetType());
				tgtEntity.setRecordModel(new RecordModel(model));
				
				switch (relationModel.getType()) {
				case oneToOne:
					tgtEntity.setRecords(srcEntity.getRecords());
					break;
					
				case oneToN:
					tgtEntity.setRecords(RecordsFetcherHelper.fetchTargetRecords(this.session, relationModel, srcEntity, tgtEntity));

				default:
					break;
				}

				treatedEntities.put(relationModel.getSourceType(), tgtEntity);

				break;

			default:
				break;
			}

			relation.setSourceEntity(srcEntity);
			relation.setTargetEntity(tgtEntity);

			// Peculiar case for targetKey
			if (RelationModelType.oneToOne.equals(relationModel.getType())) {
				// TODO!!
			}

			treatedRelations.add(relation);

		} else {
			relation = null;
		}

		return relation;
	}

	private boolean yetTreated(String sourceType, String tgtType, Map<String, String> treteadRelations) {
		return treteadRelations.containsKey(sourceType)
				&& StringUtils.equals(treteadRelations.get(sourceType), tgtType);
	}

	private Entity setRecords(Context context, RelationModel relationModel, Entity srcEntity, Entity targetEntity) {
		Entity entity = null;

		switch (context) {
		case inComingRelation:
			entity = srcEntity;
			break;

		case outComingRelation:
			entity = targetEntity;
			break;

		default:
			break;
		}

		switch (relationModel.getType()) {
		case oneToOne:
			// First target Entity (browsing relations) is a SecurityRecord which users are
			// yet calculated.
			// Store targets records in source for possible propagation - we do not fetch
			// records
			srcEntity.setRecords(targetEntity.getRecords());

			break;

		case oneToN:
			// Must fetch to get ids
			srcEntity.setRecords(
					RecordsFetcherHelper.fetchTargetRecords(this.session, relationModel, srcEntity, targetEntity));

			break;

		default:
			break;
		}

		return entity;
	}

}
