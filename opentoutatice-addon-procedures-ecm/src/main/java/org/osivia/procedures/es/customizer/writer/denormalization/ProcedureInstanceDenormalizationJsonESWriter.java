/**
 * 
 */
package org.osivia.procedures.es.customizer.writer.denormalization;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.task.TaskConstants;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.es.customizer.ESCustomizerConstants;
import org.osivia.procedures.es.customizer.ProcedureDenormalizationHelper;

import fr.toutatice.ecm.es.customizer.writers.denormalization.AbstractDenormalizationJsonESWriter;


/**
 * @author david
 *
 */
public class ProcedureInstanceDenormalizationJsonESWriter extends AbstractDenormalizationJsonESWriter {
    
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
    protected void denormalizeDoc(JsonGenerator jg, DocumentModel pi, String[] schemas, Map<String, String> contextParameters) throws IOException {
        DocumentModel task = ProcedureDenormalizationHelper.getInstance().getTaskOfProcedureInstance(super.session, pi);
        if(task != null){
            jg.writeFieldName(ESCustomizerConstants.TASK_IN_PI_KEY);
            super.jsonESWriter.writeESDocument(jg, task, schemas, contextParameters);
        }
    }

}
