package es.upm.fi.dia.oeg.obdi.core.engine;

public abstract class AbstractQueryResultWriter {
	private AbstractResultSet abstractResultSet;
	private IQueryTranslator queryTranslator;
	
	public abstract void initialize() throws Exception;
	public abstract void preProcess() throws Exception;
	public abstract void process() throws Exception;
	public abstract void postProcess() throws Exception;
	public abstract Object getOutput() throws Exception;
	public abstract void setOutput(Object output) throws Exception;
	
	public void setResultSet(AbstractResultSet resultSet) {
		this.abstractResultSet = resultSet;
	}
	void setQueryTranslator(IQueryTranslator queryTranslator) {
		this.queryTranslator = queryTranslator;
	}
	public IQueryTranslator getQueryTranslator() {
		return queryTranslator;
	}
	public AbstractResultSet getResultSet() {
		return abstractResultSet;
	}

}
