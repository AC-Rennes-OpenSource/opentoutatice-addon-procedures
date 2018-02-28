package org.osivia.procedures.record.es.customizer.writer;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.osivia.procedures.constants.ProceduresConstants;
import org.osivia.procedures.es.customizer.writer.helper.DenormalizationJsonESWriterHelper;

import fr.toutatice.ecm.es.customizer.writers.api.AbstractCustomJsonESWriter;

/**
 * @author Dorian Licois
 */
public class RecordJsonESWriter extends AbstractCustomJsonESWriter {

	private static final String ECM_ACL = "ecm:acl";

	/**
	 * Constructor.
	 */
	public RecordJsonESWriter() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(DocumentModel doc) {
		return doc != null && ProceduresConstants.RECORD_TYPE.equals(doc.getType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeData(JsonGenerator jg, DocumentModel document, String[] schemas,
			Map<String, String> contextParameters) throws IOException {
		// Set model type on Record
		if (ProceduresConstants.RECORD_TYPE.equals(document.getType())) {
			DocumentModel recordModel = super.session.getParentDocument(document.getRef());
			if (recordModel != null) {
				jg.writeStringField("rcd:type", (String) recordModel.getPropertyValue("ttc:webid"));
			}
		}

		// Custom name /value
		DenormalizationJsonESWriterHelper.mapKeyValue(jg, document, ProceduresConstants.RCD_VALUES_XPATH,
				ProceduresConstants.PI_ENTRY_KEY, ProceduresConstants.ENTRY_VALUE);

		// Custom name / value as Json
		DenormalizationJsonESWriterHelper.mapKeyValueAsJson(jg, document, ProceduresConstants.RCD_VALUES_XPATH,
				ProceduresConstants.PI_ENTRY_KEY, ProceduresConstants.ENTRY_VALUE);

	}

}
