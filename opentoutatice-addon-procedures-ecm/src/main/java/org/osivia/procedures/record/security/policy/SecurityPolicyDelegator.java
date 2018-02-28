/**
 * 
 */
package org.osivia.procedures.record.security.policy;

import java.io.Serializable;
import java.security.Principal;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.model.Document;
import org.osivia.procedures.record.RecordsConstants;
import org.osivia.procedures.record.security.rules.SecurityRulesBuilder;
import org.osivia.procedures.record.security.rules.model.SecurityRules;

import fr.toutatice.ecm.platform.core.query.helper.ToutaticeEsQueryHelper;

/**
 * @author david
 *
 */
public class SecurityPolicyDelegator {

	private static final Log log = LogFactory.getLog(SecurityPolicyDelegator.class);

	public static final String RECORD_TYPE_QUERY = "select rcd:type from Record where ecm:uuid = '%s' "
			+ RecordsConstants.DEFAULT_FILTER;

	private SecurityPolicyDelegator() {
		super();
	}

	public static Access check(String repositoryName, Principal principal, Document doc, Access access)
			throws DocumentException {
		CoreSession session = null;

		try {
			// Can't acces Record type here (rcd:data/type)
			session = CoreInstance.openCoreSessionSystem(repositoryName);

			// Debug
			final long begin = System.currentTimeMillis();

			// Record type
			IterableQueryResult rows = ToutaticeEsQueryHelper.unrestrictedQueryAndAggregate(session,
					String.format(RECORD_TYPE_QUERY, doc.getUUID()), false);

			if (rows != null) {
				Iterator<Map<String, Serializable>> iterator = rows.iterator();
				if (iterator.hasNext()) {
					Map<String, Serializable> row = iterator.next();

					String recordType = (String) row.get("rcd:type");

					SecurityRules rules = (SecurityRules) SecurityRulesBuilder.getInstance().build(repositoryName,
							principal);

					if (rules.getModelsRules().getTypes().contains(recordType)) {
						access = Access.GRANT;
					}
				}
			}

			if (log.isDebugEnabled()) {
				final long end = System.currentTimeMillis();
				log.debug("[#check] " + String.valueOf(end - begin) + " ms");
			}

		} finally {
			if (session != null) {
				session.close();
			}
		}

		return access;

	}

}
