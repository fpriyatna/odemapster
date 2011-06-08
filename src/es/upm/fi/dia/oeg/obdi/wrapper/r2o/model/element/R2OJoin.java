package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;

public class R2OJoin implements R2OElement, Cloneable {
	private Logger logger = Logger.getLogger(R2OJoin.class);
	
	private R2OConditionalExpression joinConditionalExpression;
	private String joinType;
	private boolean defaultJoinUsed;
	private String joinTable;

	public R2OJoin(Element xmlElement) throws ParseException {
		this.parse(xmlElement);
	}
	
	@Override
	public void parse(Element xmlElement) throws ParseException {
		//R2OJoin result = new R2OJoin();

		this.joinType = xmlElement.getAttribute(R2OConstants.JOINS_TYPE_ATTRIBUTE);
		if(this.joinType == null || this.joinType == "") {
			this.defaultJoinUsed = true;
			this.joinType = R2OConstants.JOINS_TYPE_LEFT;
			//throw new ParseException("join-type attribute needs to be defined on joins-via element");
		}

		Element joinToTableElement = 
			XMLUtility.getFirstChildElementByTagName(
					xmlElement, R2OConstants.HAS_TABLE_TAG);
		if(joinToTableElement != null) {
			this.joinTable = joinToTableElement.getTextContent(); 
		}

		Element joinsViaConditionElement = 
			XMLUtility.getFirstElement(xmlElement);
		this.joinConditionalExpression = new R2OConditionalExpression(joinsViaConditionElement);

	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("<" + R2OConstants.JOINS_VIA_TAG + " ");

		if(this.defaultJoinUsed != true) {
			result.append(R2OConstants.JOINS_TYPE_ATTRIBUTE+"=\"" + this.joinType + "\" ");
		}

		result.append(">\n");

		if(this.joinTable != null) {
			result.append(XMLUtility.toOpenTag(R2OConstants.HAS_TABLE_TAG)+ "\n");
			result.append(this.joinTable + "\n");
			result.append(XMLUtility.toCloseTag(R2OConstants.HAS_TABLE_TAG)+ "\n");
		}


		result.append(this.joinConditionalExpression.toString() + "\n");
		result.append(XMLUtility.toCloseTag(R2OConstants.JOINS_VIA_TAG));
		
		return result.toString();
	}

	public R2OConditionalExpression getJoinConditionalExpression() {
		return joinConditionalExpression;
	}

	public String getJoinType() {
		return joinType;
	}

	public void setJoinConditionalExpression(
			R2OConditionalExpression joinConditionalExpression) {
		this.joinConditionalExpression = joinConditionalExpression;
	}

	@Override
	public R2OJoin clone() {
		try {
			return (R2OJoin) super.clone();
		} catch(Exception e) {
			logger.error("Error occured while cloning R2OJoin object.");
			logger.error("Error message = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	


}
