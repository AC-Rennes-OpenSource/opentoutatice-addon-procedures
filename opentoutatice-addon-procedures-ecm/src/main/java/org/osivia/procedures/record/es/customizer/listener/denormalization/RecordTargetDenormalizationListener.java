/**
 * 
 */
package org.osivia.procedures.record.es.customizer.listener.denormalization;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.model.relation.RelationsModelResolver;

import fr.toutatice.ecm.es.customizer.listeners.denormalization.AbstractDenormalizationESListener;

/**
 * @author david
 *
 */
public class RecordTargetDenormalizationListener extends AbstractDenormalizationESListener {

	@Override
	protected boolean needToReIndex(DocumentModel sourceDocument, String eventId) {
		boolean needs = false;
		
		if(DocumentEventTypes.BEFORE_DOC_UPDATE.equals(eventId)) {
			needs = sourceDocument != null && StringUtils.equals(ProceduresConstants.RECORD_TYPE, sourceDocument.getType());
		}
		
		return needs;
	}

	@Override
	protected void stackCommands(CoreSession session, DocumentModel sourceDocument, String eventId) {
		// Get Record sources pointing to given record if any
		DocumentModelList sources = RelationsModelResolver.getInstance().getSourcesOfRelationWithTarget(sourceDocument);
		if(sources != null) {
			for(DocumentModel source : sources) {
				super.getEsInlineListener().stackCommand(source, eventId, true);
			}
		}
	}

}
