/**
 * 
 */
package org.osivia.procedures.instances.operation.runner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.task.Task;
import org.osivia.procedures.exception.ProcedureException;
import org.osivia.procedures.utils.ProcedureHelper;


/**
 * @author david
 *
 */
public class UnrestrictedResumeProcedure extends UnrestrictedSessionRunner {

    /** Routing service. */
    private DocumentRoutingService routingService;
    /** Procedure instance. */
    private DocumentModel procedureInstance;
    /** Properties of ProcedureInstance to update. */
    private Map<String, String> properties;

    /** taskTitle */
    private String taskTitle;
    /** taskType */
    private String taskType;
    /** groups */
    private StringList groups;
    /** Actors */
    private List<String> actors;

    public UnrestrictedResumeProcedure(CoreSession session, DocumentRoutingService routingService, DocumentModel procedureInstance,
            Map<String, String> properties, List<String> actors) {
        super(session);
        this.routingService = routingService;
        this.procedureInstance = procedureInstance;
        this.actors = actors;
    }

    /**
     * Cancels a procedure.
     */
    @Override
    public void run() throws ClientException {
        updateProcedureInstance();

        resumeProcedure();


    }

    /**
     * Upadtes the ProcedureInstance.
     * 
     * @throws Exception
     */
    public void updateProcedureInstance() throws ClientException {
        try {
            DocumentHelper.setProperties(this.session, this.procedureInstance, this.properties);
        } catch (Exception e) {
            throw new ClientException(e);
        }
        this.procedureInstance = this.session.saveDocument(this.procedureInstance);
    }

    /**
     * Resumes the procedure flow.
     */
    protected void resumeProcedure() {
        ProcedureHelper procedureHelper = ProcedureHelper.getInstance();

        DocumentRoute routeInstance = procedureHelper.getProcedureAsRoute(this.session, this.procedureInstance);
        DocumentModel documentRoute = routeInstance.getDocument();

        if (documentRoute != null) {
            Task currentTask = procedureHelper.getCurrentTask(this.session, documentRoute, this.actors);
            if (currentTask.getDocument() != null) {
                // this.routingService.completeTask(documentRoute.getId(), currentTask.getId(), data, null, this.session);
            } else {
                // FIXME: TODO: nodeId where no task id??
            }


        } else {
            throw new ProcedureException("");
        }
    }

}
