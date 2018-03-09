/**
 * 
 */
package org.osivia.procedures.record.security.rules.model.relation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.osivia.procedures.record.security.rules.model.type.Entity;

/**
 * Structure: {sourcesIds: {targetKey, targetIds}}
 * 
 * @author david
 *
 */
public class RecordsRelation {

	private RelationModel relationModel;
	
	private Entity sourceEntity;
	private Entity targetEntity;
	
	private List<String> sourceIds;
	private List<String> targetIds;
	
	public RecordsRelation() {
		super();
	}

	public RecordsRelation(RelationModel relationModel) {
		super();
		this.relationModel = relationModel;
		
		this.sourceIds = new ArrayList<>(0);
		this.targetIds = new ArrayList<>(0);
	}

	public RelationModelType getType() {
		return this.relationModel.getType();
	}

	public String getSourceType() {
		return this.relationModel.getSourceType();
	}

	public String getTargetKey() {
		return this.relationModel.getTargetKey();
	}

	public String getTargetType() {
		return this.relationModel.getTargetType();
	}

	public RelationModel getRelationModel() {
		return relationModel;
	}

	public void setRelationModel(RelationModel relationModel) {
		this.relationModel = relationModel;
	}
	
	public void addSourceId(String id) {
		if(!this.sourceIds.contains(id)) {
			this.sourceIds.add(id);
		}
	}
	
	public List<String> getSourceIds() {
		return sourceIds;
	}

	public void setSourceIds(List<String> sourceIds) {
		this.sourceIds = sourceIds;
	}
	
	public void addTargetId(String id) {
		if(!this.targetIds.contains(id)) {
			this.targetIds.add(id);
		}
	}

	public List<String> getTargetIds() {
		return targetIds;
	}

	public void setTargetIds(List<String> targetIds) {
		this.targetIds = targetIds;
	}


	public Entity getSourceEntity() {
		return sourceEntity;
	}

	public void setSourceEntity(Entity sourceEntity) {
		this.sourceEntity = sourceEntity;
	}

	public Entity getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(Entity targetEntity) {
		this.targetEntity = targetEntity;
	}

	@Override
	public boolean equals(Object other) {
		boolean equals = false;

		if (other != null && other instanceof RecordsRelation) {
			RecordsRelation otherRelation = (RecordsRelation) other;
			equals = StringUtils.equals(getSourceType(), otherRelation.getSourceType())
					&& StringUtils.equals(getTargetType(), otherRelation.getTargetType())
					&& getType().equals(otherRelation.getType());
		}
		return equals;
	}
	
	@Override
	public int hashCode() {
		return (getSourceType() + "-" + getTargetType()).hashCode();
	}

}
