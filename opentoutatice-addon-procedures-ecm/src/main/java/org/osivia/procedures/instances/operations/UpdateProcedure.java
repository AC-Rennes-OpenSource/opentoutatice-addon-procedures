package org.osivia.procedures.instances.operations;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;
import org.osivia.procedures.utils.UsersHelper;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * @author dorian
 */
@Operation(id = UpdateProcedure.ID, category = Constants.CAT_SERVICES, label = "UpdateProcedure", description = "Updates a procedure.")
public class UpdateProcedure {

    /** ID */
    public static final String ID = "Services.UpdateProcedure";

    /** properties of the created instance */
    @Param(name = "properties", required = false)
    private Properties properties;

    /** taskTitle */
    @Param(name = "taskTitle", required = true)
    private String taskTitle;

    @Context
    CoreSession session;

    @Context
    DocumentRoutingService documentRoutingService;

    @Context
    TaskService taskService;

    @OperationMethod
    public DocumentModel run(DocumentModel procedureInstance) throws Exception {

        UnrestrictedUpdateProcedure unrestrictedStartProcedure = new UnrestrictedUpdateProcedure(this.session, procedureInstance,
                (NuxeoPrincipal) this.session.getPrincipal());
        unrestrictedStartProcedure.runUnrestricted();

        return unrestrictedStartProcedure.getProcedureInstance();
    }

    private class UnrestrictedUpdateProcedure extends UnrestrictedSessionRunner {

        private DocumentModel procedureInstance;

        private NuxeoPrincipal principal;

        protected UnrestrictedUpdateProcedure(CoreSession session, DocumentModel procedureInstance, NuxeoPrincipal principal) {
            super(session);
            this.procedureInstance = procedureInstance;
            this.principal = principal;
        }

        @Override
        public void run() throws ClientException {
            // update the document
            try {
                DocumentHelper.setProperties(this.session, this.procedureInstance, UpdateProcedure.this.properties);
            } catch (IOException e) {
                throw new ClientException(e);
            }
            this.procedureInstance = this.session.saveDocument(this.procedureInstance);

            // end the related Task, run a procedure cycle
            List<Task> currentTaskInstances = UpdateProcedure.this.taskService.getTaskInstances(this.procedureInstance, this.principal, this.session);
            String processId = currentTaskInstances.get(0).getProcessId();

            UpdateProcedure.this.documentRoutingService.endTask(this.session, currentTaskInstances.get(0), new HashMap<String, Object>(0), StringUtils.EMPTY);

            // create a new task
            currentTaskInstances = UpdateProcedure.this.taskService.getAllTaskInstances(processId, this.session);
            if (CollectionUtils.isNotEmpty(currentTaskInstances)) {
                currentTaskInstances.get(0).setName(UpdateProcedure.this.taskTitle);
                String[] groups = {"equipe-dev"};
                String[] usersOfGroup = UsersHelper.getUsersOfGroup(groups);
                currentTaskInstances.get(0).setActors(Arrays.asList(usersOfGroup));
                ToutaticeDocumentHelper.saveDocumentSilently(this.session, currentTaskInstances.get(0).getDocument(), true);
            }
        }


        /**
         * Getter for procedureInstance.
         *
         * @return the procedureInstance
         */
        public DocumentModel getProcedureInstance() {
            return this.procedureInstance;
        }

    }
}
