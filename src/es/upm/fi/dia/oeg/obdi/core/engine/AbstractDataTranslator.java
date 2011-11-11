package es.upm.fi.dia.oeg.obdi.core.engine;

import java.util.List;

import es.upm.fi.dia.oeg.obdi.core.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.PostProcessorException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;

public abstract class AbstractDataTranslator {

	protected abstract Object processCustomFunctionTransformationExpression(Object argument) 
	throws PostProcessorException;
	public abstract void setMaterializer(AbstractMaterializer materializer);
	public abstract void processMappingDocument(AbstractMappingDocument mappingDocument) throws Exception;
	
}
