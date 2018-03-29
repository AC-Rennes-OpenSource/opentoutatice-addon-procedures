/**
 * 
 */
package org.osivia.procedures.record.security.rules.helper;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * @author david
 *
 */
public class RecordHelper {

	private RecordHelper() {
		super();
	}

	public static List<String> getIds(List<String> elements, String extractFrom) {
        try {
            JSONArray array = JSONArray.fromObject(extractFrom);
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                for (Object key : object.keySet()) {
                    elements.add(object.getString(String.valueOf(key)));
                }
            }
        } catch (JSONException e) {
            // Do nothing
        }

		return elements;
	}

}
