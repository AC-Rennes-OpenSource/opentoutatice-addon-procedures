/**
 * 
 */

package org.osivia.procedures.models.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.routing.core.api.DocumentRoutingEngineService;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.utils.UsersHelper;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;

/**
 * @author david
 */
public class ProcedureService extends DefaultComponent {
	
	/**
	 * @return first step of procedure.
	 */
	public DocumentModel getProcedure(CoreSession session, String procedureName) {
		StringBuilder query = new StringBuilder();
		query.append("select * from Procedure where ecm:path startswith '");
		query.append(ProceduresConstants.TEST_PROCEDURE_PATH);
		query.append("' and dc:title='");
		query.append(procedureName);
		query.append("'");

		DocumentModelList steps = session.query(query.toString());

		if (CollectionUtils.isNotEmpty(steps)) {
			return steps.get(0);
		} else {
			return null;
		}

	}

	/**
	 * @return first step of procedure.
	 */
	public DocumentModel getFirstStep(CoreSession session, DocumentModel procedureModel) {
		StringBuilder query = new StringBuilder();
		query.append("select * from Step where ecm:path startswith '");
		query.append(procedureModel.getPathAsString());
		query.append("' and step:firstStep = 1");

		DocumentModelList steps = session.query(query.toString());

		if (CollectionUtils.isNotEmpty(steps)) {
			return steps.get(0);
		} else {
			return null;
		}
	}

