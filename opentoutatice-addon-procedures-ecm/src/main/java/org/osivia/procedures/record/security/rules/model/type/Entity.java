/**
 * 
 */
package org.osivia.procedures.record.security.rules.model.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.RecordsConstants;
import org.osivia.procedures.record.security.rules.RecordsRelationsResolver;
import org.osivia.procedures.record.security.rules.model.relation.RecordsRelation;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

/**
 * @author david
 *
 */
public class Entity {

	private String type;
	private boolean isSecurity;
	
	private RecordModel recordModel;

	private RecordsRelation relationTo;
	private RecordsRelation relationFrom;

	private boolean fetched;

	private List<String> ids;
	private List<DocumentModel> records;
	
	public Entity(RecordModel recordModel) {
		super();
		this.recordModel = recordModel;
	}

	public Entity(String type) {
		super();
		this.type = type;
		this.records = new ArrayList<>();
	}

	public RecordModel getRecordModel() {
		return recordModel;
	}

	public void setRecordModel(RecordModel recordModel) {
		this.recordModel = recordModel;
	}

	public void addRecord(DocumentModel record) {
		this.records.add(record);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isSecurity() {
		return isSecurity;
	}

	public void setSecurity(boolean isSecurity) {
		this.isSecurity = isSecurity;
	}

	public RecordsRelation getRelationTo() {
		return relationTo;
	}

	public void setRelationTo(RecordsRelation relationTo) {
		this.relationTo = relationTo;
	}

	public RecordsRelation getRelationFrom() {
		return relationFrom;
	}

	public void setRelationFrom(RecordsRelation relationFrom) {
		this.relationFrom = relationFrom;
	}

	public boolean isFetched() {
		return fetched;
	}

	public void setFetched(boolean fetched) {
		this.fetched = fetched;
	}

	public List<DocumentModel> getRecords() {
		return records;
	}

	public void setRecords(List<DocumentModel> records) {
		this.records = records;
	}

	public List<String> getIds() {
		if (this.ids == null && this.records != null) {
			this.ids = new ArrayList<>(this.records.size());
			for (DocumentModel record : records) {
				this.ids.add(record.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID)
						.getValue(String.class));
			}
		}
		return this.ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}
	
	@Override
	public boolean equals(Object other) {
		boolean equals = false;
		
		if(other != null && other instanceof Entity) {
			Entity otherEntity = (Entity) other;
			equals = StringUtils.equals(this.type, otherEntity.getType());
		}
		
		return equals;
	}

}
