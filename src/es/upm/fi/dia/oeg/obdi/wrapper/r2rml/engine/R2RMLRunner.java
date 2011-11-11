package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractDataTranslator;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractParser;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;

public class R2RMLRunner extends AbstractRunner {

	public R2RMLRunner() {
		this.dataTranslator = new R2RMLElementDataTranslateVisitor();
		this.parser = new R2RMLParser();
	}
	
}
