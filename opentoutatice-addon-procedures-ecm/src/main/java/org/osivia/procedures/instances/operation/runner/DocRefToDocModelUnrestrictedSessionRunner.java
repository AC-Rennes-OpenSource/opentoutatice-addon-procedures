package org.osivia.procedures.instances.operation.runner;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;


public class DocRefToDocModelUnrestrictedSessionRunner extends UnrestrictedSessionRunner {

    private final DocumentRef documentRef;

    private DocumentModel document;

    public DocRefToDocModelUnrestrictedSessionRunner(CoreSession session, DocumentRef documentRef) {
        super(session);
        this.documentRef = documentRef;
    }

    @Override
    public void run() throws ClientException {
        setDocument(session.getDocument(documentRef));
    }

    /**
     * Getter for document.
     * @return the document
     */
    public DocumentModel getDocument() {
        return document;
    }

    /**
     * Setter for document.
     * @param document the document to set
     */
    public void setDocument(DocumentModel document) {
        this.document = document;
    }
}
