/**
 * 
 */
package org.osivia.procedures.record.security.rules.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author david
 *
 */
public class SecurityRelations extends HashSet<SecurityRelation> {
	
	private static final long serialVersionUID = 4346544310242790071L;
	
	public SecurityRelations() {
		super();
	}
	
	public SecurityRelations(int size) {
		super(size);
	}
	
	public Set<String> getTypes() {
		Set<String> types = new HashSet<>(0);
		
		Iterator<SecurityRelation> iterator = this.iterator();
		while(iterator.hasNext()) {
			SecurityRelation securityRelation = iterator.next();
			types.add(securityRelation.getRecordType());
		}
		
		return types;
	}
	
}
