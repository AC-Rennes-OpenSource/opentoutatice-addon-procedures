/**
 * 
 */
package org.osivia.procedures.record.security.rules.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author david
 *
 */
public class RecordHelper {

	private RecordHelper() {
		super();
	}

	public static List<String> getIds(List<String> elements, String extractFrom, Pattern pattern) {
		if (elements != null) {
			Matcher matcher = pattern.matcher(extractFrom);
			while (matcher.find()) {
				elements.add(matcher.group(1));
			}
		}
		return elements;
	}

}
