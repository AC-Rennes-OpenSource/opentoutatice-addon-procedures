package org.osivia.procedures.instances.operations;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;

@Operation(id = CreateDocumentFromBlob.ID, category = Constants.CAT_SERVICES, label = "CreateDocumentFromBlob",
        description = "Creates a document from a file and initialize the properties ")
public class CreateDocumentFromBlob {

    /** ID */
    public static final String ID = "Services.CreateDocumentFromBlob";

    @Param(name = "path", required = true)
    private String path;

    @Param(name = "properties", required = false)
    private Properties properties;

    @Param(name = "overwite", required = false)
    protected Boolean overwite = false;

    @Context
    CoreSession session;

    @Context
    FileManager fileManager;

    @OperationMethod
    public DocumentModel run(Blob blob) throws Exception {

        DocumentModel documentBlob = fileManager.createDocumentFromBlob(session, blob, path, overwite, blob.getFilename());
        DocumentHelper.setProperties(session, documentBlob, properties);

        return session.saveDocument(documentBlob);

    }
}
