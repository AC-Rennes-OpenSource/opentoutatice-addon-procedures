/**
 * 
 */

package org.osivia.procedures.instances.operations.library;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.filemanager.api.FileManager;

/**
 * @author david
 */
@Operation(id=UploadFileWithMetadata.ID, category=Constants.CAT_SERVICES, label="UploadFileFromPI", description="Upload file (blob) with metadata in given path according to procedure instance informations.")
public class UploadFileWithMetadata {

    public static final String ID = "Services.UploadFileFromPI";
    
    @Context
    CoreSession session;
    
    @Context
    protected FileManager fileManager;
    
    @Param(name = "path" , required = false)
    protected String path;
    
    @Param(name = "metadataXPath", required = false)
    protected StringList metadataXPath;

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentModel procedureInstance) throws Exception {
    	// Creation
    	// FIXME
    	path = "/cns/workspaces/conventions";
    	Blob blob = (Blob) procedureInstance.getPropertyValue("pi:form/document/content");
    	String fileName = (String) procedureInstance.getPropertyValue("pi:form/document/fileName");
    	
    	DocumentModel file = fileManager.createDocumentFromBlob(session, blob, path, true, fileName);
    	
    	// Set Metadata
//    	for(String md : metadataXPath){
//    		Serializable propertyValue = procedureInstance.getPropertyValue(md);
//    		file.setPropertyValue(md, propertyValue);
//    	}
    	String nature = (String) procedureInstance.getPropertyValue("pi:form/nature");
    	file.setPropertyValue("dc:nature", nature);
    	
    	session.saveDocument(file);
    	
      return file; 
    }    

}
