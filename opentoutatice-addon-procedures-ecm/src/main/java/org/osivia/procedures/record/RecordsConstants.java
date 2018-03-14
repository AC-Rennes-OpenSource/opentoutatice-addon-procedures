/**
 * 
 */
package org.osivia.procedures.record;

/**
 * @author david
 *
 */
public interface RecordsConstants {
	
	String DEFAULT_FILTER = " and ecm:isVersion = 0 and ecm:isProxy = 0 and ecm:currentLifeCycleState <> 'deleted' ";
	String UNKNOWN_TYPE = "UNKNOWN_TYPE";
	
	/** Custom ES writing. */
	String CUSTOM_RECORD_XPATH = "rcd:data";

}
