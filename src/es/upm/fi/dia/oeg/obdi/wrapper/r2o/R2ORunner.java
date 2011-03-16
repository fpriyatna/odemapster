package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractParser;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.ModelWriter;
import es.upm.fi.dia.oeg.obdi.wrapper.QueryEvaluator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.translator.SPARQL2MappingTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.RelationMappingUnfolderException;

public class R2ORunner extends AbstractRunner {
	private static Logger logger = Logger.getLogger(R2ORunner.class);
	private static final String PRIMITIVE_OPERATIONS_FILE = "primitiveOperations.cfg";
	public static R2OConfigurationProperties configurationProperties;
	public static R2OPrimitiveOperationsProperties primitiveOperationsProperties;
	private R2OPostProcessor postProcessor; 	

	public R2ORunner() {
		this.postProcessor = new R2ODefaultPostProcessor();
	}

	public R2ORunner(R2OPostProcessor postProcessor) {
		this.postProcessor = postProcessor;
	}

	private R2OConfigurationProperties loadConfigurationFile(
			String mappingDirectory, String r2oConfigurationFile) 
	throws IOException, SQLException, InvalidConfigurationPropertiesException {
		logger.info("Active Directory = " + mappingDirectory);
		logger.info("Loading R2O configuration file : " + r2oConfigurationFile);
		
		try {
			R2OConfigurationProperties r2oProperties = 
				new R2OConfigurationProperties(mappingDirectory, r2oConfigurationFile);
			return r2oProperties;
		} catch(FileNotFoundException e) {
			logger.error("Can't find R2O properties file : " + r2oConfigurationFile);
			throw e;
		}
	}
	
	private R2OPrimitiveOperationsProperties loadPrimitiveOperationsFile(String primitiveOperationsFile) throws Exception{
		logger.debug("Loading R2O operations file : " + primitiveOperationsFile);
		try {
			R2OPrimitiveOperationsProperties primitiveOperationsProperties = 
				new R2OPrimitiveOperationsProperties(primitiveOperationsFile);
			return primitiveOperationsProperties;
		} catch(Exception e) {
			logger.error("Error loading primitive operations file : " + PRIMITIVE_OPERATIONS_FILE);
			throw e;
		}
	}

	
	public void run(String mappingDirectory, String r2oConfigurationFile) throws Exception {
		long start = System.currentTimeMillis();

		//loading operations file
		R2ORunner.primitiveOperationsProperties = 
			this.loadPrimitiveOperationsFile(PRIMITIVE_OPERATIONS_FILE);

		//Loading R2O configuration file
		R2ORunner.configurationProperties = 
			this.loadConfigurationFile(mappingDirectory, r2oConfigurationFile);


		
		//parsing r2o mapping document
		R2OParser parser = new R2OParser(); 
		R2OMappingDocument originalMappingDocument = 
			(R2OMappingDocument) parser.parse(configurationProperties.getR2oFilePath());
		
		//test the parsing result
		parser.testParseResult(configurationProperties.getR2oFilePath(), originalMappingDocument);


		
		//parsing sparql file
		String queryFilePath = configurationProperties.getQueryFilePath();
		R2OMappingDocument translationResultMappingDocument = null;
		if(queryFilePath != null && !queryFilePath.equals("")) {
			logger.info("Parsing query file : " + queryFilePath);
			SPARQL2MappingTranslator translator = 
				new SPARQL2MappingTranslator(originalMappingDocument);
			Query query = QueryFactory.read(queryFilePath);
			translationResultMappingDocument = translator.processQuery(query);
			//logger.debug("translationResult = " + translationResultMappingDocument);
		}
		
		R2OMappingDocument mappingDocument;
		if(translationResultMappingDocument == null) {
			mappingDocument = originalMappingDocument;
		} else {
			mappingDocument = translationResultMappingDocument;
		}
		
		String outputFileName = configurationProperties.getOutputFilePath();

		//preparing output file
//		FileWriter fileWriter = new FileWriter(outputFileName);
	    FileOutputStream fileOut = new FileOutputStream (outputFileName);
	    OutputStreamWriter out = new OutputStreamWriter (fileOut, "UTF-8");
	    
		Model model = null;
		Collection<AbstractConceptMapping> conceptMappings = 
			mappingDocument.getConceptMappings();
		long startGeneratingModel = System.currentTimeMillis();
		for(AbstractConceptMapping conceptMapping : conceptMappings) {
			try {
				//unfold mapped concepts
				logger.info("Unfolding concept " + conceptMapping.getConceptName());
				R2OUnfolder unfolder = 
					new R2OUnfolder(mappingDocument, primitiveOperationsProperties, configurationProperties);
				String sqlQuery = unfolder.unfoldConceptMapping(conceptMapping);
				if(sqlQuery != null) {
					//evaluate query
					ResultSet rs = QueryEvaluator.evaluateQuery(sqlQuery, configurationProperties.getConn());

					if(model == null) {
						model = postProcessor.createModel(
								configurationProperties.getJenaMode(), mappingDocument.hashCode() + "");
					}
//					postProcessor.processConceptMapping(rs, model, conceptMapping, fileWriter);
					postProcessor.processConceptMapping(rs, model, conceptMapping, out);

					//cleaning up
					Utility.closeStatement(rs.getStatement());
					Utility.closeRecordSet(rs);					
				}


			} catch(SQLException e) {
				String errorMessage = "Error processing " + conceptMapping.getName();
				logger.error(errorMessage);				
				throw e;
			} catch(RelationMappingUnfolderException e) {
				String errorMessage = "Error processing " + conceptMapping.getName();
				logger.error(errorMessage);				
				throw e;				
			} catch(Exception e) {
				//e.printStackTrace();
				String errorMessage = "Error processing " + conceptMapping.getName() + " because " + e.getMessage();
				logger.error(errorMessage);
				throw e;
			}

		}
		long endGeneratingModel = System.currentTimeMillis();
		long durationGeneratingModel = (endGeneratingModel-startGeneratingModel) / 1000;
		logger.info("Post Processing all concepts time was "+(durationGeneratingModel)+" s.");

		if(model == null) {
			logger.warn("Model was empty!");
		} else {
			if(configurationProperties.getRdfLanguage().equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_NTRIPLE)) {
				//done
			} else {
				ModelWriter.writeModelStream(model, configurationProperties.getOutputFilePath(), configurationProperties.getRdfLanguage());
				model.close();				
			}
				
				
		}


		//cleaning up
		try {
//			fileWriter.flush();
//			fileWriter.close();
			out.flush(); out.close();
			fileOut.flush(); fileOut.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			
		}

		Utility.closeConnection(configurationProperties.getConn(), "r2o wrapper");
		long end = System.currentTimeMillis();
		long duration = (end-start) / 1000;
		logger.info("Execution time was "+(duration)+" s.");
		logger.info("done.");


	}

	public static void main(String args[]) {
		try {
			R2ORunner runner = new R2ORunner();
			String r2oFile = null;
			if(args == null || args.length == 0) {
				r2oFile = "r2o.properties";
			} else {
				r2oFile = args[0];
			}
			runner.run(null, r2oFile);
		} catch(Exception e) {
			logger.error("Exception occured!");
			logger.error("Error message = " + e.getMessage());
		}


	}
}
