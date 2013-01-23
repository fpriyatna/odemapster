package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.test;

import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.core.engine.IQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationOptimizer;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLRunner;

public class R2RMLRunnerFreddy extends R2RMLRunner {

	public R2RMLRunnerFreddy(String configurationDirectory, String configurationFile)
			throws Exception {
		super(configurationDirectory, configurationFile);
		
		IQueryTranslator queryTranslator = this.getQueryTranslator();
		IQueryTranslationOptimizer queryTranslationOptimizer = new QueryTranslationOptimizer();
		queryTranslationOptimizer.setSelfJoinElimination(true);
		queryTranslationOptimizer.setUnionQueryReduction(true);
		queryTranslationOptimizer.setSubQueryElimination(true);
		queryTranslator.setOptimizer(queryTranslationOptimizer);
	}

}
