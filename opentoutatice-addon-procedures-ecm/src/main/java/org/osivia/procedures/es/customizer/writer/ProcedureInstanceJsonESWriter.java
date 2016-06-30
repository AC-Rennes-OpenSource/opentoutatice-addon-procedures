/**
 * 
 */
package org.osivia.procedures.es.customizer.writer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.platform.dublincore.service.DublinCoreStorageService;
import org.osivia.procedures.constants.ProceduresConstants;

import fr.toutatice.ecm.es.customizer.writers.api.AbstractCustomJsonESWriter;


/**
 * @author david
 *
 */
public class ProcedureInstanceJsonESWriter extends AbstractCustomJsonESWriter {

    /**
     * Default constructor.
     */
    public ProcedureInstanceJsonESWriter() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(DocumentModel doc) {
        return ProceduresConstants.PI_TYPE.equals(doc.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeData(JsonGenerator jg, DocumentModel pi, String[] schemas, Map<String, String> contextParameters) throws IOException {
        // Custom name /value
        ListProperty valuesProp = (ListProperty) pi.getProperty(ProceduresConstants.PI_VALUES_XPATH);

        if (valuesProp != null && !valuesProp.isEmpty()) {
            jg.writeFieldName(ProceduresConstants.PI_VALUES_XPATH);
            jg.writeStartArray();

            for (Property valueProp : valuesProp) {
                String name = (String) valueProp.get(ProceduresConstants.PI_VALUES_ENTRY_KEY).getValue();
                String value = (String) valueProp.get(ProceduresConstants.PI_VALUES_ENTRY_VALUE).getValue();

                jg.writeStartObject();
                jg.writeStringField(name, value);
                jg.writeEndObject();
            }

            jg.writeEndArray();
        }
    }

}
