/**
 * 
 */
package org.osivia.procedures.record.security.rules.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osivia.procedures.record.security.rules.model.type.Entity;

/**
 * @author david
 *
 */
public class SecurityRecordsModels {
	
	private Map<String, Entity> entities;
	
	public SecurityRecordsModels() {
		super();
		this.entities = new HashMap<>(0);
	}
	
	public void addEntity(Entity entity) {
		if(!this.entities.containsKey(entity.getType())) {
			this.entities.put(entity.getType(), entity);
		}
	}
	
	public boolean yetBrowsed(String type) {
		return this.entities.containsKey(type);
	}

	public Map<String, Entity> getEntities() {
		return entities;
	}
	
	public Set<String> getTypes(){
		return this.entities.keySet();
	}

}
