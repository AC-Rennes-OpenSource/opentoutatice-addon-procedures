/**
 * 
 */
package org.osivia.procedures.es.customizer.writer.denormalization;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskConstants;
import org.nuxeo.ecm.platform.task.TaskService;
import org.nuxeo.runtime.api.Framework;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.es.customizer.ESCustomizerConstants;
import org.osivia.procedures.es.customizer.ProcedureDenormalizationHelper;

import fr.toutatice.ecm.es.customizer.nx.writer.JsonESDocumentWriterCustomizer;
import fr.toutatice.ecm.es.customizer.writers.denormalization.AbstractDenormalizationJsonESWriter;


/**
 * @author david
 *
 */
public class ProcedureTaskDenormalizationJsonESWriter extends AbstractDenormalizationJsonESWriter {
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(DocumentModel doc) {
        return TaskConstants.TASK_TYPE_NAME.equals(doc.getType());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void denormalizeDoc(JsonGenerator jg, DocumentModel taskDoc, String[] schemas, Map<String, String> contextParameters) throws IOException {
        DocumentModel pi = ProcedureDenormalizationHelper.getInstance().getProcedureInstanceOfTask(super.session, taskDoc);
        if(pi != null){
            jg.writeFieldName(ESCustomizerConstants.PI_IN_TASK_KEY);
            this.jsonESWriter.writeESDocument(jg, pi, pi.getSchemas(), contextParameters);
        }
    }

}
