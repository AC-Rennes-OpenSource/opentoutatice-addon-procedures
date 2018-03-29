/**
 * 
 */
package org.osivia.procedures.record.security.rules.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.api.model.impl.MapProperty;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.record.RecordsConstants;
import org.osivia.procedures.record.security.rules.model.relation.RelationModel;
import org.osivia.procedures.record.security.rules.model.type.Entity;
import org.osivia.procedures.record.security.rules.model.type.FieldsConstants;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.query.helper.ToutaticeEsQueryHelper;

/**
 * @author david
 *
 */
public class RecordsFetcherHelper {

	public static final String RECORDS_QUERY = "select * from Record where rcd:type = '%s' and rcd:data.%s.ttc:webid in %s "
			+ RecordsConstants.DEFAULT_FILTER;

	public static final String TGT_RECORDS_QUERY = "select * from Record where rcd:type = '%s' and ttc:webid in %s "
			+ RecordsConstants.DEFAULT_FILTER;

	private RecordsFetcherHelper() {
		super();
	}

	public static DocumentModelList fetchSourceRecords(CoreSession session, RelationModel relationModel,
			Entity targetEntity) {
		List<String> tgtIds = getIds(targetEntity);
		String query = String.format(RECORDS_QUERY, relationModel.getSourceType(), relationModel.getTargetKey(),
				getInOperand(tgtIds));

		return ToutaticeEsQueryHelper.query(session, query, -1);
	}

	// public static DocumentModelList fetchTargetRecords(CoreSession session,
	// RelationModel relationModel,
	// Entity srcEntity) {
	// List<String> srcIds = getIds(srcEntity);
	// String query = String.format(RECORDS_QUERY, relationModel.getTargetType(),
	// relationModel.getTargetKey(),
	// getInOperand(srcIds));
	//
	// DocumentModelList targets = ToutaticeEsQueryHelper.query(session, query, -1);
	// // Detach them to load all data and do not use session to fetch data on later
	// calls
	// DocumentModelList detachedTgts = new DocumentModelListImpl(targets.size());
	//
	// for(DocumentModel tgt : targets) {
	// tgt.detach(true);
	// detachedTgts.add(tgt);
	// }
	//
	// return detachedTgts;
	// }

	public static List<String> getIds(Entity entity) {
		List<String> ids = new ArrayList<>(0);

		if (entity.getRecords() != null) {
			for (DocumentModel record : entity.getRecords()) {
				ids.add(record.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID)
						.getValue(String.class));
			}
		}

		return ids;
	}

	public static DocumentModelList fetchTargetRecords(CoreSession session, RelationModel relationModel,
			Entity srcEntity, Entity targetEntity) {
		List<String> ids = getTargetIds(session, srcEntity.getRecordModel().getDocument(), srcEntity, relationModel);
		String query = String.format(TGT_RECORDS_QUERY, targetEntity.getType(), getInOperand(ids));

		return ToutaticeEsQueryHelper.query(session, query, -1);
	}

	public static List<String> getTargetIds(CoreSession session, DocumentModel model, Entity entity,
			RelationModel relationModel) {
		// Result
		List<String> webIds = null;
		String targetKey = relationModel.getTargetKey();

		if (StringUtils.isNotBlank(targetKey)) {
			// Flat value stored as global String (not denormalized)
			String webIdsAsString = null;
			webIds = new ArrayList<>(0);

			for (DocumentModel record : entity.getRecords()) {
				ListProperty fields = (ListProperty) record.getProperty(ProceduresConstants.RCD_VALUES_XPATH);
				for (Property field : fields) {
					MapProperty fld = (MapProperty) field;

					if (StringUtils.equals(targetKey, fld.get(FieldsConstants.NAME).getValue(String.class))) {
						webIdsAsString = fld.get(FieldsConstants.VALUE).getValue(String.class);
					}
				}

				if (StringUtils.isNotBlank(webIdsAsString)) {
					// Extract webIds
                    webIds = RecordHelper.getIds(webIds, webIdsAsString);
				}
			}
		}

		return webIds;
	}

	public static String getInOperand(List<String> elements) {
		StringBuffer operand = new StringBuffer();

		if (elements != null) {
			operand.append("('");
			operand.append(StringUtils.join(elements, "','"));
			operand.append("')");
		}

		return operand.toString();
	}

}
