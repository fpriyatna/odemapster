package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Restriction.RestrictionType;

public class TransformationExpression extends Expression {
	//	(30) transformation::= primitive-transf (arg-restriction arg-restriction)*
	private String primitiveTransf;
	private String operId;
	private List<ArgumentRestriction> argRestrictions;

	@Override
	public TransformationExpression parse(Element element) throws ParseException {
		TransformationExpression result = new TransformationExpression();
		Element operationElement = XMLUtility.getFirstElement(element);
		String operationElementNodeName = operationElement.getNodeName();
		result.primitiveTransf = operationElementNodeName;

		if(R2OConstants.OPERATION_TAG.equalsIgnoreCase(operationElementNodeName)) {
			result.operId = operationElement.getAttribute(R2OConstants.OPER_ID_ATTRIBUTE);
		}


		result.argRestrictions = new ArrayList<ArgumentRestriction>();
		NodeList argumentRestrictionsNodeList = operationElement.getElementsByTagName(R2OConstants.ARG_RESTRICTION_TAG);
		for(int i=0; i<argumentRestrictionsNodeList.getLength(); i++) {
			Element argumentRestrictionElement = (Element) argumentRestrictionsNodeList.item(i);
			ArgumentRestriction argumentRestictionObject = new ArgumentRestriction().parse(argumentRestrictionElement);
			result.argRestrictions.add(argumentRestictionObject);
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		if(R2OConstants.OPERATION_TAG.equalsIgnoreCase(this.primitiveTransf)) {
			result.append("<" + this.primitiveTransf + " ");
			result.append(R2OConstants.OPER_ID_ATTRIBUTE + "=\"" + this.operId + "\">\n");
		} else {
			result.append("<" + this.primitiveTransf + ">\n");
		}

		for(ArgumentRestriction argRestriction : argRestrictions) {
			result.append(argRestriction.toString() + "\n");
		}
		result.append("</" + this.primitiveTransf + ">");
		return result.toString();
	}
	
	public Collection<String> getInvolvedColumns() {
		Vector<String> result = new Vector<String>();
		
		for(ArgumentRestriction argRestriction : argRestrictions) {
			Restriction restriction = argRestriction.getRestriction();
			RestrictionType restrictionType = restriction.getRestrictionType();
			if(restrictionType == RestrictionType.HAS_COLUMN) {
				result.add(restriction.getHasColumn());
			}
		}
		return result;
	}

	public Vector<String> getInvolvedTables() {
		Vector<String> result = new Vector<String>();
		
		for(ArgumentRestriction argRestriction : argRestrictions) {
			Restriction restriction = argRestriction.getRestriction();
			RestrictionType restrictionType = restriction.getRestrictionType();
			if(restrictionType == RestrictionType.HAS_COLUMN) {
				String columnName = restriction.getHasColumn(); 
				String tableName = columnName.substring(0, columnName.lastIndexOf("."));
				result.add(tableName);
			}
		}
		return result;
	}

	public String getPrimitiveTransf() {
		return primitiveTransf;
	}

	public List<ArgumentRestriction> getArgRestrictions() {
		return argRestrictions;
	}

	public String getOperId() {
		return operId;
	}

}
