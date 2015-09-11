/**
 * 
 */

package org.osivia.procedures.instances.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;
import org.osivia.procedures.models.services.ProcedureService;

/**
 * @author david
 */
@Operation(id = GoToNextStep.ID, category = Constants.CAT_SERVICES, label = "GoToNextStep", description = "Allows to go to next procedure's step.")
public class GoToNextStep {

	public static final String ID = "Services.GoToNextStep";
	
	@Context
	protected OperationContext context;

	@Context
	protected CoreSession session;

	@Context
	protected ProcedureService procedureService;

	@Context
	protected DocumentRoutingService documentRoutingService;
	
	@Context
	protected AutomationService automationService;

	@Context
	protected TaskService taskService;

	@OperationMethod
	public DocumentModel run(DocumentModel procedureInstance) throws Exception {

		// End current Task
		NuxeoPrincipal principal = (NuxeoPrincipal) session.getPrincipal();
		List<String> actors = new ArrayList<String>(1);
		actors.add(principal.getName());

		List<Task> currentTaskInstances = taskService.getTaskInstances(procedureInstance,
				actors, session);
		if (CollectionUtils.isNotEmpty(currentTaskInstances)) {

			Task currentTask = currentTaskInstances.get(0);
			String action = (String) procedureInstance.getPropertyValue("pi:form/action");

			documentRoutingService.endTask(session, currentTask, new HashMap<String, Object>(), StringUtils.EMPTY);
			
			// Execute action (defined before end task)
			if(StringUtils.isNotBlank(action)){
				executeAction(context, action, procedureInstance);
			}
				
			// Get updated procedureInstance (in operation on endtask)
			DocumentModel updatedProcedureInst = session.getDocument(procedureInstance.getRef());
			// Update generic task
			String[] nextActors = (String[]) updatedProcedureInst.getPropertyValue("pi:currentActors");
			List<Task> taskInstances = taskService.getTaskInstances(procedureInstance, Arrays.asList(nextActors), session);
			if(CollectionUtils.isNotEmpty(taskInstances)){
					
				String procedureName = (String) procedureInstance.getPropertyValue("pi:name");
				DocumentModel procedureModel = procedureService.getProcedure(session, procedureName);
				String titleStep = (String) updatedProcedureInst.getPropertyValue("pi:currentStep");
				DocumentModel step = procedureService.getStep(session, procedureModel, titleStep);
					
				procedureService.updateGenericTask(session, step, taskInstances.get(0));
			}

		}

		return procedureInstance;
	}
	
	/**
	 * Excecute an action (operation for the moment).
	 * @param action
	 */
	protected void executeAction(OperationContext context, String action, DocumentModel pi) throws Exception {
		context.setInput(pi);
		automationService.run(context, action);
	}

}
