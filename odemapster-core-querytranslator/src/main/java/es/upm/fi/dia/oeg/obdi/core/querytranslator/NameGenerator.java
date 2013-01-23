package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;


public class NameGenerator {
	public static final String PREFIX_URI = "uri_";
	public static final String PREFIX_VAR = "var_";
	public static final String PREFIX_LIT = "lit_";
	
	public String generateName(Node node) {
		String nodeHashCode = (node.hashCode() + "").replaceAll("-", "");//remove negative values
		
		String result = null;
		if(node.isVariable()) {
			result = this.generateName((Var) node);
		} else if(node.isURI()) {
			String localName = node.getLocalName(); 
			result = PREFIX_URI + localName + nodeHashCode;
		} else if(node.isLiteral()) {
			result = PREFIX_LIT + nodeHashCode;
		}

		result = result.replaceAll("-", "_"); 
		return result;
	}
	
	public String generateName(Var var) {
		return PREFIX_VAR + var.getName();
	}
	

}
