/**
 * 
 */
package org.osivia.procedures.record.security.rules;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.security.rules.helper.RecordModelHelper;
import org.osivia.procedures.record.security.rules.model.SecurityRules;
import org.osivia.procedures.record.security.rules.model.type.Entity;

import fr.toutatice.ecm.platform.core.query.helper.ToutaticeEsQueryHelper;

/**
 * @author david
 *
 */
public class SecurityRulesBuilder {

	private static final Log log = LogFactory.getLog(SecurityRulesBuilder.class);

	private static LinkedEntitiesResolver resolver;

	private static SecurityRulesBuilder instance;

	private SecurityRulesBuilder() {
		super();
	}

	public static synchronized SecurityRulesBuilder getInstance() {
		if (instance == null) {
			instance = new SecurityRulesBuilder();
		}
		return instance;
	}

	public static LinkedEntitiesResolver getLinkedEntitiesResolver() {
		if (resolver == null) {
			resolver = LinkedEntitiesResolver.getInstance();
		}
		return resolver;
	}

	public SecurityRules build(String repositoryName, Principal principal) {
		// Result
		SecurityRules rules = new SecurityRules();

		// To do in unrestricted mode
		CoreSession systemSession = null;

		try {
			repositoryName = StringUtils.isNotBlank(repositoryName) ? repositoryName
					: ProceduresConstants.DEFAULT_REPOSITORY_NAME;

			systemSession = CoreInstance.openCoreSessionSystem(repositoryName);

			Map<String, Entity> securityEntities = SecurityEntitiesResolver.getInstance()
					.getSecurityEntitiesOf(systemSession, principal);

			if (securityEntities != null && securityEntities.size() > 0) {
				// Get models
				DocumentModelList models = ToutaticeEsQueryHelper.query(systemSession,
						RecordModelHelper.RECORD_MODEL_QUERY);

				// Debug
				final long begin = System.currentTimeMillis();

				rules = builsRules(systemSession, rules, securityEntities, new HashMap<String, Entity>(),
						new HashSet<String>(), models);

				if (log.isDebugEnabled()) {
					final long end = System.currentTimeMillis();
					log.debug("[#resolveLinkedEntites] " + String.valueOf(end - begin) + " ms");
				}
			}
		} finally {
			if (systemSession != null) {
				systemSession.close();
			}
		}

		return rules;
	}

	
	public SecurityRules builsRules(CoreSession session, SecurityRules rules, Map<String, Entity> entities,
			Map<String, Entity> linkedEntities, Set<String> treatedEntities, DocumentModelList models) {

		// Compute linked entities
		for (Entity entity : entities.values()) {

			Map<String, Entity> linkedEntitiesTo = getLinkedEntitiesResolver().getLinkedEntitiesTo(session, rules,
					models, entity);

			if (log.isDebugEnabled()) {
				log.debug("[Linked entities to: " + entity.getType() + " ]");
				if (linkedEntitiesTo.keySet() != null) {
					for (String type : linkedEntitiesTo.keySet()) {
						log.debug(type);
					}
				}
			}

			Map<String, Entity> linkedEntitiesFrom = getLinkedEntitiesResolver().getLinkedEntitiesFrom(session, rules,
					models, entity);

			if (log.isDebugEnabled()) {
				log.debug("[Linked entities from: " + entity.getType() + " ]");
				if (linkedEntitiesFrom.keySet() != null) {
					for (String type : linkedEntitiesFrom.keySet()) {
						log.debug(type);
					}
				}
			}

			for (Entity entTo : linkedEntitiesTo.values()) {
				if (!linkedEntities.containsKey(entTo.getType())) {
					linkedEntities.put(entTo.getType(), entTo);
				}
			}

			for (Entity entFrom : linkedEntitiesFrom.values()) {
				if (!linkedEntities.containsKey(entFrom.getType())) {
					linkedEntities.put(entFrom.getType(), entFrom);
				}
			}

			if (!treatedEntities.contains(entity.getType())) {
				treatedEntities.add(entity.getType());
			}

			Map<String, Entity> nextEntities = getNextEntities(linkedEntities, treatedEntities);

			if (MapUtils.isNotEmpty(nextEntities)) {
				builsRules(session, rules, nextEntities, linkedEntities, treatedEntities, models);
			}

		}

		return rules;

	}

	private Map<String, Entity> getNextEntities(Map<String, Entity> linkedEntities, Set<String> treatedEntities) {
		Map<String, Entity> nextEntities = new HashMap<>(0);

		if (linkedEntities != null) {
			for (Entry<String, Entity> entry : linkedEntities.entrySet()) {
				String type = entry.getKey();
				if (!treatedEntities.contains(type)) {
					nextEntities.put(type, entry.getValue());
				}
			}
		}

		return nextEntities;
	}

}
