package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.List;

import es.upm.fi.dia.oeg.obdi.core.sql.SQLJoinQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;

public class AlphaResult {
	private SQLLogicalTable alphaSubject;
	private List<SQLJoinQuery> alphaPredicateObjects;
	private String predicateURI;
	
	public AlphaResult(SQLLogicalTable alphaSubject
			, List<SQLJoinQuery> alphaPredicateObjects, String predicateURI) {
		super();
		this.alphaSubject = alphaSubject;
		this.alphaPredicateObjects = alphaPredicateObjects;
		this.predicateURI = predicateURI;
	}

	public SQLLogicalTable getAlphaSubject() {
		return alphaSubject;
	}

	public List<SQLJoinQuery> getAlphaPredicateObjects() {
		return alphaPredicateObjects;
	}

	@Override
	public String toString() {
		return "AlphaResult [alphaSubject=" + alphaSubject
				+ ", alphaPredicateObjects=" + alphaPredicateObjects
				+ ", predicateURI=" + predicateURI + "]";
	}

	public String getPredicateURI() {
		return predicateURI;
	}
	
	
	
}
