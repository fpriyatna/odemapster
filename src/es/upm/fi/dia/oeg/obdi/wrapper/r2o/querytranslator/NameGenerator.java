package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.SPARQL2SQLTranslator.POS;

public class NameGenerator {
	public String generateName(Triple tp, Node node) {
		String nodeHashCode = (node.hashCode() + "").replaceAll("-", "");
		String result = null;
		if(node.isVariable()) {
			result = this.generateName(tp, (Var) node);
		} else if(node.isURI()) {
			String localName = node.getLocalName(); 
			result = "uri_" + localName + nodeHashCode;
		} else if(node.isLiteral()) {
			result = "lit_" + nodeHashCode;
		}

		 
		return result;
	}
	
	public String generateName(Triple tp, Var var) {
		return "var_" + var.getName();
	}
}
