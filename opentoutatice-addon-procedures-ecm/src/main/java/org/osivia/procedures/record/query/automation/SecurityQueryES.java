package org.osivia.procedures.record.query.automation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.elasticsearch.api.ElasticSearchAdmin;
import org.nuxeo.elasticsearch.api.ElasticSearchService;

import fr.toutatice.ecm.elasticsearch.automation.QueryES;
import fr.toutatice.ecm.elasticsearch.query.TTCNxQueryBuilder;

@Operation(id = SecurityQueryES.ID)
public class SecurityQueryES extends QueryES {

	private static final Log log = LogFactory.getLog(SecurityQueryES.class);
	
	@Context
	protected CoreSession session;
	
	@Context
	protected OperationContext ctx;

    @Context
    protected ElasticSearchService elasticSearchService;

	@Context
	protected ElasticSearchAdmin elasticSearchAdmin;

	@Context
	protected SchemaManager schemaManager;

	@Param(name = "query", required = true)
	protected String query;

	@Param(name = "queryLanguage", required = false, description = "Language of the query parameter : NXQL or ES.", values = { "NXQL" })
	protected String queryLanguage = QueryLanguage.NXQL.name();

	@Param(name = "pageSize", required = false)
	protected Integer pageSize;

	@Param(name = "currentPageIndex", required = false)
    protected Integer currentPageIndex;

    @Deprecated
    @Param(name = "page", required = false)
    // For Document.PageProvider only: to remove later
    protected Integer page;

	@Param(name = "X-NXDocumentProperties", required = false)
	protected String nxProperties;


	@Override
	protected TTCNxQueryBuilder getNxQueryBuilder() {
		initInheritedContext();
		initInheritedParameters();
		
		return new SecurityNxQueryBuilder(this.session);
	}


	protected void initInheritedContext() {
		super.session = this.session;
		super.ctx = this.ctx;
		super.elasticSearchService = this.elasticSearchService;
		super.elasticSearchAdmin = this.elasticSearchAdmin;
		super.schemaManager = this.schemaManager;
	}

	protected void initInheritedParameters() {
		super.query = this.query;
		super.queryLanguage = this.queryLanguage;
		super.pageSize = this.pageSize;
		super.currentPageIndex = this.currentPageIndex;
		super.page = this.page;
		super.nxProperties = this.nxProperties;
	}
	
}
