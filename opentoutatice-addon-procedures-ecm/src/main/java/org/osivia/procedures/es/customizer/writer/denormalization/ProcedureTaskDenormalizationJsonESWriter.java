/**
 * 
 */
package org.osivia.procedures.es.customizer.writer.denormalization;

import java.io.IOException;
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
    public void initializeDenormalizationInfos() {
        
        super.denormalizationInfos = new LinkedHashMap<String, Object>(1){
            
            private static final long serialVersionUID = 7391540965330663468L;

            {   
                List<Object> linkedEntities = new LinkedList<Object>(){
                    
                    private static final long serialVersionUID = 5244086703423115278L;

                    {
                        add(ProceduresConstants.PI_TYPE);
                    }
                };
                put(TaskConstants.TASK_TYPE_NAME, linkedEntities);
            }
        };
        
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
