/**
 * 
 */
package org.osivia.procedures.record.model;

/**
 * @author david
 *
 */
public enum FieldTypes {
	
	picture("PICTURE"), file("FILE");
	
	private String value;
	
	private FieldTypes(String value) {
		this.value = value;
	}
	
	public String value() {
		return this.value;
	}
	
	public static String[] types() {
		FieldTypes[] fieldTypes = FieldTypes.values();
		String[] values = new String[fieldTypes.length];
		
		for(int index = 0; index < fieldTypes.length; index++) {
			values[index] = fieldTypes[index].value();
		}
		
		return values;
	}

}
