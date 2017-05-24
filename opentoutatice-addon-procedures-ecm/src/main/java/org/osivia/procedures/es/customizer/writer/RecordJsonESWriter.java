package org.osivia.procedures.es.customizer.writer;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.es.customizer.writer.helper.DenormalizationJsonESWriterHelper;

import fr.toutatice.ecm.es.customizer.writers.api.AbstractCustomJsonESWriter;


/**
 * @author Dorian Licois
 */
public class RecordJsonESWriter extends AbstractCustomJsonESWriter {

    /**
     * Constructor.
     */
    public RecordJsonESWriter() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(DocumentModel doc) {
        return ProceduresConstants.RECORD_TYPE.equals(doc.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeData(JsonGenerator jg, DocumentModel pi, String[] schemas, Map<String, String> contextParameters) throws IOException {
        // Custom name /value
        DenormalizationJsonESWriterHelper.mapKeyValue(jg, pi, ProceduresConstants.RCD_VALUES_XPATH, ProceduresConstants.PI_ENTRY_KEY,
                ProceduresConstants.ENTRY_VALUE);
    }

}
