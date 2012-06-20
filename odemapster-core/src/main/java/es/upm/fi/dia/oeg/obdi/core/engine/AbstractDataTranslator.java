package es.upm.fi.dia.oeg.obdi.core.engine;

import es.upm.fi.dia.oeg.obdi.core.exception.PostProcessorException;
import es.upm.fi.dia.oeg.obdi.core.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;

public abstract class AbstractDataTranslator {
	protected AbstractMaterializer materializer;
	
	protected abstract Object processCustomFunctionTransformationExpression(Object argument) 
			throws PostProcessorException;
	public abstract void setMaterializer(AbstractMaterializer materializer);
	public abstract void translateData(AbstractMappingDocument mappingDocument) throws Exception;
	
}
