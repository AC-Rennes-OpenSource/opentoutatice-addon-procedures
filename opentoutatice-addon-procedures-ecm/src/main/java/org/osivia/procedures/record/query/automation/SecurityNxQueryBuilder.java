package org.osivia.procedures.record.query.automation;

import static org.nuxeo.ecm.core.api.security.SecurityConstants.UNSUPPORTED_ACL;
import static org.nuxeo.elasticsearch.ElasticSearchConstants.ACL_FIELD;

import java.security.Principal;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.internal.InternalSearchResponse;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.SortInfo;
import org.nuxeo.ecm.core.security.SecurityService;
import org.nuxeo.elasticsearch.fetcher.Fetcher;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.osivia.procedures.record.security.query.SecurityClauseBuilder;

import fr.toutatice.ecm.elasticsearch.fetcher.TTCEsFetcher;
import fr.toutatice.ecm.elasticsearch.query.TTCNxQueryBuilder;

public class SecurityNxQueryBuilder extends TTCNxQueryBuilder {
	
	public SecurityNxQueryBuilder(CoreSession session) {
		super(session);
		super.session = session;
	}

	private static final Log log = LogFactory.getLog(SecurityNxQueryBuilder.class);

	@Override
	protected QueryBuilder addSecurityFilter(QueryBuilder query) {
		long begin = System.currentTimeMillis();
		
		QueryBuilder securedQuery = query;

		AndFilterBuilder aclFilter = getDocumentACLFilter(query);
		// No principal or principal is Admin
		if (aclFilter != null) {
			FilterBuilder recordsSecureFilter = SecurityClauseBuilder.getInstance().getFilter(super.session);
			// There is Records security
			if (recordsSecureFilter != null) {
				OrFilterBuilder aclNRecordsSecureFilter = FilterBuilders.orFilter(recordsSecureFilter, aclFilter);
				securedQuery = QueryBuilders.filteredQuery(query, aclNRecordsSecureFilter);
			} else {
				securedQuery = QueryBuilders.filteredQuery(query, aclFilter);
			}
		}
		
		if(log.isDebugEnabled()) {
			long end =System.currentTimeMillis();
			log.debug("[#addSecurityFilter] " + String.valueOf(end - begin) + " ms");
		}

		return securedQuery;
	}

	/**
	 * @param query
	 * @return
	 */
	protected AndFilterBuilder getDocumentACLFilter(QueryBuilder query) {
		AndFilterBuilder aclFilter;
		Principal principal = super.session.getPrincipal();
		if (principal == null
				|| (principal instanceof NuxeoPrincipal && ((NuxeoPrincipal) principal).isAdministrator())) {
			return null;
		}
		String[] principals = SecurityService.getPrincipalsToCheck(principal);
		// we want an ACL that match principals but we discard
		// unsupported ACE that contains negative ACE
		aclFilter = FilterBuilders.andFilter(FilterBuilders.inFilter(ACL_FIELD, principals),
				FilterBuilders.notFilter(FilterBuilders.inFilter(ACL_FIELD, UNSUPPORTED_ACL)));
		return aclFilter;
	}

}
