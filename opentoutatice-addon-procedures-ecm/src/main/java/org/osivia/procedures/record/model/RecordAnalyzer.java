/**
 * 
 */
package org.osivia.procedures.record.model;

import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * @author david
 *
 */
public class RecordAnalyzer {
	
	private RecordAnalyzer() {
		super();
	}
	
	public static DocumentModel getRecordModelOf(DocumentModel record) {
		return ToutaticeDocumentHelper.getUnrestrictedParent(record);
	}

}
