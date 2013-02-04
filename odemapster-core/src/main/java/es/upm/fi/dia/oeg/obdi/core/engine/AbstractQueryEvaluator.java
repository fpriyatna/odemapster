package es.upm.fi.dia.oeg.obdi.core.engine;

public abstract class AbstractQueryEvaluator {
	public abstract AbstractResultSet evaluateQuery(String query) throws Exception;
}
