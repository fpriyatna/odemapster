package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.List;

import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public class AlphaResult {
	private SQLLogicalTable alphaSubject;
	private List<SQLQuery> alphaPredicateObjects;
	private String predicateURI;
	
	public AlphaResult(SQLLogicalTable alphaSubject
			, List<SQLQuery> alphaPredicateObjects, String predicateURI) {
		super();
		this.alphaSubject = alphaSubject;
		this.alphaPredicateObjects = alphaPredicateObjects;
		this.predicateURI = predicateURI;
	}

	public SQLLogicalTable getAlphaSubject() {
		return alphaSubject;
	}

	public List<SQLQuery> getAlphaPredicateObjects() {
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
