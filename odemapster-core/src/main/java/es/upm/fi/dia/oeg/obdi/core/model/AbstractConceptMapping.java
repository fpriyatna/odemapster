package es.upm.fi.dia.oeg.obdi.core.model;

import java.util.Collection;


public abstract class AbstractConceptMapping extends AbstractRDB2RDFMapping implements IConceptMapping {
	public abstract String getConceptName();
	public abstract boolean hasWellDefinedURIExpression();
	public abstract Collection<AbstractPropertyMapping> getPropertyMappings(String propertyURI);
	public abstract Collection<AbstractPropertyMapping> getPropertyMappings();
	public abstract Collection<IRelationMapping> getRelationMappings();
	//public abstract String getLogicalTableAlias();
	//public abstract void setLogicalTableAlias(String logicalTableAlias);
	public abstract boolean isPossibleInstance(String uri);
	public abstract long getLogicalTableSize();
	public abstract Collection<String> getMappedClassURIs();
}
