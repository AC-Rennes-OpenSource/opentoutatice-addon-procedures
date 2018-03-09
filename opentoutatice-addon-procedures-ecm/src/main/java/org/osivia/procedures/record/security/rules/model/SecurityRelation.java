/**
 * 
 */
package org.osivia.procedures.record.security.rules.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.osivia.procedures.record.security.rules.model.type.Entity;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * Security (meaning functionally readable) object storing relevant information
 * for secure queries and policy.
 * 
 * @author david
 *
 */
public class SecurityRelation {

	private SecurityRelationType type;

	private String relationKey;
	private Entity entity;

	private String recordType;
	private List<String> recordsIds;

	public SecurityRelation(SecurityRelationType type) {
		super();
		this.type = type;
	}

	public SecurityRelation() {
		super();
	}

	public SecurityRelationType getType() {
		return type;
	}

	public void setType(SecurityRelationType type) {
		this.type = type;
	}

	public String getRelationKey() {
		return relationKey;
	}

	public void setRelationKey(String relationKey) {
		this.relationKey = relationKey;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public List<String> getRecordsIds() {
		return recordsIds;
	}

	public void setRecordsIds(List<String> recordsIds) {
		this.recordsIds = recordsIds;
	}

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(CoreSession session, Entity entity) {
		this.entity = entity;

		if (this.recordType == null) {
			this.recordType = entity.getType();
		}

		if (this.recordsIds == null) {
			List<DocumentModel> records = entity.getRecords();

			if (CollectionUtils.isNotEmpty(records)) {
				this.recordsIds = new ArrayList<>(records.size());
			}

			for (DocumentModel record : records) {
				String id = (String) ToutaticeDocumentHelper.getUnrestrictedProperty(session, record.getId(),
						ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
				this.recordsIds.add(id);
			}
		}
	}

	@Override
	public boolean equals(Object other) {
		boolean equals = false;

		if (other != null && other instanceof SecurityRelation) {
			SecurityRelation otherSr = (SecurityRelation) other;
			return StringUtils.equals(this.recordType, otherSr.getRecordType());
		}

		return equals;
	}

}
