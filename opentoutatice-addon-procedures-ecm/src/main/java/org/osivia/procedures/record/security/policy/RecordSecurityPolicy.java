/**
 * 
 */
package org.osivia.procedures.record.security.policy;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.query.sql.model.SQLQuery;
import org.nuxeo.ecm.core.security.AbstractSecurityPolicy;
import org.osivia.procedures.constants.ProceduresConstants;

/**
 * @author david
 *
 */
public class RecordSecurityPolicy extends AbstractSecurityPolicy {

	private static final Log log = LogFactory.getLog(RecordSecurityPolicy.class);
	

	@Override
	public Access checkPermission(Document doc, ACP mergedAcp, Principal principal, String permission,
			String[] resolvedPermissions, String[] additionalPrincipals) {
		// result
		Access access = Access.UNKNOWN;

		// Record type filter
		if (doc != null && doc.getType() != null
				&& StringUtils.equals(ProceduresConstants.RECORD_TYPE, doc.getType().getName())) {
			// Only Read permission (for the moment?)
			if (StringUtils.equals(SecurityConstants.READ, permission)) {
				// Question is: is current document linked to one of current security records?

				CoreSession session = null;
				try {
					String repositoryName = ProceduresConstants.DEFAULT_REPOSITORY_NAME;
					if (doc.getSession() != null) {
						repositoryName = doc.getSession().getRepositoryName();
					}

					try {
						access = SecurityPolicyDelegator.check(repositoryName, principal, doc, access);
					} catch (DocumentException e) {
						access = Access.UNKNOWN;
					}
				} finally {
					if (session != null) {
						session.close();
					}
				}

			}
		}

		return access;
	}

	@Override
	public boolean isRestrictingPermission(String permission) {
		assert permission.equals("Browse"); // others not coded
		return false;
	}

	@Override
	public boolean isExpressibleInQuery() {
		return true;
	}

	@Override
	public SQLQuery.Transformer getQueryTransformer() {
		return SQLQuery.Transformer.IDENTITY;
	}

}
