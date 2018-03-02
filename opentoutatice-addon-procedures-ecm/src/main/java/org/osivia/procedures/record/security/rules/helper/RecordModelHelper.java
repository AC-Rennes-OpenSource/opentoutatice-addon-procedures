/**
 * 
 */
package org.osivia.procedures.record.security.rules.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.osivia.procedures.record.RecordsConstants;
import org.osivia.procedures.record.security.rules.model.type.Entity;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

/**
 * @author david
 *
 */
public class RecordModelHelper {
	
	public static final String RECORD_MODEL_QUERY = "select * from RecordFolder where "
			+ StringUtils.substringAfter(RecordsConstants.DEFAULT_FILTER, " and");
	
	public static final Pattern MODEL_WEBID_PATTERN = Pattern
			.compile("\\\"recordFolderWebId\\\":\\\"([0-9a-zA-Z]{6}){1}\\\"");
	
	private RecordModelHelper() {
		super();
	}

	public static String getModelType(MapProperty relationProperty) {
		String typeAsString = (String) relationProperty.get("varOptions").getValue();
		List<String> ids = RecordHelper.getIds(new ArrayList<String>(0), typeAsString, MODEL_WEBID_PATTERN);
		return CollectionUtils.isNotEmpty(ids) ? ids.get(0) : null;
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
