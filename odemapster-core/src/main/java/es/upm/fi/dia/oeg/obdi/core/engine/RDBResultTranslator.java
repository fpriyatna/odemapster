package es.upm.fi.dia.oeg.obdi.core.engine;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;

public class RDBResultTranslator extends AbstractResultTranslator {
	private static Logger logger = Logger.getLogger(RDBResultTranslator.class);

	public RDBResultTranslator(AbstractRunner runner) {
		super(runner);
	}

	@Override
	protected void generateResultBindingElement(SQLQuery query, Document xmlDoc
			, Element resultsElement, List<String> headElementsString) throws Exception {
		int timeout = super.runner.configurationProperties.getDatabaseTimeout();
		
		String resultString = "result";
		String bindingString = "binding";
		int i=0;
		ResultSet rs = DBUtility.executeQuery(
				super.runner.getConnection(), query.toString(), timeout);
		while(rs.next()) {
			Element resultElement = xmlDoc.createElement(resultString);
			resultsElement.appendChild(resultElement);
			Iterator<String> headElementsStringIterator = headElementsString.iterator(); 
			while(headElementsStringIterator.hasNext()) {
				String columnLabel = headElementsStringIterator.next();
				Element bindingElement = xmlDoc.createElement(bindingString);
				bindingElement.setAttribute("name", columnLabel);
				String dbValue = rs.getString(columnLabel);
				dbValue = super.runner.queryTranslator.translateResultSet(columnLabel, dbValue);
				bindingElement.setTextContent(dbValue);
				resultElement.appendChild(bindingElement);
			}
			i++;
		}
		String status = i  + " instance(s) retrieved ";
		logger.info(status);
	}

}
