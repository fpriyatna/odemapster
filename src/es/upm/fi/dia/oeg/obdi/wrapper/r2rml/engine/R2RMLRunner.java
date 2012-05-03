package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.exception.R2RMLInvalidRefObjectMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.exception.R2RMLInvalidTriplesMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.exception.R2RMLJoinConditionException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLInvalidTermMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator.R2RMLQueryTranslator;

public class R2RMLRunner extends AbstractRunner {
	private static Logger logger = Logger.getLogger(R2RMLRunner.class);
	
	public R2RMLRunner(String configurationDirectory, String configurationFile) 
			throws Exception {
		//this.dataTranslator = new R2RMLElementDataTranslateVisitor();
		this.parser = new R2RMLParser();
		
		try {
			R2RMLRunner.configurationProperties = new ConfigurationProperties(configurationDirectory, configurationFile);
			
			String mappingDocumentFile = R2RMLRunner.configurationProperties.getMappingDocumentFilePath();
			String queryFilePath = R2RMLRunner.configurationProperties.getQueryFilePath();
			
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
			R2RMLElementUnfoldVisitor unfolder = new R2RMLElementUnfoldVisitor(
					configurationDirectory, configurationFile);

			this.queryTranslator = new R2RMLQueryTranslator(md, unfolder);
			this.queryTranslator.setQueryFilePath(queryFilePath);
			this.dataTranslator = new R2RMLElementDataTranslateVisitor(
							configurationDirectory, configurationFile, unfolder);

		} catch (IOException e) {
			logger.error("IO error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			//e.printStackTrace();
			throw e;
		} catch (InvalidConfigurationPropertiesException e) {
			logger.error("invalid configuration error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			//e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			logger.error("Database error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			//e.printStackTrace();
			throw e;
		} catch(Exception e) {
			logger.error("error message = " + e.getMessage());
			//e.printStackTrace();
			throw e;
			
		}
	}
	
}
