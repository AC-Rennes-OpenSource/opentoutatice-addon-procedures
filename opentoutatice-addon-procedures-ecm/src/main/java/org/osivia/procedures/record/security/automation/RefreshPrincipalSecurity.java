/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 * Contributors:
 *   mberhaut1
 *    
 */
package org.osivia.procedures.record.security.automation;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.osivia.procedures.record.security.rules.SecurityRulesBuilder;

import fr.toutatice.ecm.platform.automation.RefreshPrincipal;

@Operation(id = RefreshPrincipalSecurity.ID)
public class RefreshPrincipalSecurity extends RefreshPrincipal {

	private static final Log log = LogFactory.getLog(RefreshPrincipalSecurity.class);

	@Context
	protected OperationContext ctx;

	@Context
	protected CoreSession session;

	@OperationMethod
	public void run() throws Exception {
		initInheritedContexts();

		// Refresh ACLs
		super.run();

		// Refresh (business) Security
		Principal principal = this.ctx.getPrincipal();
		if (principal != null) {
			if (log.isDebugEnabled()) {
				log.debug("[Refreshing Security] for: " + principal.getName());
			}

			SecurityRulesBuilder rulesBuilder = SecurityRulesBuilder.getInstance();

			rulesBuilder.invalidateRulesOf(principal.getName());
			rulesBuilder.buildSecurityRelations(this.session, principal);
		}
	}

	protected void initInheritedContexts() {
		super.ctx = this.ctx;
	}

}
