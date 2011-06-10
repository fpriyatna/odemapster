package es.upm.fi.dia.oeg.obdi.wrapper;


public abstract class AbstractParser {
	public abstract IMappingDocument parse(Object mappingResource) throws Exception; 
}
