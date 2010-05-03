package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.rdf.model.Model;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractParser;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.ModelWriter;
import es.upm.fi.dia.oeg.obdi.wrapper.QueryEvaluator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.test.RunnerTest;

public class R2ORunner extends AbstractRunner {
	private static Logger logger = Logger.getLogger(RunnerTest.class);

	public void run(String r2oConfigurationFile) throws Exception {
		long start = System.currentTimeMillis();

		Properties r2oProperties = new Properties();
		try {
			r2oProperties.load(new FileInputStream(r2oConfigurationFile));
		} catch(FileNotFoundException e) {
			logger.error("Can't find R2O properties file : " + r2oConfigurationFile);
			throw e;
		}


		logger.info("Loading R2O configuration file " + r2oConfigurationFile);


		int noOfDatabase = Integer.parseInt(r2oProperties.getProperty("no_of_database"));
		if(noOfDatabase != 1) {
			throw new Exception("Only one database is supported.");
		}


		Connection conn = null;
		for(int i=0; i<noOfDatabase;i++) {
			String propertyDatabaseDriver = R2OConstants.DATABASE_DRIVER_PROP_NAME + "[" + i + "]";
			String databaseDriver = r2oProperties.getProperty(propertyDatabaseDriver);

			String propertyDatabaseURL = R2OConstants.DATABASE_URL_PROP_NAME + "[" + i + "]";
			String databaseURL = r2oProperties.getProperty(propertyDatabaseURL);

			String propertyDatabaseName= R2OConstants.DATABASE_NAME_PROP_NAME + "[" + i + "]";
			String databaseName = r2oProperties.getProperty(propertyDatabaseName);

			String propertyDatabaseUser = R2OConstants.DATABASE_USER_PROP_NAME + "[" + i + "]";
			String databaseUser = r2oProperties.getProperty(propertyDatabaseUser);

			String propertyDatabasePassword = R2OConstants.DATABASE_PWD_PROP_NAME  + "[" + i + "]";
			String databasePassword = r2oProperties.getProperty(propertyDatabasePassword);

			conn = Utility.getLocalConnection(
					databaseUser, databasePassword, 
					databaseDriver, 
					databaseURL, "Test R2O Wrapper");
		}

		//initialization
		String r2oAbsolutePath = r2oProperties.getProperty(R2OConstants.R2OFILE_PROP_NAME);
		String outputFilename = r2oProperties.getProperty(R2OConstants.OUTPUTFILE_PROP_NAME);
		String rdfLanguage = "RDF/XML";



		//parsing r2o mapping
		R2OParser parser = new R2OParser(); 
		R2OMappingDocument r2oMappingDocument = (R2OMappingDocument) parser.parse(r2oAbsolutePath);


		//test the parsing result
		parser.testParseResult(r2oAbsolutePath, r2oMappingDocument);


		String jenaMode = r2oProperties.getProperty(R2OConstants.JENA_MODE_TYPE);
		logger.debug("Jena mode = " + jenaMode);
		//			String jenaTDBDir= r2oProperties.getProperty(R2OConstants.JENA_TDB_DIRECTORY);
		//			logger.debug("Jena TDB Directory = " + jenaTDBDir);

		boolean splitOutput = false;
		String splitOutputPerConcept = r2oProperties.getProperty(R2OConstants.SPLIT_OUTPUT_PER_CONCEPT);
		if(splitOutputPerConcept != null) {
			if(splitOutputPerConcept.equalsIgnoreCase("yes") || splitOutputPerConcept.equalsIgnoreCase("true")) {
				splitOutput = true;
			}
		}

		Model model = null;
		Collection<AbstractConceptMapping> conceptMappings = r2oMappingDocument.getConceptMappings();
		long startGeneratingModel = System.currentTimeMillis();
		for(AbstractConceptMapping conceptMapping : conceptMappings) {
			//unfold mapped concepts
			AbstractUnfolder unfolder = new R2OUnfolder(r2oMappingDocument);
			String sqlQuery = unfolder.unfold(conceptMapping);
			logger.debug("sqlQueries for " + conceptMapping.getName() + " = " + sqlQuery);

			//evaluate query
			ResultSet rs = QueryEvaluator.evaluateQuery(sqlQuery, conn);


			//generating model
			R2OModelGenerator modelGenerator = new R2OModelGenerator();
			if(splitOutput) { //if split output is true, then always create a new model and write it to the output
				model = modelGenerator.createModel(jenaMode, conceptMapping.hashCode() + "");

				modelGenerator.generateModel(rs, model, conceptMapping);
				String conceptOutputFilename = outputFilename + "." + conceptMapping.getId();
				int dotLastIndex = outputFilename.lastIndexOf(".");
				if(dotLastIndex == -1) {
					conceptOutputFilename = outputFilename + "-" + conceptMapping.getId() + ".rdf"; 
				} else {
					conceptOutputFilename = outputFilename.substring(0, dotLastIndex) 
					+ "-" + conceptMapping.getId() 
					+ outputFilename.substring(dotLastIndex, outputFilename.length());
				}

				//logger.debug("output file = " + conceptOutputFilename);
				ModelWriter.writeModel(model, conceptOutputFilename, rdfLanguage);
				model.close();
			} else { //if split output is false, then only create the model when it's not null, and write after the looping 
				if(model == null) {
					model = modelGenerator.createModel(jenaMode, r2oMappingDocument.hashCode() + "");
				}
				new R2OModelGenerator().generateModel(rs, model, conceptMapping);
			}


			//cleaning up
			Utility.closeStatement(rs.getStatement());
			Utility.closeRecordSet(rs);
		}
		long endGeneratingModel = System.currentTimeMillis();
		long durationGeneratingModel = (endGeneratingModel-startGeneratingModel) / 1000;
		logger.info("Generating all models time was "+(durationGeneratingModel)+" s.");

		if(!splitOutput) {
			ModelWriter.writeModel(model, outputFilename, rdfLanguage);
			model.close();				
		}



		//cleaning up
		Utility.closeConnection(conn, "r2o wrapper");
		long end = System.currentTimeMillis();
		long duration = (end-start) / 1000;
		logger.info("Execution time was "+(duration)+" s.");
		logger.info("done.\n\n");


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
			runner.run(r2oFile);
		} catch(Exception e) {
			logger.error("Exception occured!");
			logger.error("Error message = " + e.getMessage());
		}


	}
}
