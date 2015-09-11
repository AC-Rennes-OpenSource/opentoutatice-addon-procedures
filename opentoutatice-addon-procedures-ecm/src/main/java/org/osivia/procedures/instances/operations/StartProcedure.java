/**
 * 
 */

package org.osivia.procedures.instances.operations;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.models.services.ProcedureService;

/**
 * @author david
 */
@Operation(id = StartProcedure.ID, category = Constants.CAT_SERVICES, label = "StartProcedure", description = "Starts a procedure.")
public class StartProcedure {

	public static final String ID = "Services.StartProcedure";

	@Param(name = "name", required = true)
	protected String name;

	@Context
	CoreSession session;

	@Context
	DocumentRoutingService documentRoutingService;

	@Context
	ProcedureService procedureService;

	@OperationMethod
	public DocumentModel run() throws Exception {
		String id = documentRoutingService.getRouteModelDocIdWithId(session,
				ProceduresConstants.GENERICMODEL_ID);
		DocumentModel genericModel = session.getDocument(new IdRef(id));

		procedureService.startProcedure(session, genericModel, name);
		
		return procedureService.getProcedureInstance(session, name);
	}

}
