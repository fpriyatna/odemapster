package es.upm.fi.dia.oeg.obdi.core.engine;

//import java.sql.ResultSetMetaData;
//import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public class DefaultResultProcessor {
	private static Logger logger = Logger.getLogger(DefaultResultProcessor.class);
	AbstractRunner runner;
	AbstractQueryEvaluator queryEvaluator;
	AbstractQueryResultWriter queryResultWriter;
	
	public DefaultResultProcessor(AbstractRunner runner
			, AbstractQueryEvaluator queryEvaluator, AbstractQueryResultWriter queryResultWriter) {
		this.runner = runner;
		this.queryEvaluator = queryEvaluator;
		this.queryResultWriter = queryResultWriter;
		this.queryResultWriter.setQueryTranslator(runner.getQueryTranslator());
	}

	public void translateResult(Collection<SQLQuery> sqlQueries) throws Exception {
		this.queryResultWriter.initalize();
		
		int i=0;
		for(SQLQuery sqlQuery : sqlQueries) {
			AbstractResultSet abstractResultSet = 
					this.queryEvaluator.evaluateQuery(sqlQuery.toString());
			List<String> columnNames = sqlQuery.getSelectItemAliases();
			abstractResultSet.setColumnNames(columnNames);
			
			this.queryResultWriter.setAbstractResultSet(abstractResultSet);
			if(i==0) {
				this.queryResultWriter.preProcess();	
			}
			this.queryResultWriter.process();
			i++;
		}

		if(i > 0) {
			this.queryResultWriter.postProcess();	
		}
	}
	
}