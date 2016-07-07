/**
 *
 */
package org.osivia.procedures.es.customizer.listener;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.platform.task.TaskConstants;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.es.customizer.ProcedureDenormalizationHelper;

import fr.toutatice.ecm.es.customizer.listeners.denormalization.AbstractDenormalizationESListener;


/**
 * @author david
 *
 */
public class ProcedureInstanceDenormalizationESListener extends AbstractDenormalizationESListener {

    /**
     * Default constructor.
     */
    public ProcedureInstanceDenormalizationESListener() {
        super();
    }

    /**
     * We re-index (with Task denormalized) a ProcedureInstance
     * when it's associated to new Task.
     */
    @Override
    protected boolean needToReIndex(DocumentModel sourceDocument, String eventId) {
        return (DocumentEventTypes.DOCUMENT_CREATED.equals(eventId) && TaskConstants.TASK_TYPE_NAME.equals(sourceDocument.getType()))
                || ProceduresConstants.PI_TYPE.equals(sourceDocument.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stackCommands(CoreSession session, DocumentModel taskDoc, String eventId) {
        DocumentModel pi = ProcedureDenormalizationHelper.getInstance().getProcedureInstanceOfTask(session, taskDoc);
        if(pi != null){
            super.esListener.stackCommand(pi, eventId, true);
        }
    }

}
