package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class TransformationExpression extends Expression {
	//	(30) transformation::= primitive-transf (arg-restriction arg-restriction)*
	private String primitiveTransf;
	private String operId;
	private List<ArgumentRestriction> argRestrictions;

	@Override
	R2OElement parse(Element element) throws R2OParserException {
		TransformationExpression result = new TransformationExpression();
		Element operationElement = XMLUtility.getFirstElement(element);
		String operationElementNodeName = operationElement.getNodeName();
		result.primitiveTransf = operationElementNodeName;

		if(Constants.OPERATION_TAG.equalsIgnoreCase(operationElementNodeName)) {
			result.operId = operationElement.getAttribute(Constants.OPER_ID_ATTRIBUTE);
		}


		result.argRestrictions = new ArrayList<ArgumentRestriction>();
		NodeList argumentRestrictionsNodeList = operationElement.getElementsByTagName(Constants.ARG_RESTRICTION_TAG);
		for(int i=0; i<argumentRestrictionsNodeList.getLength(); i++) {
			Element argumentRestrictionElement = (Element) argumentRestrictionsNodeList.item(i);
			ArgumentRestriction argumentRestictionObject = (ArgumentRestriction) new ArgumentRestriction().parse(argumentRestrictionElement);
			result.argRestrictions.add(argumentRestictionObject);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		if(Constants.OPERATION_TAG.equalsIgnoreCase(this.primitiveTransf)) {
			result.append("<" + this.primitiveTransf + " ");
			result.append(Constants.OPER_ID_ATTRIBUTE + "=\"" + this.operId + "\">\n");
		} else {
			result.append("<" + this.primitiveTransf + ">\n");
		}

		for(ArgumentRestriction argRestriction : argRestrictions) {
			result.append(argRestriction.toString() + "\n");
		}
		result.append("</" + this.primitiveTransf + ">");
		return result.toString();
	}


}
