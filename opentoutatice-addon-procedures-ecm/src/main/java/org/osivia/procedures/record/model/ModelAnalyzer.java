/**
 * 
 */
package org.osivia.procedures.record.model;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.osivia.procedures.constants.ProceduresConstants;

/**
 * @author david
 *
 */
public class ModelAnalyzer {
	
	private static ModelAnalyzer instance;
	
	private ModelAnalyzer() {
		super();
	}
	
	public static synchronized ModelAnalyzer getInstance() {
		if(instance == null) {
			instance = new ModelAnalyzer();
		}
		return instance;
	}
	
	public String getFieldType(DocumentModel model, String fieldName) {
		String fieldType = null;
		
		ListProperty fields = (ListProperty) model.getProperty(ProceduresConstants.PROCEDURE_DEFINITIONS_XPATH);
		
		if(fields != null) {
			Iterator<Property> iterator = fields.iterator();
			
			while(iterator.hasNext() && fieldType == null) {
				MapProperty field = (MapProperty) iterator.next();
				
				if(StringUtils.equals(fieldName, field.get("name").getValue(String.class))) {
					fieldType = field.get("type").getValue(String.class);
				}
			}
		}
		
		return fieldType;
	}
	
}
