package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import com.hp.hpl.jena.graph.Node;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.SPARQL2SQLTranslator.POS;

public class NameGenerator {
	public String generateName(Node node) {
		if(node.isVariable()) {
			return node.getName();
		} else if(node.isURI()) {
			String localName = node.getLocalName(); 
			return localName;
		} 

		return null;
	}
}
