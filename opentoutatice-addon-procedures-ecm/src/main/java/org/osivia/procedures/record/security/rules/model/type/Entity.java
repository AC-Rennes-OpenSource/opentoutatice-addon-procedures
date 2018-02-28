/**
 * 
 */
package org.osivia.procedures.record.security.rules.model.type;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.RecordsConstants;
import org.osivia.procedures.record.security.rules.model.relation.Relation;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

/**
 * @author david
 *
 */
public class Entity {

	private String type;
	private boolean isSecurity;

	private Relation relationTo;
	private Relation relationFrom;

	private boolean fetch;

	private List<String> ids;
	private List<DocumentModel> records;

	public Entity(String type) {
		super();
		this.type = type;
		this.records = new ArrayList<>();
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

	public Relation getRelationTo() {
		return relationTo;
	}

	public void setRelationTo(Relation relationTo) {
		this.relationTo = relationTo;
	}

	public Relation getRelationFrom() {
		return relationFrom;
	}

	public void setRelationFrom(Relation relationFrom) {
		this.relationFrom = relationFrom;
	}

	public boolean isFetch() {
		return fetch;
	}

	public void setFetch(boolean fetch) {
		this.fetch = fetch;
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

}