	/**
	 * @param session
	 * @param titleStep
	 * @return step with given title.
	 */
	public DocumentModel getStep(CoreSession session, DocumentModel procedureModel, String titleStep) {
		StringBuilder query = new StringBuilder();
		query.append("select * from Step where ecm:path startswith '");
		query.append(procedureModel.getPathAsString());
		query.append("' and dc:title = '");
		query.append(titleStep);
		query.append("'");

		DocumentModelList steps = session.query(query.toString());

		if (CollectionUtils.isNotEmpty(steps)) {
			return steps.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Updates procedure instance with firstStep.
	 */
	public DocumentModel updateProcedureInstance(CoreSession session, String procedureName, DocumentModel procedureInstance, DocumentModel step, boolean fisrtStep) {
		String stepTitle = (String) step.getPropertyValue("dc:title");

		if (procedureInstance != null) {
			procedureInstance.setPropertyValue("pi:name", procedureName);
			procedureInstance.setPropertyValue("pi:currentStep", stepTitle);
			
			String directive = (String) step.getPropertyValue("step:directive");
			procedureInstance.setPropertyValue("pi:directive", directive);
			
			String message = (String) step.getPropertyValue("step:message");
			procedureInstance.setPropertyValue("pi:message", message);

			String nextStep = (String) step.getPropertyValue("step:nextStep");
			procedureInstance.setPropertyValue("pi:nextStep", nextStep);
			
			String formId = (String) step.getPropertyValue("step:form");
			procedureInstance.setPropertyValue("pi:formId", formId);
			
			String action = (String) step.getPropertyValue("step:action");
			procedureInstance.setPropertyValue("pi:form/action", action);
			
			procedureInstance.setPropertyValue("pi:currentStepPath", step.getPathAsString());
			
			if(fisrtStep){
				DocumentModel procedure = getProcedure(session, procedureName);
				String[] currentActors = (String[]) procedure.getPropertyValue("pcd:authorizedActors");
				procedureInstance.setPropertyValue("pi:currentActors", currentActors);
			} else {
				Boolean isInitiator = (Boolean) step.getPropertyValue("step:initiator");
				if(BooleanUtils.isTrue(isInitiator)){
					
					String initiatorName = UsersHelper.getInitiator(procedureInstance);
					String initiatorFullName = UsersHelper.getUsername(initiatorName);
					String[] initiator = new String[1];
					initiator[0] = initiatorName;
					procedureInstance.setPropertyValue("pi:currentActors", initiator);
					procedureInstance.setPropertyValue("pi:initiator", initiatorFullName);
					
				} else {
					
					String[] groups = (String[]) step.getPropertyValue("step:groups");
					if(groups != null && groups.length > 0){
						String[] actors = UsersHelper.getUsersOfGroup(groups);
						procedureInstance.setPropertyValue("pi:currentActors", actors);
						
					}
				}
			} 

			procedureInstance = session.saveDocument(procedureInstance);
		}
		
		return procedureInstance;
	}
	
	/**
	 * Updates generic task.
	 */
	public void updateGenericTask(CoreSession session, DocumentModel step, Task genericTask){
		String directive = (String) step.getPropertyValue("step:directive");
		genericTask.setName(directive);
		ToutaticeDocumentHelper.saveDocumentSilently(session, genericTask.getDocument(), true);
	}
	
	/**
	 * 
	 * @return procedure instance to finalize procedure.
	 */
	public DocumentModel finalizeProcedureInstance(CoreSession session, DocumentModel procedureInstance){
		procedureInstance.setPropertyValue("pi:currentStep", "endStep");
		return session.saveDocument(procedureInstance);
	}

	/**
	 * @param session
	 * @return ProcedureInstance document.
	 */
	public DocumentModel getProcedureInstance(CoreSession session, String procedureName) {
		
		StringBuilder query = new StringBuilder();
		query.append("select * from ProcedureInstance where ecm:path startswith '");
		query.append(ProceduresConstants.PI_CONTAINER_PATH);
		query.append("' and pi:name='");
		query.append(procedureName);
		query.append("'");

		DocumentModelList pis = session.query(query.toString());

		if (CollectionUtils.isNotEmpty(pis)) {
			return pis.get(0);
		} else {
			return null;
		}
		
	}
	
	/**
	 * @param session
	 * @return list of ProcedureInstance documents.
	 */
	public DocumentModelList getProcedureInstances(CoreSession session, String procedureName) {
		
		StringBuilder query = new StringBuilder();
		query.append("select * from ProcedureInstance where ecm:path startswith '");
		query.append(ProceduresConstants.PI_CONTAINER_PATH);
		query.append("' and pi:name='");
		query.append(procedureName);
		query.append("'");

		return session.query(query.toString());
		
	}

	/**
	 * @return a procedure instance.
	 */
	private DocumentModel createProcedureInstance(CoreSession session, String procedureName) {
		DocumentModel piModel = session.createDocumentModel(
				ProceduresConstants.PI_CONTAINER_PATH, procedureName,
				ProceduresConstants.PI_TYPE);
		DocumentModel pi = session.createDocument(piModel);
		return session.saveDocument(pi);
	}

	/**
	 * Starts a procedure.
	 */
	public void startProcedure(CoreSession session, DocumentModel genericModel, String procedureName) {
		DocumentModel procedureInstance = createProcedureInstance(session, procedureName);
		
		DocumentModel procedure = getProcedure(session, procedureName);
		DocumentModel firstStep = getFirstStep(session, procedure);
		procedureInstance = updateProcedureInstance(session, procedureName, procedureInstance, firstStep, true);

		List<String> currentDocIds = new ArrayList<String>(1);
		currentDocIds.add(procedureInstance.getId());
		
		String processId = getDocumentRoutingService().createNewInstance(genericModel.getName(),
				currentDocIds, session, true);
		
		List<Task> allTaskInstances = getTaskService().getAllTaskInstances(processId, session);
		if(CollectionUtils.isNotEmpty(allTaskInstances)){
			updateGenericTask(session, firstStep, allTaskInstances.get(0));
		}

	}
	
	/**
	 * Cancel a procedure.
	 */
	public void stopProcedure(CoreSession session, DocumentModel currentProcedure) {
		DocumentModel pi = getProcedureInstance(session, currentProcedure.getTitle());

		DocumentRoute inputWorkflowRoute = ToutaticeWorkflowHelper
				.getWorkflowByName(ProceduresConstants.GENERICMODEL_ID, pi);
		if (inputWorkflowRoute != null) {
			DocumentRoutingEngineService engineRoutingService = (DocumentRoutingEngineService) Framework
					.getService(DocumentRoutingEngineService.class);
			engineRoutingService.cancel(inputWorkflowRoute, session);
		}

		session.removeDocument(pi.getRef());
	}

	/**
	 * @return TaskService.
	 */
	protected TaskService getTaskService() {
		return (TaskService) Framework.getService(TaskService.class);
	}

	/**
	 * @return DocumentRoutingService
	 */
	private DocumentRoutingService getDocumentRoutingService() {
		return (DocumentRoutingService) Framework
				.getService(DocumentRoutingService.class);
	}

}
