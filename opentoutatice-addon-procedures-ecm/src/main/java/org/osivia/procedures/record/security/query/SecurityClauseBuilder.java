/**
 * 
 */
package org.osivia.procedures.record.security.query;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.nuxeo.ecm.core.api.CoreSession;
import org.osivia.procedures.record.security.rules.SecurityRulesBuilder;
import org.osivia.procedures.record.security.rules.model.SecurityRelation;
import org.osivia.procedures.record.security.rules.model.SecurityRelationType;
import org.osivia.procedures.record.security.rules.model.SecurityRelations;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

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

		SecurityRelations rules = SecurityRulesBuilder.buildSecurityRelations(session, session.getPrincipal());
		List<FilterBuilder> filters = build(session, rules);
		// There is Records security
		if (filters.size() > 0) {
			filterBuilder = FilterBuilders.orFilter(filters.toArray(new FilterBuilder[0]));
		}

		return filterBuilder;
	}

	public List<FilterBuilder> build(CoreSession session, SecurityRelations rules) {
		List<FilterBuilder> filters = new LinkedList<>();

		if (rules != null) {

			for (SecurityRelation relation : rules) {

				switch (relation.getType()) {
				case NtoOne:
					List<String> ids = (ArrayList<String>) relation.getRecordsIds();
					if (CollectionUtils.isNotEmpty(ids)) {
						for (String id : ids) {
							AndFilterBuilder oneToOneFilter = FilterBuilders.andFilter(
									FilterBuilders.termFilter("rcd:type", relation.getRecordType()),
									FilterBuilders.termFilter(
											"rcd:data." + adapt(SecurityRelationType.NtoOne, relation.getRelationKey()),
											id));

							filters.add(oneToOneFilter);
						}
					}
					break;

				case NtoN:
					if (CollectionUtils.isNotEmpty(relation.getRecordsIds())) {
						AndFilterBuilder oneToNFilter = FilterBuilders.andFilter(
								FilterBuilders.termFilter("rcd:type", relation.getRecordType()),
								FilterBuilders.inFilter(adapt(SecurityRelationType.NtoN, relation.getRelationKey()),
										getIds(relation.getRecordsIds())));

						filters.add(oneToNFilter);
					}
					break;

				default:
					break;
				}
			}
		}

		return filters;
	}

	protected String[] getIds(Object value) {
		if (value instanceof ArrayList<?>) {
			ArrayList<String> values = (ArrayList<String>) value;
			return values.toArray(new String[0]);
		}
		return null;
	}

	protected String adapt(SecurityRelationType relationType, String relationKey) {
		String key = relationKey;

		switch (relationType) {
		case NtoOne:
			key += "." + ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID;
			break;

		case NtoN:
			key = ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID;
			break;

		default:
			break;
		}

		return key;
	}

}
