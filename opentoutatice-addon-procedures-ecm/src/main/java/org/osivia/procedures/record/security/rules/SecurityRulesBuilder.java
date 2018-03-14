/**
 * 
 */
package org.osivia.procedures.record.security.rules;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.security.rules.helper.RecordModelHelper;
import org.osivia.procedures.record.security.rules.model.SecurityRelation;
import org.osivia.procedures.record.security.rules.model.SecurityRelations;
import org.osivia.procedures.record.security.rules.model.SecurityRules;
import org.osivia.procedures.record.security.rules.model.relation.RecordsRelation;
import org.osivia.procedures.record.security.rules.model.type.Entity;

import fr.toutatice.ecm.platform.core.query.helper.ToutaticeEsQueryHelper;

/**
 * @author david
 *
 */
public class SecurityRulesBuilder {

	private static final Log log = LogFactory.getLog(SecurityRulesBuilder.class);
	
	private static SecurityRulesBuilder instance;

	// Temporary ?
	private Map<String, SecurityRelations> rulesByUser;

	private RecordsRelationsResolver resolver;

	private SecurityRulesBuilder() {
		super();
		rulesByUser = new ConcurrentHashMap<>(0);
	}

	public static synchronized SecurityRulesBuilder getInstance() {
		if (instance == null) {
			instance = new SecurityRulesBuilder();
		}
		return instance;
	}

	public RecordsRelationsResolver getRecordsRelationsResolver() {
		if (this.resolver == null) {
			this.resolver = RecordsRelationsResolver.getInstance();
		}
		return this.resolver;
	}

	public SecurityRelations buildSecurityRelations(CoreSession session, Principal currentPrincipal) {
		UnrestrictedSecurityRulesBuilder builder = new UnrestrictedSecurityRulesBuilder(session, currentPrincipal);
		builder.runUnrestricted();
		return builder.getSecurityRelations();
	}

	public SecurityRelations build(CoreSession session, Principal currentPrincipal) {
		// Result
		SecurityRelations rules = null;

		// Check if yet calculated
		if (this.rulesByUser.containsKey(currentPrincipal.getName())) {
			rules = this.rulesByUser.get(currentPrincipal.getName());
		} else {
			rules = new SecurityRelations(0);

			Map<String, Entity> securityEntities = SecurityEntitiesResolver.getInstance().getSecurityEntitiesOf(session,
					currentPrincipal);

			if (securityEntities != null && securityEntities.size() > 0) {
				// Get models
				DocumentModelList models = ToutaticeEsQueryHelper.query(session, RecordModelHelper.RECORD_MODELS_QUERY);

				// Debug
				final long begin = System.currentTimeMillis();

				Map<String, Entity> treatedEntities = securityEntities;
				Set<RecordsRelation> treatedRelations = new HashSet<>(0);

				rules = buildSecurity(session, rules, securityEntities, models, treatedEntities, treatedRelations);

				// Store
				this.rulesByUser.put(currentPrincipal.getName(), rules);

				if (log.isDebugEnabled()) {
					final long end = System.currentTimeMillis();
					log.debug("[#resolveLinkedEntites] " + String.valueOf(end - begin) + " ms");
				}
			}
		}

		return rules;
	}
	
	public synchronized void invalidateRulesOf(String userName) {
		this.rulesByUser.remove(userName);
	}

	/**
	 * Browse entities (as Record types) building associated relations.
	 * 
	 * @param session
	 * @param securityRelations
	 * @param entities
	 * @param treatedRelations
	 * @param models
	 * @return
	 */
	public SecurityRelations buildSecurity(CoreSession session, SecurityRelations securityRelations,
			Map<String, Entity> entities, DocumentModelList models, Map<String, Entity> treatedEntities,
			Set<RecordsRelation> treteadRelations) {
		// Compute linked entities
		for (Entity entity : entities.values()) {

			if (log.isDebugEnabled()) {
				log.debug("Treating: " + entity.getType());
			}

			SecurityRelations inComingRelations = getRecordsRelationsResolver().getInComingRelations(session, models,
					entity, treatedEntities, treteadRelations);
			store(securityRelations, inComingRelations);

			if (log.isDebugEnabled()) {
				log.debug("* in:");
				for (SecurityRelation sr : inComingRelations) {
					log.debug(" - " + sr.getRecordType());
				}
			}

			SecurityRelations outComingRelations = getRecordsRelationsResolver().getOutComingRelations(session, models,
					entity, treatedEntities, treteadRelations);
			store(securityRelations, outComingRelations);

			if (log.isDebugEnabled()) {
				log.debug("* out:");
				for (SecurityRelation sr : outComingRelations) {
					log.debug(" - " + sr.getRecordType());
				}
			}

			Map<String, Entity> nextEntities = getNextEntities(inComingRelations, outComingRelations);
			if (MapUtils.isNotEmpty(nextEntities)) {
				buildSecurity(session, securityRelations, nextEntities, models, treatedEntities, treteadRelations);
			}

		}

		return securityRelations;

	}

	private void store(SecurityRelations securityRelations, SecurityRelations relations) {
		if (relations != null) {
			for (SecurityRelation sr : relations) {
				if (!securityRelations.contains(sr)) {
					securityRelations.add(sr);
				}
			}
		}
	}

	private synchronized Map<String, Entity> getNextEntities(SecurityRelations inComingRelations,
			SecurityRelations outComingRelations) {
		Map<String, Entity> nextEntities = new ConcurrentHashMap<>(0);

		for (SecurityRelation inSr : inComingRelations) {
			Entity entity = inSr.getEntity();
			nextEntities.put(entity.getType(), entity);
		}

		for (SecurityRelation outSr : outComingRelations) {
			Entity entity = outSr.getEntity();
			nextEntities.put(entity.getType(), entity);
		}

		return nextEntities;
	}

	// private Map<String, Entity> getNextEntities(SecurityRelations
	// secureRelations,
	// Map<String, Entity> treatedEntities) {
	// Map<String, Entity> nextEntities = new HashMap<>(0);
	//
	// for (SecurityRelation sr : secureRelations) {
	// String nextType = sr.getEntity().getType();
	// if (!treatedEntities.containsKey(nextType)) {
	// nextEntities.put(nextType, sr.getEntity());
	// }
	// }
	//
	// return nextEntities;
	// }
	//
	// private void store(Set<String> treatedEntities, SecurityRelations
	// outComingRelations) {
	// Iterator<SecurityRelation> outRelationIterator =
	// outComingRelations.iterator();
	//
	// if (log.isDebugEnabled()) {
	// log.debug("[Tretead entities]: ");
	// }
	//
	// while (outRelationIterator.hasNext()) {
	// SecurityRelation securityRelation = outRelationIterator.next();
	//
	// String entityType = securityRelation.getEntity().getType();
	//
	// if (log.isDebugEnabled()) {
	// log.debug(entityType);
	// }
	//
	// if (!treatedEntities.contains(entityType)) {
	// treatedEntities.add(entityType);
	// }
	// }
	// }

}
