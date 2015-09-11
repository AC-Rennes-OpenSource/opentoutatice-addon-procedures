/**
 * 
 */

package org.osivia.procedures.instances.operations;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.task.TaskService;
import org.osivia.procedures.models.services.ProcedureService;

/**
 * @author david
 */
@Operation(id = UpdateProcedureInstance.ID, category = Constants.CAT_SERVICES, label = "UpdateProcedureInstance", description = "")
public class UpdateProcedureInstance {

	public static final String ID = "Services.UpdateProcedureInstance";

	@Context
	protected CoreSession session;

	@Context
	protected ProcedureService procedureService;

	@Context
	protected TaskService taskService;
	
	

	@OperationMethod
	public DocumentModelList run(DocumentModelList inputs) throws Exception {
		DocumentModel procedureInstance = inputs.get(0);
		String procedureName = (String) procedureInstance.getPropertyValue("pi:name");
		DocumentModel procedure = procedureService.getProcedure(session, procedureName);

		String nextStep = (String) procedureInstance
				.getPropertyValue("pi:nextStep");
		DocumentModel step = procedureService.getStep(session, procedure, nextStep);

		if (step != null) {
			procedureInstance = procedureService.updateProcedureInstance(session, procedureName,
					procedureInstance, step, false);
			
		} else {
			// End Step
			procedureInstance = procedureService.finalizeProcedureInstance(session, procedureInstance);
		}
		
		inputs.set(0, procedureInstance);

		return inputs;
	}

}
