/**
 * 
 */
package org.osivia.procedures.record.security.rules.model.type;

/**
 * @author david
 *
 */
// FIXME: to generate from Model?
public enum FieldType {

	Person("PERSON"), Record("RECORD");

	private String type;

	private FieldType(String type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

}
