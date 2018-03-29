/**
 * 
 */
package org.osivia.procedures.record.security.rules.helper;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.RecordsConstants;
import org.osivia.procedures.record.security.rules.model.type.Entity;
import org.osivia.procedures.record.security.rules.model.type.FieldsConstants;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * @author david
 *
 */
public class RecordModelHelper {
	
	public static final String RECORD_MODELS_QUERY = "select * from RecordFolder where "
			+ StringUtils.substringAfter(RecordsConstants.DEFAULT_FILTER, " and");
	
	private RecordModelHelper() {
		super();
	}
	
	public static ListProperty getFieldsDefinitions(DocumentModel recordModel) {
		return (ListProperty) recordModel
				.getProperty(ProceduresConstants.PROCEDURE_DEFINITIONS_XPATH);
	}

	public static String getModelType(MapProperty relationProperty) {
        String value = (String) relationProperty.get(FieldsConstants.VAR_OPTIONS).getValue();

        String webId;
        try {
            JSONObject object = JSONObject.fromObject(value);
            webId = object.getString("recordFolderWebId");
        } catch (JSONException e) {
            webId = null;
        }

        return webId;
	}
	
	public static String getType(DocumentModel model) {
		return model.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID).getValue(String.class);
	}

	public static DocumentModel getModelOf(DocumentModelList models, Entity entity) {
		DocumentModel modelOfEntity = null;
		
		Iterator<DocumentModel> iterator = models.iterator();
		while(iterator.hasNext() && modelOfEntity == null) {
			DocumentModel model = iterator.next();
			String modelType = model.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID).getValue(String.class);
			
			if(StringUtils.equals(entity.getType(), modelType)) {
				modelOfEntity = model;
			}
		}

		return modelOfEntity;
	}
	
}
