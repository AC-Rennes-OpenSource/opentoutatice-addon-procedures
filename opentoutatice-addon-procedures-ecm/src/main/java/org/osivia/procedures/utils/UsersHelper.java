/**
 *
 */
package org.osivia.procedures.utils;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingConstants;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;
import org.osivia.procedures.constants.ProceduresConstants;

import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;

/**
 * @author david
 *
 */
public class UsersHelper {

	private static UserManager userManager = Framework
			.getService(UserManager.class);

	/**
	 * Utility class.
	 */
	private UsersHelper() {
	};

	/**
	 * @return users of given group.
	 */
	public static String[] getUsersOfGroup(String[] names) {
		String[] users = new String[0];

		if ((names != null) && (names.length > 0)) {

			for (String name : names) {
				NuxeoGroup group = userManager.getGroup(name);
				if (group != null) {
					List<String> memberUsers = group.getMemberUsers();
					if (CollectionUtils.isNotEmpty(memberUsers)) {
						users = (String[]) ArrayUtils.addAll(users, memberUsers
								.toArray(new String[memberUsers.size()]));
					}
				}
			}
		}

		return users;
	}

    /**
     * @return users of given group.
     */
    public static String[] getUsersOfGroup(StringList names) {
        String[] users = new String[0];

        for (String name : names) {
            NuxeoGroup group = userManager.getGroup(name);
            if (group != null) {
                List<String> memberUsers = group.getMemberUsers();
                if (CollectionUtils.isNotEmpty(memberUsers)) {
                    users = (String[]) ArrayUtils.addAll(users, memberUsers.toArray(new String[memberUsers.size()]));
                }
            }
        }

        return users;
    }

	/**
	 * @return the procedure's initiator.
	 */
	public static String getInitiator(DocumentModel pi) {
		String initiator = StringUtils.EMPTY;

		DocumentRoute genericWf = ToutaticeWorkflowHelper.getWorkflowByName(
				ProceduresConstants.GENERICMODEL_ID, pi);
		if (genericWf != null) {
			initiator = (String) genericWf.getDocument().getPropertyValue(
					DocumentRoutingConstants.INITIATOR);
		}

		return initiator;
	}

	/**
	 * @return the user name.
	 */
	public static String getUsername(String login){
		NuxeoPrincipal principal = userManager.getPrincipal(login);
		String firstName = principal.getFirstName();
		String lastName = principal.getLastName();

		return firstName + " " + lastName;
	}

}
