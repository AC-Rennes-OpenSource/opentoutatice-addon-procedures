package org.osivia.procedures.instances.operations;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;
import org.nuxeo.ecm.platform.userworkspace.api.UserWorkspaceService;

@Operation(id = CreateDocumentFromBlob.ID, category = Constants.CAT_SERVICES, label = "CreateDocumentFromBlob",
        description = "Creates a document file in the current user workspace from a blob in another document.")
public class CreateDocumentFromBlob {

    /** ID */
    public static final String ID = "Services.CreateDocumentFromBlob";

    @Param(name = "variableName", required = true)
    private String variableName;

    @Param(name = "overwite", required = false)
    protected Boolean overwite = false;

    @Context
    CoreSession session;

    @Context
    UserWorkspaceService userWorkspaceService;

    @Context
    FileManager fileManager;


    @OperationMethod
    public DocumentModel run(DocumentModel documentModel) throws Exception {

        DocumentModel currentUserPersonalWorkspace = userWorkspaceService.getCurrentUserPersonalWorkspace(session, documentModel);

        return fileManager.createDocumentFromBlob(session, getBlobFromDocument(documentModel), currentUserPersonalWorkspace.getPathAsString(), overwite,
                getBlobFromDocument(documentModel).getFilename());
    }

    private Blob getBlobFromDocument(DocumentModel documentModel) {

        List<Map<String, Object>> attachmentsTypeList = (List<Map<String, Object>>) documentModel.getPropertyValue("attachments");
        for (Map<String, Object> attachmentsmap : attachmentsTypeList) {
            if (StringUtils.equals((String) attachmentsmap.get("variableName"), variableName)) {
                return (Blob) attachmentsmap.get("blob");
            }
        }

        return null;
    }
}
