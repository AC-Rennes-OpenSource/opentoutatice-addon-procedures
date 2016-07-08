/**
 * 
 */
package org.osivia.procedures.es.customizer.writer.helper;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;


/**
 * @author david
 *
 */
public class DenormalizationJsonESWriterHelper {

    /**
     * Utility class.
     */
    private DenormalizationJsonESWriterHelper() {
        super();    
    }
    
    /**
     * listPropXPath refers a ListProperty of document.
     * List is of the form: [{entryKey: a, entryValue: b}, {entryKey: c, entryValue: d}, .. ]
     * This method writes the list as an map: {a : b, c : d, ...}.
     * 
     * @param jg
     * @param doc
     * @param listPropXPath
     * @param entryKey
     * @param entryValue
     * @return
     * @throws JsonGenerationException
     * @throws IOException
     */
    public static JsonGenerator mapKeyValue(JsonGenerator jg, DocumentModel doc, String listPropXPath, 
            String entryKey, String entryValue) throws JsonGenerationException, IOException {
        
        ListProperty valuesProp = (ListProperty) doc.getProperty(listPropXPath);

        if (valuesProp != null && !valuesProp.isEmpty()) {
            jg.writeFieldName(listPropXPath);
            jg.writeStartObject();

            for (Property valueProp : valuesProp) {
                String name = (String) valueProp.get(entryKey).getValue();
                String value = (String) valueProp.get(entryValue).getValue();

                jg.writeStringField(name, value);
            }

            jg.writeEndObject();
        }
        
        return jg;
    }

}
