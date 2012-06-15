package es.upm.fi.dia.oeg.obdi.core.engine;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;

public abstract class AbstractUnfolder {

	protected abstract Collection<String> unfold(Set<ILogicalQuery> logicalQueries, AbstractMappingDocument mapping) throws Exception;

	public abstract String unfoldConceptMapping(AbstractConceptMapping mapping) throws Exception;
	
	protected abstract Collection<String> unfold(AbstractMappingDocument mappingDocument) throws Exception;

}
