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
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;

@Operation(id = UpdateDocumentFromBlob.ID, category = Constants.CAT_SERVICES, label = "UpdateDocumentFromBlob",
        description = "Updates a document from a file and its properties")
public class UpdateDocumentFromBlob {

    /** ID */
    public static final String ID = "Services.UpdateDocumentFromBlob";

    @Param(name = "properties", required = false)
    private Properties properties;

    @Param(name = "path", required = true)
    private String path;

    @Context
    CoreSession session;

    @Context
    FileManager fileManager;

    @OperationMethod
    public DocumentModel run(Blob blob) throws Exception {

        DocumentModel document = session.getDocument(new PathRef(path));

        document.setProperty("file", "content", blob);

        DocumentHelper.setProperties(session, document, properties);

        return session.saveDocument(document);
    }
}
