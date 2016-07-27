package org.osivia.procedures.instances.operations;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
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
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskConstants;
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

    /** taskType */
    @Param(name = "taskType", required = false)
    private String taskType;

	/** groups */
    @Param(name = "groups", required = false)
	private StringList groups;

    /** users */
    @Param(name = "users", required = false)
    private StringList users;

    @Context
    CoreSession session;

    @Context
    DocumentRoutingService documentRoutingService;

    @Context
    TaskService taskService;

    @OperationMethod
    public DocumentModel run() throws Exception {
        UnrestrictedStartProcedure unrestrictedStartProcedure = new UnrestrictedStartProcedure(session, null, (NuxeoPrincipal) session.getPrincipal());
        unrestrictedStartProcedure.runUnrestricted();

        return unrestrictedStartProcedure.getProcedureInstance();
    }

    @OperationMethod
    public DocumentModel run(Blob blob) throws Exception {

        BlobList blobList = new BlobList();
        blobList.add(blob);
        UnrestrictedStartProcedure unrestrictedStartProcedure = new UnrestrictedStartProcedure(session, blobList, (NuxeoPrincipal) session.getPrincipal());
        unrestrictedStartProcedure.runUnrestricted();

        return unrestrictedStartProcedure.getProcedureInstance();
    }

    @OperationMethod
    public DocumentModel run(BlobList blobList) throws Exception {

        UnrestrictedStartProcedure unrestrictedStartProcedure = new UnrestrictedStartProcedure(session, blobList, (NuxeoPrincipal) session.getPrincipal());
        unrestrictedStartProcedure.runUnrestricted();

        return unrestrictedStartProcedure.getProcedureInstance();
    }

    private class UnrestrictedStartProcedure extends UnrestrictedSessionRunner {

        private DocumentModel procedureInstance;

        private BlobList blobList;

        private NuxeoPrincipal principal;

        protected UnrestrictedStartProcedure(CoreSession session, BlobList blobList, NuxeoPrincipal principal) {
            super(session);
            this.blobList = blobList;
            this.principal = principal;
        }

        @Override
        public void run() throws ClientException {
            // retrieve the generic model used for procedures
            String id = documentRoutingService.getRouteModelDocIdWithId(session, "generic-model");
            DocumentModel genericModel = session.getDocument(new IdRef(id));

            List<String> currentDocIds = new ArrayList<String>(1);
            // create the procedure Instance
            ArrayList<Map<String, Serializable>> stepTaskVariables = createProcedureInstance(principal.getName());
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
            // documentRoutingService.endTask(session, allTaskInstances.get(0), new HashMap<String, Object>(0), StringUtils.EMPTY);

            // create a new task
            allTaskInstances = taskService.getAllTaskInstances(processId, session);
            DocumentModel taskDocument = allTaskInstances.get(0).getDocument();
            taskDocument.setPropertyValue(TaskConstants.TASK_NAME_PROPERTY_NAME, taskTitle);
            taskDocument.setPropertyValue(TaskConstants.TASK_TYPE_PROPERTY_NAME, taskType);
            ArrayList<Map<String, Serializable>> taskVariables = (ArrayList<Map<String, Serializable>>) taskDocument
                    .getPropertyValue(TaskConstants.TASK_VARIABLES_PROPERTY_NAME);
            if (taskVariables != null) {
                taskVariables.addAll(stepTaskVariables);
            } else {
                taskVariables = stepTaskVariables;
            }
            taskDocument.setPropertyValue(TaskConstants.TASK_VARIABLES_PROPERTY_NAME, taskVariables);

            ArrayList<String> usersAndGroupUsers = new ArrayList<String>();
            if (groups != null) {
                List<String> usersOfGroup = Arrays.asList(UsersHelper.getUsersOfGroup(groups));
                usersAndGroupUsers.addAll(usersOfGroup);
            }
            if (users != null) {
                usersAndGroupUsers.addAll(users);
            }
            taskDocument.setPropertyValue(TaskConstants.TASK_USERS_PROPERTY_NAME, usersAndGroupUsers);
            // Set ACLs for Actors on Task
            ACP acp = taskDocument.getACP();
            ACL acl = acp.getOrCreateACL(ACL.LOCAL_ACL);
            for (String actorId : usersAndGroupUsers) {
                acl.add(new ACE(actorId, SecurityConstants.EVERYTHING, true));
            }
            acp.addACL(acl);
            taskDocument.setACP(acp, true);

            ToutaticeDocumentHelper.saveDocumentSilently(session, taskDocument, true);
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
        private ArrayList<Map<String, Serializable>> createProcedureInstance(String procedureInitiator) {
            // retrieve the model of the current procedure
            String procedureModelPath = properties.get("pi:procedureModelPath");
            DocumentModel procedureModel = session.getDocument(new PathRef(procedureModelPath));

            ArrayList<Map<String, Serializable>> stepTaskVariables = null;
            List<Map<String, Object>> steps = (List<Map<String, Object>>) procedureModel.getPropertyValue("pcd:steps");
            if (steps != null) {
                for (Map<String, Object> stepMap : steps) {
                    String reference = (String) stepMap.get("reference");
                    if (StringUtils.equals(reference, properties.get("pi:currentStep"))) {
                        stepTaskVariables = new ArrayList<Map<String, Serializable>>(7);
                        Map<String, Serializable> taskVariableNotifiable = new HashMap<String, Serializable>(2);
                        taskVariableNotifiable.put("key", "notifiable");
                        taskVariableNotifiable.put("value", BooleanUtils.toStringTrueFalse((Boolean) stepMap.get("notifiable")));
                        stepTaskVariables.add(taskVariableNotifiable);

                        Map<String, Serializable> taskVariableAcquitable = new HashMap<String, Serializable>(2);
                        taskVariableAcquitable.put("key", "acquitable");
                        taskVariableAcquitable.put("value", BooleanUtils.toStringTrueFalse((Boolean) stepMap.get("acquitable")));
                        stepTaskVariables.add(taskVariableAcquitable);

                        Map<String, Serializable> taskVariableClosable = new HashMap<String, Serializable>(2);
                        taskVariableClosable.put("key", "closable");
                        taskVariableClosable.put("value", BooleanUtils.toStringTrueFalse((Boolean) stepMap.get("closable")));
                        stepTaskVariables.add(taskVariableClosable);

                        Map<String, Serializable> taskVariableactionIdClosable = new HashMap<String, Serializable>(2);
                        taskVariableactionIdClosable.put("key", "actionIdClosable");
                        taskVariableactionIdClosable.put("value", (String) stepMap.get("actionIdClosable"));
                        stepTaskVariables.add(taskVariableactionIdClosable);

                        Map<String, Serializable> taskVariableActionIdYes = new HashMap<String, Serializable>(2);
                        taskVariableActionIdYes.put("key", "actionIdYes");
                        taskVariableActionIdYes.put("value", (String) stepMap.get("actionIdYes"));
                        stepTaskVariables.add(taskVariableActionIdYes);

                        Map<String, Serializable> taskVariableActionIdNo = new HashMap<String, Serializable>(2);
                        taskVariableActionIdNo.put("key", "actionIdNo");
                        taskVariableActionIdNo.put("value", (String) stepMap.get("actionIdNo"));
                        stepTaskVariables.add(taskVariableActionIdNo);

                        Map<String, Serializable> taskVariableStringMsg = new HashMap<String, Serializable>(2);
                        taskVariableStringMsg.put("key", "stringMsg");
                        taskVariableStringMsg.put("value", (String) stepMap.get("stringMsg"));
                        stepTaskVariables.add(taskVariableStringMsg);
                    }
                }
            }


            // create documentModel based on ProcedureInstance
            DocumentModel procedureInstanceModel = session.createDocumentModel(INSTANCE_CONTAINER_PATH, procedureModel.getName(), INSTANCE_TYPE);

            // create procedureInstance based on documentModel
            procedureInstance = session.createDocument(procedureInstanceModel);

            // set the initiator
            properties.put("pi:procedureInitiator", procedureInitiator);

            // update the procedureInstance with properties in parameters
            try {
                DocumentHelper.setProperties(session, procedureInstance, properties);
            } catch (IOException e) {
                throw new ClientException(e);
            }
            procedureInstance = session.saveDocument(procedureInstance);


            return stepTaskVariables;
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
