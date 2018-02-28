/**
 * 
 */
package org.osivia.procedures.record.security.rules.helper;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;

/**
 * @author david
 *
 */
public class RelationHelper {
	
	private RelationHelper () {
		super();
	}
	
	public static boolean isNToNRelation(DocumentModel model, String fieldName) {
		ListProperty references = (ListProperty) model.getProperty("pcd:steps/0/globalVariablesReferences");
		return getNToNRelationIndicator(fieldName, references) != null;
	}

	public static boolean isNToOneRelation(DocumentModel model, String fieldName) {
		return !isNToNRelation(model, fieldName);
	}
	
	public static  String getNToNRelationIndicator(String fieldName, ListProperty references) {
		Iterator<Property> iterator = references.iterator();

		boolean typeFound = false;
		String levelIndicator = null;

		while (iterator.hasNext() && !typeFound) {
			MapProperty ref = (MapProperty) iterator.next();
			String variable = ref.get("variableName").getValue(String.class);

			if (StringUtils.equals(fieldName, variable)) {
				typeFound = true;

				String path = ref.get("path").getValue(String.class);

				if (StringUtils.isNotBlank(path)) {
					String[] levels = StringUtils.split(path, ",");

					if (levels.length > 1) {
						// It is a N-N relation
						levelIndicator = levels[0];
					}
				}
			}
		}

		return levelIndicator;
	}
	
	public static String getNToNRelationKey(DocumentModel model, String fieldName) {
		String relationKey = StringUtils.EMPTY;

		ListProperty references = (ListProperty) model.getProperty("pcd:steps/0/globalVariablesReferences");

		String nToNBaseLevel = RelationHelper.getNToNRelationIndicator(fieldName, references);

		if (StringUtils.isNotBlank(nToNBaseLevel)) {
			Iterator<Property> iterator = references.iterator();
			boolean parentFound = false;

			while (iterator.hasNext() && !parentFound) {
				MapProperty ref = (MapProperty) iterator.next();
				String path = ref.get("path").getValue(String.class);

				if (StringUtils.equals(nToNBaseLevel, path)) {
					parentFound = true;
					relationKey = ref.get("variableName").getValue(String.class);
				}
			}
		}

		return relationKey;
	}

}
