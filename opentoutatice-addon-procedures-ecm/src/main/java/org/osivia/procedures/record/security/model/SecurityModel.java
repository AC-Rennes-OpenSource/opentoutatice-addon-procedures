/**
 * 
 */
package org.osivia.procedures.record.security.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.osivia.procedures.record.security.rules.model.relation.RelationModel;

/**
 * @author david
 *
 */
public class SecurityModel {
	
	private Set<String> types;
	private List<String> entryModelTypes;
	private List<RelationModel> relationsModels;
	
	private static SecurityModel instance;
	
	private SecurityModel() {
		super();
		this.types = new HashSet<>();
		this.entryModelTypes = new LinkedList<>();
		this.relationsModels = new ArrayList<>();
	}
	
	public static synchronized SecurityModel getInstance() {
		if(instance == null) {
			instance = new SecurityModel();
		}
		return instance;
	}
	
	public void build(){
		// Types
		this.types.add("facture");
		this.types.add("client");
		this.types.add("produit");
		this.types.add("actualite");
		this.types.add("document");
		
		// Relations
//		RelationModel fToCl = new RelationModel(RelationModel.Type.oneToOne);
//		fToCl.addSourceType("facture");
//		fToCl.addTargetType("client");
//		this.relationsModels.add(fToCl);
//		
//		RelationModel clToPdts = new RelationModel(RelationModel.Type.oneToN);
//		clToPdts.addSourceType("client");
//		clToPdts.addTargetType("produits/produit");
//		this.relationsModels.add(clToPdts);
//		
//		RelationModel actToPdts = new RelationModel(RelationModel.Type.oneToOne);
//		actToPdts.addSourceType("actualite");
//		actToPdts.addTargetType("produit");
//		this.relationsModels.add(actToPdts);
//		
//		RelationModel docToPdts = new RelationModel(RelationModel.Type.oneToN);
//		docToPdts.addSourceType("document");
//		docToPdts.addTargetType("produits/produit");
//		this.relationsModels.add(docToPdts);
//		
//		// Entry points
//		this.entryModelTypes.add("facture");
//		this.entryModelTypes.add("document");
		
	}

	public List<String> getEntryModelTypes() {
		return entryModelTypes;
	}

	public List<RelationModel> getRelationsModels() {
		return relationsModels;
	}
	
}
