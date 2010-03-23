package es.upm.fi.dia.oeg.obdi;

import java.util.Set;

public abstract class SQLizer {
	protected abstract Set<String> toSQL(Set<ILogicalQuery> logicalQueries, IMapping mapping);

}
