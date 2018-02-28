/**
 * 
 */
package org.osivia.procedures.record.security.query;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.nuxeo.ecm.core.api.CoreSession;
import org.osivia.procedures.record.security.rules.SecurityRulesBuilder;
import org.osivia.procedures.record.security.rules.model.SecurityRules;
import org.osivia.procedures.record.security.rules.model.relation.Relation;

/**
 * @author david
 *
 */
public class SecurityClauseBuilder {

	private static final Log log = LogFactory.getLog(SecurityClauseBuilder.class);

	private static SecurityClauseBuilder instance;
	private static SecurityRulesBuilder rulesManager;

	private SecurityClauseBuilder() {
		super();
	}

	public static synchronized SecurityClauseBuilder getInstance() {
		if (instance == null) {
			instance = new SecurityClauseBuilder();
		}
		return instance;
	}

	public static SecurityClauseBuilder getSecurityRulesManager() {
		if (rulesManager == null) {
			rulesManager = SecurityRulesBuilder.getInstance();
		}
		return instance;
	}

	public FilterBuilder getFilter(CoreSession session) {
		// Result
		FilterBuilder filterBuilder = null;
		
		SecurityRules rules = (SecurityRules) SecurityRulesBuilder.getInstance().build(session.getRepositoryName(),
				session.getPrincipal());
		List<FilterBuilder> filters = build(session, rules);
		// There is Records security
		if(filters.size() > 0) {
			filterBuilder = FilterBuilders.orFilter(filters.toArray(new FilterBuilder[0]));
		}

		return filterBuilder;
	}

	public List<FilterBuilder> build(CoreSession session, SecurityRules rules) {
		List<FilterBuilder> filters = new LinkedList<>();

		if (rules.getRelationsRules() != null) {
			for (Relation relation : rules.getRelationsRules().getRules()) {

				switch (relation.getType()) {
				case NtoOne:
					List<String> ids = (ArrayList<String>) relation.getTargetIds();
					for (String id : ids) {
						AndFilterBuilder oneToOneFilter = FilterBuilders.andFilter(
								FilterBuilders.termFilter("rcd:type", relation.getSourceType()),
								FilterBuilders.termFilter("rcd:data." + relation.getTargetKey(), id));

						filters.add(oneToOneFilter);
					}
					break;

				case NtoN:
					AndFilterBuilder oneToNFilter = FilterBuilders.andFilter(
							FilterBuilders.termFilter("rcd:type", relation.getSourceType()),
							FilterBuilders.inFilter(relation.getTargetKey(), getIds(relation.getTargetIds())));

					filters.add(oneToNFilter);

					break;

				default:
					break;
				}
			}
		}

		return filters;
	}

	private String[] getIds(Object value) {
		if (value instanceof ArrayList<?>) {
			ArrayList<String> values = (ArrayList<String>) value;
			return values.toArray(new String[0]);
		}
		return null;
	}

}
