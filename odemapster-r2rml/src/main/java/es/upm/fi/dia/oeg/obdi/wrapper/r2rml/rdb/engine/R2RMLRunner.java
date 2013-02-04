package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractDataTranslator;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;

public class R2RMLRunner extends AbstractRunner {
	private static Logger logger = Logger.getLogger(R2RMLRunner.class);
	 	
	public R2RMLRunner(String configurationDirectory, String configurationFile)
			throws Exception {
		super(configurationDirectory, configurationFile);
//		AbstractParser r2rmlparser = new R2RMLParser();
//		this.parser = r2rmlparser;
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
	protected AbstractDataTranslator createDataTranslator(
			ConfigurationProperties configurationProperties) {
		return new R2RMLElementDataTranslateVisitor(
				configurationProperties);
	}

	@Override
	protected AbstractUnfolder createUnfolder() {
		AbstractUnfolder unfolder = new R2RMLElementUnfoldVisitor();
		unfolder.setDbType(this.configurationProperties.getDatabaseType());
		return unfolder;
	}

	@Override
	protected AbstractMappingDocument createMappingDocument(
			String mappingDocumentFile) throws Exception {
		return new R2RMLMappingDocument(mappingDocumentFile, super.getConnection());
	}

	@Override
	protected IQueryTranslationOptimizer createQueryTranslationOptimizer() {
		return new QueryTranslationOptimizer();
	}

//	@Override
//	public String getQueryTranslatorClassName() {
//		return R2RMLRunner.QUERY_TRANSLATOR_CLASS_NAME;
//	}

}
