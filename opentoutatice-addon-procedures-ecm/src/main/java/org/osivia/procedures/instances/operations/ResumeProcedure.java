/**
 * 
 */
package org.osivia.procedures.instances.operations;

import javax.ws.rs.core.Context;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.osivia.procedures.instances.operation.runner.UnrestrictedResumeProcedure;


/**
 * @author david
 *
 */
@Operation(id = ResumeProcedure.ID, category = Constants.CAT_SERVICES, label = "ResumeProcedure", description = "Resumes a procedure.")
public class ResumeProcedure {
    
    /** Operation's id. */
    public final static String ID = "Services.ResumeProcedure";

    /** Core Session. */
    @Context
    private CoreSession session;
    /** Routing service. */
    @Context
    private DocumentRoutingService routingService;
    
    /** Properties of ProcedureInstance to update. */
    @Param(name = "properties", required = false)
    private Properties properties;
    /** Actors of current step. */
    @Param(name = "actors", required = true)
    private StringList actors;
    
    /**
     * Resumes a procedure.
     * 
     * @throws ClientException
     */
    @OperationMethod
    public void run(DocumentModel procedureInstance) throws ClientException {
        UnrestrictedResumeProcedure resumer = new UnrestrictedResumeProcedure(session, routingService, procedureInstance, properties, actors);
        resumer.runUnrestricted();
    }

}
