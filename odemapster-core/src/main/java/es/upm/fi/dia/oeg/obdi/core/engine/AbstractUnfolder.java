package es.upm.fi.dia.oeg.obdi.core.engine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import org.apache.log4j.Logger;

import Zql.ZUtils;

import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;

public abstract class AbstractUnfolder {
	private static Logger logger = Logger.getLogger(AbstractUnfolder.class);
	protected ConfigurationProperties properties;

	private AbstractUnfolder() {
		ZUtils.addCustomFunction("concat", 2);
		ZUtils.addCustomFunction("substring", 3);
		ZUtils.addCustomFunction("convert", 2);
		ZUtils.addCustomFunction("coalesce", 2);
		ZUtils.addCustomFunction("abs", 1);
		ZUtils.addCustomFunction("lower", 1);
		ZUtils.addCustomFunction("REPLACE", 3);
		ZUtils.addCustomFunction("TRIM", 1);
	}
	
	public AbstractUnfolder(String configurationDirectory, String configurationFile) {
		this();
		
		try {
			this.properties = new ConfigurationProperties(configurationDirectory, configurationFile);
		} catch (IOException e) {
			logger.error("IO error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			e.printStackTrace();
		} catch (InvalidConfigurationPropertiesException e) {
			logger.error("invalid configuration error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error("Database error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			//e.printStackTrace();
		}
	}
	
	public AbstractUnfolder(ConfigurationProperties properties) {
		this();
		this.properties = properties;
	}
	
	protected abstract Collection<String> unfold(Set<ILogicalQuery> logicalQueries, AbstractMappingDocument mapping) throws Exception;

	public abstract String unfoldConceptMapping(AbstractConceptMapping mapping) throws Exception;
	
	protected abstract Collection<String> unfold(AbstractMappingDocument mappingDocument) throws Exception;

}
