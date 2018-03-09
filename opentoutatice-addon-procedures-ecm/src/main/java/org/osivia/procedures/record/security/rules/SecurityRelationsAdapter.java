/**
 * 
 */
package org.osivia.procedures.record.security.rules;

import org.nuxeo.ecm.core.api.CoreSession;
import org.osivia.procedures.record.security.rules.model.SecurityRelation;
import org.osivia.procedures.record.security.rules.model.SecurityRelationType;
import org.osivia.procedures.record.security.rules.model.relation.RecordsRelation;
import org.osivia.procedures.record.security.rules.model.relation.RelationModelType;
import org.osivia.procedures.record.security.rules.model.type.Entity;

/**
 * @author david
 *
 */
public class SecurityRelationsAdapter {

	private static SecurityRelationsAdapter instance;

	private SecurityRelationsAdapter() {
		super();
	}

	public static synchronized SecurityRelationsAdapter getInstance() {
		if (instance == null) {
			instance = new SecurityRelationsAdapter();
		}
		return instance;
	}

	public SecurityRelation adapt(CoreSession session, Context context, RecordsRelation recordsRelation) {
		SecurityRelation secureRelation = null;

		if (recordsRelation != null) {
			secureRelation = new SecurityRelation();
			secureRelation.setType(adapType(recordsRelation.getType()));
			secureRelation.setRelationKey(recordsRelation.getTargetKey());
			secureRelation.setEntity(session, adaptEntity(context, recordsRelation));
		}

		return secureRelation;
	}

	private SecurityRelationType adapType(RelationModelType recordsRelationType) {
		SecurityRelationType navType = null;

		switch (recordsRelationType) {
		case oneToOne:
			navType = SecurityRelationType.NtoOne;
			break;

		case oneToN:
			navType = SecurityRelationType.NtoN;
			break;

		default:
			break;
		}

		return navType;
	}

	private Entity adaptEntity(Context context, RecordsRelation recordsRelation) {
		// Readable Entity
		Entity secureEntity = null;

		switch (context) {
		case inComingRelation:
			secureEntity = recordsRelation.getSourceEntity();
			break;
		case outComingRelation:
			secureEntity = recordsRelation.getTargetEntity();
		default:
			break;
		}

		return secureEntity;
	}

}
