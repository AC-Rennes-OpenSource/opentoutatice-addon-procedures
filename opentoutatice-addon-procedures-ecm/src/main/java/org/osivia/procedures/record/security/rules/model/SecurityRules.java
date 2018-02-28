/**
 * 
 */
package org.osivia.procedures.record.security.rules.model;

import java.util.List;

import org.osivia.procedures.record.security.rules.model.relation.Relation;
import org.osivia.procedures.record.security.rules.model.type.Entity;

/**
 * @author david
 *
 */
public class SecurityRules {
	
	private ModelsRules modelsRules;
	private RelationsRules relationsRules;
	
	private List<String> recordsIds;
	
	public SecurityRules() {
		super();
		this.modelsRules = new ModelsRules();
		this.relationsRules = new RelationsRules();
	}
	
	public void addModelIfNotPresent(String type) {
		this.modelsRules.addEntity(new Entity(type));
	}
	
	public void addRelationIfNotPresent(Relation relation) {
		this.relationsRules.addIfNotPresent(relation);
	}

	public ModelsRules getModelsRules() {
		return modelsRules;
	}

	public void setModelsRules(ModelsRules modelsRules) {
		this.modelsRules = modelsRules;
	}

	public RelationsRules getRelationsRules() {
		return relationsRules;
	}

	public void setRelationsRules(RelationsRules relationsRules) {
		this.relationsRules = relationsRules;
	}

	public List<String> getRecordsIds() {
		return recordsIds;
	}

	public void setRecordsIds(List<String> recordsIds) {
		this.recordsIds = recordsIds;
	}

}
