/**
 * 
 */
package org.osivia.procedures.record.security.rules.model;

import java.util.HashSet;
import java.util.Set;

import org.osivia.procedures.record.security.rules.model.relation.Relation;

/**
 * @author david
 *
 */
public class RelationsRules {
	
	private Set<Relation> rules;
	
	public RelationsRules() {
		super();
		this.rules = new HashSet<>(0);
	}
	
	public void addIfNotPresent(Relation relation) {
		if(!this.rules.contains(relation)) {
			this.rules.add(relation);
		}
	}

	public Set<Relation> getRules() {
		return rules;
	}

	public void setRules(Set<Relation> rules) {
		this.rules = rules;
	}
	
}
