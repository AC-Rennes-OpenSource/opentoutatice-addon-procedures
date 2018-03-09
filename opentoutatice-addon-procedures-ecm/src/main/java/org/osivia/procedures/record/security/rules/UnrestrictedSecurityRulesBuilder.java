/**
 * 
 */
package org.osivia.procedures.record.security.rules;

import java.security.Principal;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.osivia.procedures.record.security.rules.model.SecurityRelations;

/**
 * @author david
 *
 */
public class UnrestrictedSecurityRulesBuilder extends UnrestrictedSessionRunner {
	
	private Principal currentPrincipal;
	private SecurityRelations securityRelations;

	protected UnrestrictedSecurityRulesBuilder(CoreSession session, Principal currentPrincipal) {
		super(session);
		this.currentPrincipal = currentPrincipal;
	}

	@Override
	public void run() throws ClientException {
		this.securityRelations = SecurityRulesBuilder.getInstance().build(super.session, this.currentPrincipal);
	}

	public SecurityRelations getSecurityRelations() {
		return securityRelations;
	}

}
