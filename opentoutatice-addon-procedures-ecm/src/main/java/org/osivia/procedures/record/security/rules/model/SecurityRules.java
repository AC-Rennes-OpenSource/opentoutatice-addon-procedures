/**
 * 
 */
package org.osivia.procedures.record.security.rules.model;

import java.util.List;

import org.osivia.procedures.record.security.rules.model.relation.RecordsRelation;
import org.osivia.procedures.record.security.rules.model.type.Entity;

/**
 * @author david
 *
 */
public class SecurityRules {
	
	private SecurityRecordsModels modelsRules;
	private SecurityRelations relationsRules;
	
	private List<String> recordsIds;
	
	public SecurityRules() {
		super();
		this.modelsRules = new SecurityRecordsModels();
		this.relationsRules = new SecurityRelations();
	}
	
	public void addModelIfNotPresent(String type) {
		this.modelsRules.addEntity(new Entity(type));
	}

	public SecurityRecordsModels getModelsRules() {
		return modelsRules;
	}

	public void setModelsRules(SecurityRecordsModels modelsRules) {
		this.modelsRules = modelsRules;
	}

	public SecurityRelations getRelationsRules() {
		return relationsRules;
	}

	public void setRelationsRules(SecurityRelations relationsRules) {
		this.relationsRules = relationsRules;
	}

	public List<String> getRecordsIds() {
		return recordsIds;
	}

	public void setRecordsIds(List<String> recordsIds) {
		this.recordsIds = recordsIds;
	}

}
