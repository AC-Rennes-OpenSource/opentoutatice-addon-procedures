/**
 * 
 */

package org.osivia.procedures.instances.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.routing.web.RoutingTaskActionsBean;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.runtime.api.Framework;
import org.osivia.procedures.constants.ExtendedSeamPrecedence;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.models.services.ProcedureService;
import org.osivia.procedures.utils.UsersHelper;

import fr.toutatice.ecm.platform.web.workflows.ToutaticeDocumentRoutingActionsBean;

/**
 * Manage Procedures instances.
 */
@Name("routingActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.PROCEDURE)
public class ProcedureActionsBean extends ToutaticeDocumentRoutingActionsBean {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(ProcedureActionsBean.class);
    
    private static final String VIEW = "view_documents";

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected NavigationContext navigationContext;

    protected transient ProcedureService procedureService;
    
	/**
	 * Gets generic model.
	 * 
	 * @return generic model.
	 * @throws ClientException
	 */
	private DocumentModel getGenericModel() throws ClientException {
		String id = getDocumentRoutingService().getRouteModelDocIdWithId(
				this.documentManager, ProceduresConstants.GENERICMODEL_ID);
		return getRouteModel(id);
	}
    
    /**
     * Starts a procedure.
     */
    public String startProcedure(){
    	DocumentModel genericModel = getGenericModel();
    	DocumentModel currentProcedure = navigationContext.getCurrentDocument();
    	String procedureName = (String) currentProcedure.getPropertyValue("dc:title");
    	getProcedureService().startProcedure(this.documentManager, genericModel, procedureName);
    	return VIEW;
    }
    
    /**
     * Cancel procedure. 
     */
    public String cancelProcedure(){
    	DocumentModel currentProcedure = navigationContext.getCurrentDocument();
    	getProcedureService().stopProcedure(this.documentManager, currentProcedure);
    	return VIEW;
    }
    
    @Observer(value = {EventNames.NEW_DOCUMENT_CREATED, EventNames.BEFORE_DOCUMENT_CHANGED})
    public void updateActors(){
    	DocumentModel document = navigationContext.getChangeableDocument();
    	if(document == null){
    		document = navigationContext.getCurrentDocument();
    	}
    	
    	if(ProceduresConstants.PROCEDURE_TYPE.equals(document.getType())){
    		String[] groups = (String[]) document.getPropertyValue("pcd:authorizedGroups");
    		if(groups != null && groups.length > 0){
    			String[] usersOfGroup = UsersHelper.getUsersOfGroup(groups);
    			if(usersOfGroup != null && usersOfGroup.length > 0){
    				document.setPropertyValue("pcd:authorizedActors", usersOfGroup);
    			}
    		}
    	}
    }
    
    /**
     * @return RoutingTaskActionsBean 
     */
    public ProcedureService getProcedureService(){
    	if(procedureService == null){
    		procedureService = Framework.getService(ProcedureService.class);
    	}
    	return procedureService;
    }
}
