package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractQueryResultWriter;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;

public class R2RMLRunner extends AbstractRunner {
	private static Logger logger = Logger.getLogger(R2RMLRunner.class);
	 	
	public R2RMLRunner() {
		super();
	}
	
	public R2RMLRunner(String configurationDirectory, String configurationFile)
			throws Exception {
		super(configurationDirectory, configurationFile);
	}

	public static void main(String args[]) {
		try {
			if(args == null || args.length == 0 || args.length != 2) {
				logger.info("usage R2RMLRunner propertiesDirectory propertiesFile");
				System.exit(-1);
			}
			
			//String configurationDirectory = System.getProperty("user.dir");
			String configurationDirectory = args[0];
			logger.info("propertiesDirectory = " + configurationDirectory);
			
			String configurationFile = args[1];
			logger.info("propertiesFile = " + configurationFile);
			
			AbstractRunner runner = new R2RMLRunner(configurationDirectory, configurationFile);
			runner.run();
		} catch(Exception e) {
			logger.error("Exception occured!");
			logger.error("Error message = " + e.getMessage());
		}
	}
	
	@Override
	protected void createDataTranslator(
			ConfigurationProperties configurationProperties) {
		super.dataTranslator = new R2RMLElementDataTranslateVisitor(
				configurationProperties);
	}

	@Override
	protected AbstractUnfolder createUnfolder() {
		AbstractUnfolder unfolder = new R2RMLElementUnfoldVisitor();
		unfolder.setDbType(this.configurationProperties.getDatabaseType());
		return unfolder;
	}

	@Override
	public void readMappingDocumentFile(
			String mappingDocumentFile) throws Exception {
		super.mappingDocument = new R2RMLMappingDocument(mappingDocumentFile, configurationProperties);
	}


//	@Override
//	public String getQueryTranslatorClassName() {
//		return R2RMLRunner.QUERY_TRANSLATOR_CLASS_NAME;
//	}

}
