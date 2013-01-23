package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;


public class NameGenerator {
	public String generateName(Node node) {
		String nodeHashCode = (node.hashCode() + "").replaceAll("-", "");//remove negative values
		
		String result = null;
		if(node.isVariable()) {
			result = this.generateName((Var) node);
		} else if(node.isURI()) {
			String localName = node.getLocalName(); 
			result = "uri_" + localName + nodeHashCode;
		} else if(node.isLiteral()) {
			result = "lit_" + nodeHashCode;
		}

		result = result.replaceAll("-", "_"); 
		return result;
	}
	
	public String generateName(Var var) {
		return "var_" + var.getName();
	}
	

}
