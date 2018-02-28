/**
 * 
 */
package org.osivia.procedures.record.security.rules.model.relation;

/**
 * @author david
 *
 */
// FIXME: to generate from Model?
public enum RelationType {

	Person("PERSON"), Record("RECORD");

	private String type;

	private RelationType(String type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

}
