/**
 * 
 */
package org.osivia.procedures.record.security.rules;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.RecordsConstants;
import org.osivia.procedures.record.security.rules.helper.EntityHelper;
import org.osivia.procedures.record.security.rules.helper.RelationModelHelper;
import org.osivia.procedures.record.security.rules.model.relation.RelationModel;
import org.osivia.procedures.record.security.rules.model.relation.RelationModelType;
import org.osivia.procedures.record.security.rules.model.type.Entity;
import org.osivia.procedures.record.security.rules.model.type.FieldType;
import org.osivia.procedures.record.security.rules.model.type.RecordModel;

import fr.toutatice.ecm.platform.core.query.helper.ToutaticeEsQueryHelper;

/**
 * @author david
 *
 */
public class SecurityEntitiesResolver {

	private static final Log log = LogFactory.getLog(SecurityEntitiesResolver.class);

	public static final String SECURITY_ENTITIES_MODELS_QUERY = "select * from RecordFolder where pcd:globalVariablesDefinitions.type = 'PERSON' ";
	public static final String SECURITY_ENTITIES_OF_QUERY = "select * from Record where rcd:type = '%s' and rcd:data.%s = '%s' "
			+ RecordsConstants.DEFAULT_FILTER;

	private static SecurityEntitiesResolver instance;

	private SecurityEntitiesResolver() {
		super();
	}

	public static synchronized SecurityEntitiesResolver getInstance() {
		if (instance == null) {
			instance = new SecurityEntitiesResolver();
		}
		return instance;
	}

	public Map<String, Entity> getSecurityEntitiesOf(CoreSession session, Principal principal) {
		// Result
		Map<String, Entity> entities = null;

		DocumentModelList securityModels = ToutaticeEsQueryHelper.query(session,
				SECURITY_ENTITIES_MODELS_QUERY + RecordsConstants.DEFAULT_FILTER, -1);

		// Get security records of current user
		for (DocumentModel securityModel : securityModels) {
			// Model and instances informations
			Set<RelationModel> usersRelation = getUsersRelations(securityModel);
			DocumentModelList securityRecords = getSecurityRecordsOf(session, usersRelation, principal);

			// Current user belongs to Security Entity
			if (CollectionUtils.isNotEmpty(securityRecords)) {
				// Entity
				Entity securityEntity = new Entity(EntityHelper.getType(securityModel));
				securityEntity.setRecordModel(new RecordModel(securityModel));
				securityEntity.setRecords(securityRecords);

				// Store
				if (entities == null) {
					entities = new HashMap<>();
				}
				entities.put(EntityHelper.getType(securityModel), securityEntity);
			}

		}

		return entities;
	}

	private DocumentModelList getSecurityRecordsOf(CoreSession session, Set<RelationModel> relationsModels,
			Principal principal) {
		// Result
		DocumentModelList records = null;

		if (relationsModels != null) {
			for (RelationModel relationM : relationsModels) {
				// Query to fetch records of given type
				String query = String.format(SECURITY_ENTITIES_OF_QUERY, relationM.getSourceType(),
						relationM.getTargetKey(), principal.getName());
				if (records == null) {
					records = new DocumentModelListImpl();
				}
				records.addAll(ToutaticeEsQueryHelper.query(session, query, -1));
			}
		}

		return records;
	}

	private Set<RelationModel> getUsersRelations(DocumentModel securityModel) {
		// Result
		Set<RelationModel> usersRelations = null;

		ListProperty fieldsDefinitions = (ListProperty) securityModel
				.getProperty(ProceduresConstants.PROCEDURE_DEFINITIONS_XPATH);
		if (fieldsDefinitions != null) {
			for (Property fieldDefinition : fieldsDefinitions) {
				// Property is a map
				MapProperty fieldDef = (MapProperty) fieldDefinition;

				// Check if field definition is the one of PERSON type
				String fieldDefType = fieldDef.get("type").getValue(String.class);
				if (StringUtils.equals(FieldType.Person.getType(), fieldDefType)) {
					// Build Relation
					RelationModel usrRelation = buildRelationModel(securityModel, fieldDef);

					// Store
					if (usersRelations == null) {
						usersRelations = new HashSet<>(1);
					}
					usersRelations.add(usrRelation);
				}
			}
		}

		return usersRelations;
	}

	private RelationModel buildRelationModel(DocumentModel model, MapProperty fieldDefinitionType) {
		// Result
		RelationModel relationM = null;

		String fieldName = fieldDefinitionType.get("name").getValue(String.class);

		if (RelationModelHelper.isOneToNRelation(model, fieldName)) {
			// Build Relation model
			relationM = new RelationModel(RelationModelType.oneToN);
			relationM.setSourceType(EntityHelper.getType(model));
			relationM.setTargetKey(RelationModelHelper.getOneToNRelationKey(model, fieldName) + "/" + fieldName);

		} else if (RelationModelHelper.isOneToOneRelation(model, fieldName)) {
			// Build Relation model
			relationM = new RelationModel(RelationModelType.oneToOne);
			relationM.setSourceType(EntityHelper.getType(model));
			relationM.setTargetKey(fieldName);
		}

		return relationM;
	}

}
