package es.upm.fi.dia.oeg.obdi.wrapper;

import java.util.Collection;
import java.util.Set;

import es.upm.fi.dia.oeg.obdi.ILogicalQuery;

public abstract class AbstractUnfolder {
	public abstract Set<String> toSQL(Set<ILogicalQuery> logicalQueries, IMappingDocument mapping) throws Exception;

	public abstract String toSQL(AbstractConceptMapping mapping) throws Exception;
}
