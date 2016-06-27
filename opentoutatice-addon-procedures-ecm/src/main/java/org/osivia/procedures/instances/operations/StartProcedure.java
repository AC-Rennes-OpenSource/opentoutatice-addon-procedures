package org.osivia.procedures.instances.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.Blob;
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

	/** groups */
	@Param(name = "groups", required = true)
	private StringList groups;

    @Context
    CoreSession session;

    @Context
    DocumentRoutingService documentRoutingService;

    @Context
    TaskService taskService;

    @OperationMethod
    public DocumentModel run() throws Exception {
        UnrestrictedStartProcedure unrestrictedStartProcedure = new UnrestrictedStartProcedure(session, null);
        unrestrictedStartProcedure.runUnrestricted();

        return unrestrictedStartProcedure.getProcedureInstance();
    }

    @OperationMethod
    public DocumentModel run(Blob blob) throws Exception {

        BlobList blobList = new BlobList();
        blobList.add(blob);
        UnrestrictedStartProcedure unrestrictedStartProcedure = new UnrestrictedStartProcedure(session, blobList);
        unrestrictedStartProcedure.runUnrestricted();

        return unrestrictedStartProcedure.getProcedureInstance();
    }

    @OperationMethod
    public DocumentModel run(BlobList blobList) throws Exception {

        UnrestrictedStartProcedure unrestrictedStartProcedure = new UnrestrictedStartProcedure(session, blobList);
        unrestrictedStartProcedure.runUnrestricted();

        return unrestrictedStartProcedure.getProcedureInstance();
    }

    private class UnrestrictedStartProcedure extends UnrestrictedSessionRunner {

        private DocumentModel procedureInstance;

        private BlobList blobList;

        protected UnrestrictedStartProcedure(CoreSession session, BlobList blobList) {
            super(session);
            this.blobList = blobList;
        }

        @Override
        public void run() throws ClientException {
            // retrieve the generic model used for procedures
            String id = documentRoutingService.getRouteModelDocIdWithId(session, "generic-model");
            DocumentModel genericModel = session.getDocument(new IdRef(id));

            List<String> currentDocIds = new ArrayList<String>(1);
            // create the procedure Instance
			createProcedureInstance();
            currentDocIds.add(procedureInstance.getId());

            // attach blobs to procedure instance
            if (blobList != null) {
                List<Map<String, Object>> attachmentsTypeList = (List<Map<String, Object>>) procedureInstance.getPropertyValue("pi:attachments");
                if (attachmentsTypeList != null) {
                    // for each file element in the document properties
                    int i = 0;
                    for (Map<String, Object> attachmentsmap : attachmentsTypeList) {
                        Object blobObject = attachmentsmap.get("blob");
                        if (blobObject == null) {
                            // find the corresponding blob and add it to the document
                            DocumentHelper.addBlob(procedureInstance.getProperty("pi:attachments/" + i + "/blob"),
                                    getBlobByFileName((String) attachmentsmap.get("fileName")));
                        }
                        i++;
                    }
                    session.saveDocument(procedureInstance);
                }
            }

            // associate objects to workflow
            List<Map<String, Object>> procedureObjectInstancesList = (List<Map<String, Object>>) procedureInstance
                    .getPropertyValue("pi:procedureObjectInstances");
            if (procedureObjectInstancesList != null) {
                for (Map<String, Object> procedureObjectInstancesMap : procedureObjectInstancesList) {
                    String procedureObjectId = (String) procedureObjectInstancesMap.get("procedureObjectId");
                    if (procedureObjectId != null) {
                        currentDocIds.add(procedureObjectId);
                    }
                }
            }


            String processId = documentRoutingService.createNewInstance(genericModel.getName(), currentDocIds, session, true);
            List<Task> allTaskInstances = taskService.getAllTaskInstances(processId, session);
            documentRoutingService.endTask(session, allTaskInstances.get(0), new HashMap<String, Object>(0), StringUtils.EMPTY);

            // create a new task
            allTaskInstances = taskService.getAllTaskInstances(processId, session);
            allTaskInstances.get(0).setName(taskTitle);
            // allTaskInstances.get(0).setType(arg0);
            String[] usersOfGroup = UsersHelper.getUsersOfGroup(groups);
            allTaskInstances.get(0).setActors(Arrays.asList(usersOfGroup));
            ToutaticeDocumentHelper.saveDocumentSilently(session, allTaskInstances.get(0).getDocument(), true);
        }

        /**
         * find the blob corresponding to the provided hashcode in the bloblist
         */
        private Blob getBlobByFileName(String fileName) {
            for (Blob blob : blobList) {
                if (StringUtils.equals(String.valueOf(blob.getFilename()), fileName)) {
                    return blob;
                }
            }
            return null;
        }

        /**
         * create the procedure Instance
         */
		private void createProcedureInstance() {
            // retrieve the model of the current procedure
            String procedureModelPath = properties.get("pi:procedureModelPath");
            DocumentModel procedureModel = session.getDocument(new PathRef(procedureModelPath));

            // create documentModel based on ProcedureInstance
            DocumentModel procedureInstanceModel = session.createDocumentModel(INSTANCE_CONTAINER_PATH, procedureModel.getName(), INSTANCE_TYPE);

            // create procedureInstance based on documentModel
            procedureInstance = session.createDocument(procedureInstanceModel);

            // update the procedureInstance with properties in parameters
            try {
                DocumentHelper.setProperties(session, procedureInstance, properties);
            } catch (IOException e) {
                throw new ClientException(e);
            }
            procedureInstance = session.saveDocument(procedureInstance);
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
}
