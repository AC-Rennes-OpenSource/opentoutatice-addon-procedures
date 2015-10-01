package org.osivia.procedures.instances.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;
import org.osivia.procedures.utils.UsersHelper;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;




/**
 * @author dorian
 */
@Operation(id = StartProcedure.ID, category = Constants.CAT_SERVICES, label = "StartProcedure", description = "Starts a procedure.")
public class StartProcedure {

    /** ID */
    public static final String ID = "Services.StartProcedure";

    /** INSTANCE_CONTAINER_PATH */
    private static final String INSTANCE_CONTAINER_PATH = "/default-domain/procedures-instances";

    /** INSTANCE_TYPE */
    private static final String INSTANCE_TYPE = "ProcedureInstance";

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
    public DocumentModel run() throws Exception {

        UnrestrictedStartProcedure unrestrictedStartProcedure = new UnrestrictedStartProcedure(this.session);
        unrestrictedStartProcedure.runUnrestricted();

        return unrestrictedStartProcedure.getProcedureInstance();
    }

    private class UnrestrictedStartProcedure extends UnrestrictedSessionRunner {

        private DocumentModel procedureInstance;

        protected UnrestrictedStartProcedure(CoreSession session) {
            super(session);
        }

        @Override
        public void run() throws ClientException {
            // retrieve the generic model used for procedures
            String id = StartProcedure.this.documentRoutingService.getRouteModelDocIdWithId(this.session, "generic-model");
            DocumentModel genericModel = this.session.getDocument(new IdRef(id));

            // retrieve the model of the current procedure
            String procedureModelPath = StartProcedure.this.properties.get("pi:procedureModelPath");
            DocumentModel procedureModel = this.session.getDocument(new PathRef(procedureModelPath));

            // create documentModel based on ProcedureInstance
            DocumentModel procedureInstanceModel = this.session.createDocumentModel(INSTANCE_CONTAINER_PATH, procedureModel.getName(), INSTANCE_TYPE);

            // create procedureInstance based on documentModel
            this.procedureInstance = this.session.createDocument(procedureInstanceModel);

            // session.saveDocument(procedureInstance);

            // update the procedureInstance with properties in parameters
            try {
                DocumentHelper.setProperties(this.session, this.procedureInstance, StartProcedure.this.properties);
            } catch (IOException e) {
                throw new ClientException(e);
            }
            this.procedureInstance = this.session.saveDocument(this.procedureInstance);


            // create a new task and end it to run a procedure cycle
            List<String> currentDocIds = new ArrayList<String>(1);
            currentDocIds.add(this.procedureInstance.getId());
            String processId = StartProcedure.this.documentRoutingService.createNewInstance(genericModel.getName(), currentDocIds, this.session, true);
            List<Task> allTaskInstances = StartProcedure.this.taskService.getAllTaskInstances(processId, this.session);
            StartProcedure.this.documentRoutingService.endTask(this.session, allTaskInstances.get(0), new HashMap<String, Object>(0), StringUtils.EMPTY);

            // create a new task
            allTaskInstances = StartProcedure.this.taskService.getAllTaskInstances(processId, this.session);
            allTaskInstances.get(0).setName(StartProcedure.this.taskTitle);
            String[] groups = {"equipe-dev"};
            String[] usersOfGroup = UsersHelper.getUsersOfGroup(groups);
            allTaskInstances.get(0).setActors(Arrays.asList(usersOfGroup));
            ToutaticeDocumentHelper.saveDocumentSilently(this.session, allTaskInstances.get(0).getDocument(), true);
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
