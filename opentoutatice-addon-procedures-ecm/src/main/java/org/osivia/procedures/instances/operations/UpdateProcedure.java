package org.osivia.procedures.instances.operations;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.task.TaskService;

/**
 * Update procedure operation.
 *
 * @author Dorian Licois
 * @author Cédric Krommenhoek
 */
@Operation(id = UpdateProcedure.ID, category = Constants.CAT_SERVICES, label = "UpdateProcedure", description = "Updates a procedure.")
public class UpdateProcedure {

    /** Operation identifier. */
    public static final String ID = "Services.UpdateProcedure";


    /** Core session. */
    @Context
    private CoreSession session;

    /** Document routing service. */
    @Context
    private DocumentRoutingService documentRoutingService;

    /** Task service. */
    @Context
    private TaskService taskService;


    /** Task title parameter. */
    @Param(name = "taskTitle")
    private String taskTitle;

    /** Task properties parameter. */
    @Param(name = "properties", required = false)
    private Properties properties;

    /** Task actor groups parameter. */
    @Param(name = "groups", required = false)
    private StringList groups;

    /** Task actor users parameter. */
    @Param(name = "users", required = false)
    private StringList users;

    /** Task additional authorizations parameter. */
    @Param(name = "additionalAuthorizations", required = false)
    private StringList additionalAuthorizations;


    /**
     * Constructor.
     */
    public UpdateProcedure() {
        super();
    }


    /**
     * Run operation.
     *
     * @param procedureInstance procedure instance
     * @return updated procedure instance
     * @throws Exception
     */
    @OperationMethod
    public DocumentModel run(DocumentModel procedureInstance) throws Exception {
        UpdateProcedureUnrestrictedSessionRunner unrestrictedSessionRunner = new UpdateProcedureUnrestrictedSessionRunner(this.session, procedureInstance,
                this.taskTitle, this.properties, this.users, this.additionalAuthorizations);
        unrestrictedSessionRunner.runUnrestricted();

        return unrestrictedSessionRunner.getProcedureInstance();
    }

}
