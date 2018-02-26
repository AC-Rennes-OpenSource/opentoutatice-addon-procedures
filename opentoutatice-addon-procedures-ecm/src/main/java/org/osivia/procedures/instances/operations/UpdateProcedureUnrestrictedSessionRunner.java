package org.osivia.procedures.instances.operations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;
import org.nuxeo.runtime.api.Framework;

/**
 * Update procedure unrestricted session runner.
 *
 * @author Cédric Krommenhoek
 * @see AbstractProcedureUnrestrictedSessionRunner
 */
public class UpdateProcedureUnrestrictedSessionRunner extends AbstractProcedureUnrestrictedSessionRunner {

    /** Procedure instance. */
    private DocumentModel procedureInstance;


    /** Task title. */
    private final String title;
    /** Task properties. */
    private final Properties properties;
    /** Task actors : users and groups. */
    private final StringList actors;
    /** Task additional authorizations. */
    private final StringList additionalAuthorizations;

    /** Document routing service. */
    private final DocumentRoutingService documentRoutingService;
    /** Task service. */
    private final TaskService taskService;


    /**
     * Constructor.
     *
     * @param session core session
     * @param procedureInstance procedure instance
     * @param title task title
     * @param properties task properties
     * @param actors task users and groups
     * @param additionalAuthorizations task additional authorizations
     */
    public UpdateProcedureUnrestrictedSessionRunner(CoreSession session, DocumentModel procedureInstance, String title, Properties properties,
            StringList actors, StringList additionalAuthorizations) {
        super(session, properties);
        this.procedureInstance = procedureInstance;
        this.title = title;
        this.properties = properties;
        this.actors = actors;
        this.additionalAuthorizations = additionalAuthorizations;

        documentRoutingService = Framework.getService(DocumentRoutingService.class);
        taskService = Framework.getService(TaskService.class);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void run() throws ClientException {
        // Procedure model
        DocumentModel model = getModel();

        // Update procedure instance properties
        try {
            DocumentHelper.setProperties(session, procedureInstance, properties);
        } catch (IOException e) {
            throw new ClientException(e);
        }

        // Save document
        procedureInstance = session.saveDocument(procedureInstance);


        // Previous task
        NuxeoPrincipal actor = null;
        List<Task> previousTasks = taskService.getTaskInstances(procedureInstance, actor, session);
        Task previousTask;
        if (previousTasks.size() == 1) {
            previousTask = previousTasks.get(0);
        } else {
            throw new ClientException("Task not found.");
        }

        // Process identifier
        String processId = previousTask.getProcessId();

        // End previous task
        Map<String, Object> data = new HashMap<>(0);
        documentRoutingService.endTask(session, previousTask, data, StringUtils.EMPTY);

        // Create task
        createTask(model, procedureInstance, processId, title, actors, additionalAuthorizations);

        procedureInstance.detach(true);
    }


    /**
     * Getter for procedureInstance.
     *
     * @return the procedureInstance
     */
    public DocumentModel getProcedureInstance() {
        return procedureInstance;
    }

}
