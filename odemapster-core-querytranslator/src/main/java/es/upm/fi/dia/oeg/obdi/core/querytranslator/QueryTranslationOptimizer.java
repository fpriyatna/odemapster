package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;

public class QueryTranslationOptimizer implements IQueryTranslationOptimizer {

	private boolean selfJoinElimination = false;
	private boolean unionQueryReduction = false;
	private boolean subQueryElimination = false;
	private boolean subQueryAsView = false;
	
	public boolean isSelfJoinElimination() {
		return selfJoinElimination;
	}

	public void setSelfJoinElimination(boolean selfJoinElimination) {
		this.selfJoinElimination = selfJoinElimination;
	}

	public void setUnionQueryReduction(boolean unionQueryReduction) {
		this.unionQueryReduction = unionQueryReduction;
	}

	public boolean isUnionQueryReduction() {
		return this.unionQueryReduction;
	}

	public boolean isSubQueryElimination() {
		return subQueryElimination;
	}

	public void setSubQueryElimination(boolean subQueryElimination) {
		this.subQueryElimination = subQueryElimination;
	}

	public boolean isSubQueryAsView() {
		return subQueryAsView;
	}

	public void setSubQueryAsView(boolean subQueryAsView) {
		this.subQueryAsView = subQueryAsView;
	}

}
