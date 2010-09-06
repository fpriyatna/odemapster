package es.upm.fi.dia.oeg.obdi.wrapper;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import es.upm.fi.dia.oeg.obdi.ILogicalQuery;

public abstract class AbstractUnfolder {
	protected Properties unfolderProperties;
	
	protected abstract Set<String> unfold(Set<ILogicalQuery> logicalQueries, IMappingDocument mapping) throws Exception;

	public abstract String unfoldConceptMapping(AbstractConceptMapping mapping) throws Exception;
	
	protected abstract String unfold(IMappingDocument mapping) throws Exception;

	public void setUnfolderProperties(Properties properties) {
		this.unfolderProperties = properties;
	}
}
