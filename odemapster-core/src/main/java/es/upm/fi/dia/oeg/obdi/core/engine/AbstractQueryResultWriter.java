package es.upm.fi.dia.oeg.obdi.core.engine;

public abstract class AbstractQueryResultWriter {
	AbstractResultSet abstractResultSet;
	IQueryTranslator queryTranslator;
	
	public abstract void initalize() throws Exception;
	public abstract void preProcess() throws Exception;
	public abstract void process() throws Exception;
	public abstract void postProcess() throws Exception;
	
	
	public void setResultSet(AbstractResultSet resultSet) {
		this.abstractResultSet = resultSet;
	}
	void setAbstractResultSet(AbstractResultSet abstractResultSet) {
		this.abstractResultSet = abstractResultSet;
	}
	void setQueryTranslator(IQueryTranslator queryTranslator) {
		this.queryTranslator = queryTranslator;
	}

}
