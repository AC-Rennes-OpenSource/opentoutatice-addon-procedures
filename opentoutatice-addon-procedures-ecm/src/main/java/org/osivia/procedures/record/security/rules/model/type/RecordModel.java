/**
 * 
 */
package org.osivia.procedures.record.security.rules.model.type;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author david
 *
 */
public class RecordModel {
	
	public DocumentModel document;
	
	public RecordModel(DocumentModel document) {
		super();
		this.document = document;
	}

	public DocumentModel getDocument() {
		return document;
	}

	public void setDocument(DocumentModel document) {
		this.document = document;
	}

}
