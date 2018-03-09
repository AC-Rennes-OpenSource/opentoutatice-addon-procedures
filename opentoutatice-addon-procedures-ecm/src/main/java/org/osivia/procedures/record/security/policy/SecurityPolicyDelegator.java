/**
 * 
 */
package org.osivia.procedures.record.security.policy;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.model.Document;
import org.osivia.procedures.record.RecordsConstants;
import org.osivia.procedures.record.security.rules.SecurityRulesBuilder;
import org.osivia.procedures.record.security.rules.model.SecurityRelations;

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

		String sessionId = doc.getSession().getSessionId();
		CoreSession coreSession = CoreInstance.getInstance().getSession(sessionId);
		if (coreSession != null) {
			SecurityRelations rules = SecurityRulesBuilder.buildSecurityRelations(coreSession, principal);
			String recordType = (String) doc.getPropertyValue("rcd:procedureModelWebId");

			if (rules.getTypes().contains(recordType)) {
				access = Access.GRANT;
			}
		} else {
			throw new DocumentException("No CoreSession bound to document: " + doc.getPath());
		}

		return access;

	}

}
