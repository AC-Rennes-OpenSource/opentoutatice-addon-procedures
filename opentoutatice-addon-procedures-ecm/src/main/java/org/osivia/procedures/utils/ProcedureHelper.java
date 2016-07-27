/**
 * 
 */
package org.osivia.procedures.utils;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.routing.core.api.DocumentRoutingEngineService;
import org.nuxeo.ecm.platform.routing.core.impl.DocumentRouteImpl;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskImpl;
import org.nuxeo.ecm.platform.task.TaskService;
import org.nuxeo.runtime.api.Framework;
import org.osivia.procedures.exception.ProcedureException;


/**
 * @author david
 *
 */
public class ProcedureHelper {
    
    /** Routing service. */
    private DocumentRoutingService routingService;
    /** Routing engine service. */
    private DocumentRoutingEngineService engineService;
    /** Task service. */
    private TaskService taskService;
    
    /** ProcedureHelper instance. */
    private static ProcedureHelper instance;

    /**
     * Singleton.
     */
    private ProcedureHelper() {
        super();
        this.routingService = Framework.getService(DocumentRoutingService.class);
        this.engineService = Framework.getService(DocumentRoutingEngineService.class);
        this.taskService = Framework.getService(TaskService.class);;
    }
    
    /**
     * Getter for ProcedureHelper instance.
     * 
     * @return ProcedureHelper instance.
     */
    public synchronized static ProcedureHelper getInstance(){
        if(instance == null){
            instance = new ProcedureHelper();
        }
        return instance;
    }
    
    /**
     * Gets procedure (ready or running) as DocumentRoute.
     * 
     * @return procedure as DocumentRoute.
     */
    public DocumentRoute getProcedureAsRoute(CoreSession session, DocumentModel procedureInstance) {
        List<DocumentRoute> documentRoutes = this.routingService.getDocumentRoutesForAttachedDocument(session, procedureInstance.getId());
        if(CollectionUtils.isNotEmpty(documentRoutes)){
            return documentRoutes.get(0);
        }
        return new DocumentRouteImpl(null,  null);
    }
    
    /**
     * Gets the current task for given procedure
     * and given actors.
     * 
     * @param session
     * @param procedureInstance
     * @param actors
     * @return
     */
    public Task getCurrentTask(CoreSession session, DocumentModel procedureInstance, List<String> actors) {
        List<Task> taskInstances = this.taskService.getTaskInstances(procedureInstance, actors, session);
        if(taskInstances != null && taskInstances.size() > 1){
            return taskInstances.get(0);
        } 
        return new TaskImpl(null);
    }

}
