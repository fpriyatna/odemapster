package es.upm.fi.dia.oeg.obdi.core.engine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

import Zql.ZUtils;

import es.upm.fi.dia.oeg.obdi.core.Constants;
import es.upm.fi.dia.oeg.obdi.core.ILogicalQuery;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;

public abstract class AbstractUnfolder {
	private static Logger logger = Logger.getLogger(AbstractUnfolder.class);
	//protected ConfigurationProperties properties;
	protected String dbType = Constants.DATABASE_MYSQL;
	
	protected AbstractUnfolder() {
		ZUtils.addCustomFunction("concat", 2);
		ZUtils.addCustomFunction("substring", 3);
		ZUtils.addCustomFunction("convert", 2);
		ZUtils.addCustomFunction("coalesce", 2);
		ZUtils.addCustomFunction("abs", 1);
		ZUtils.addCustomFunction("lower", 1);
		ZUtils.addCustomFunction("REPLACE", 3);
		ZUtils.addCustomFunction("TRIM", 1);
	}
	

	
	protected abstract Collection<String> unfold(Set<ILogicalQuery> logicalQueries, AbstractMappingDocument mapping) throws Exception;

	public abstract String unfoldConceptMapping(AbstractConceptMapping mapping) throws Exception;
	
	protected abstract Collection<String> unfold(AbstractMappingDocument mappingDocument) throws Exception;

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

}
