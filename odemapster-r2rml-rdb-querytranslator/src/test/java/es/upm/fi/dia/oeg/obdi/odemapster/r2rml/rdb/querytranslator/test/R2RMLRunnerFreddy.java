package es.upm.fi.dia.oeg.obdi.odemapster.r2rml.rdb.querytranslator.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLRunner;

public class R2RMLRunnerFreddy extends R2RMLRunner {
	private static Logger logger = Logger.getLogger(R2RMLRunnerFreddy.class);

	public R2RMLRunnerFreddy(String configurationDirectory, String configurationFile)
			throws Exception {
		super(configurationDirectory, configurationFile);
		this.buildQueryTranslator();
		IQueryTranslator queryTranslator = this.getQueryTranslator();
		IQueryTranslationOptimizer queryTranslationOptimizer = new QueryTranslationOptimizer();
		queryTranslationOptimizer.setSelfJoinElimination(true);
		queryTranslationOptimizer.setUnionQueryReduction(true);
		queryTranslationOptimizer.setSubQueryElimination(true);
		queryTranslator.setOptimizer(queryTranslationOptimizer);
	}

	public void runFreddy(String testName) {
		logger.info("------ Running " + testName + " Freddy ------");
		String configurationFile = testName + ".r2rml.properties";
		long start = System.currentTimeMillis();
		
		try {
			IQueryTranslator queryTranslator = this.getQueryTranslator();
			this.run();
			long end = System.currentTimeMillis();
			logger.info("test execution time was "+(end-start)+" ms.");
			logger.info("------" + testName + " Freddy DONE------");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}
}
