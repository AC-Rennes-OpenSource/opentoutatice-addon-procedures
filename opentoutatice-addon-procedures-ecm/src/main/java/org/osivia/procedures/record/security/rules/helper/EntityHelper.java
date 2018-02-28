/**
 * 
 */
package org.osivia.procedures.record.security.rules.helper;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * @author david
 *
 */
public class EntityHelper {

	public static final String UNKNOWN_TYPE = "UNKNOWN_TYPE";

	private EntityHelper() {
		super();
	}
	
	public static String getType(DocumentModel model) {
		return (String) model.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID).getValue(String.class);
	}

	public static String getType(CoreSession session, DocumentModel record) {
		String type = UNKNOWN_TYPE;

		DocumentModel recordModel = ToutaticeDocumentHelper.getUnrestrictedParent(session, record);
		if (recordModel != null) {
			type = (String) ToutaticeDocumentHelper.getUnrestrictedProperty(session, recordModel.getId(), ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
		}

		return type;
	}
	
//	public static String getType(CoreSession systemSession, DocumentModel record) {
//		String type = UNKNOWN_TYPE;
//
//		DocumentModel recordModel = systemSession.getParentDocument(record.getRef());
//		if (recordModel != null) {
//			type = recordModel.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID).getValue(String.class);
//		}
//
//		return type;
//	}

}
