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
import org.osivia.procedures.record.security.rules.model.relation.RelationModel;
import org.osivia.procedures.record.security.rules.model.relation.RelationModelType;
import org.osivia.procedures.record.security.rules.model.type.Entity;
import org.osivia.procedures.record.security.rules.model.type.FieldsConstants;

/**
 * @author david
 *
 */
public class RelationModelHelper {
	
	private RelationModelHelper () {
		super();
	}
	
	public static String getTargetKey(MapProperty field) {
		return field.get(FieldsConstants.NAME).getValue(String.class);
	}
	
	public static boolean isOneToNRelation(DocumentModel srcModel, String targetKey) {
		ListProperty references = (ListProperty) srcModel.getProperty(FieldsConstants.REFERENCES);
		return getOneToNRelationIndicator(references, targetKey) != null;
	}

	public static boolean isOneToOneRelation(DocumentModel targetModel, String targetKey) {
		return !isOneToNRelation(targetModel, targetKey);
	}
	
	public static String getOneToNRelationIndicator(ListProperty references, String targetKey) {
		Iterator<Property> iterator = references.iterator();

		boolean typeFound = false;
		String oneToNIndicator = null;

		while (iterator.hasNext() && !typeFound) {
			MapProperty ref = (MapProperty) iterator.next();
			String variable = ref.get(FieldsConstants.VARIABLE_NAME).getValue(String.class);

			if (StringUtils.equals(targetKey, variable)) {
				typeFound = true;

				String path = ref.get(FieldsConstants.PATH).getValue(String.class);

				if (StringUtils.isNotBlank(path)) {
					String[] levels = StringUtils.split(path, ",");

					if (levels.length > 1) {
						// It is a N-N relation
						oneToNIndicator = levels[0];
					}
				}
			}
		}

		return oneToNIndicator;
	}
	
	public static String getOneToNRelationKey(DocumentModel srcModel, String targetKey) {
		String relationKey = StringUtils.EMPTY;

		ListProperty references = (ListProperty) srcModel.getProperty(FieldsConstants.REFERENCES);

		String nToNBaseLevel = RelationModelHelper.getOneToNRelationIndicator(references, targetKey);

		if (StringUtils.isNotBlank(nToNBaseLevel)) {
			Iterator<Property> iterator = references.iterator();
			boolean parentFound = false;

			while (iterator.hasNext() && !parentFound) {
				MapProperty ref = (MapProperty) iterator.next();
				String path = ref.get(FieldsConstants.PATH).getValue(String.class);

				if (StringUtils.equals(nToNBaseLevel, path)) {
					parentFound = true;
					relationKey = ref.get(FieldsConstants.VARIABLE_NAME).getValue(String.class);
				}
			}
		}

		return relationKey;
	}
	
	public static RelationModel buildModel(RelationModelType relationType, String srcType, String targetKey, String targetType) {
		RelationModel relationModel = new RelationModel(relationType);
		
		relationModel.setSourceType(srcType);
		relationModel.setTargetKey(targetKey);
		relationModel.setTargetType(targetType);
		
		return relationModel;
	}

	public static String getTargetKey(RelationModelType relationType, DocumentModel model, String targetKey) {
		String key = targetKey;
		
		if(RelationModelType.oneToN.equals(relationType)) {
			key = getOneToNRelationKey(model, targetKey);
		}
		
		return key;
	}

}
