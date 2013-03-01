package es.upm.fi.dia.oeg.obdi.core.engine;

//import java.sql.ResultSetMetaData;
//import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public class DefaultResultProcessor {
	private static Logger logger = Logger.getLogger(DefaultResultProcessor.class);
	//AbstractRunner runner;
	AbstractDataSourceReader queryEvaluator;
	AbstractQueryResultWriter queryResultWriter;
	
	public DefaultResultProcessor(AbstractDataSourceReader queryEvaluator, AbstractQueryResultWriter queryResultWriter) {
		this.queryEvaluator = queryEvaluator;
		this.queryResultWriter = queryResultWriter;
	}

	public void translateResult(Collection<SQLQuery> sqlQueries) throws Exception {
		this.queryResultWriter.initialize();
		
		int i=0;
		for(SQLQuery sqlQuery : sqlQueries) {
			AbstractResultSet abstractResultSet = 
					this.queryEvaluator.evaluateQuery(sqlQuery.toString());
			ArrayList<String> columnNames = sqlQuery.getSelectItemAliases();
			abstractResultSet.setColumnNames(columnNames);
			
			this.queryResultWriter.setResultSet(abstractResultSet);
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
