package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import Zql.ZConstant;
import Zql.ZSelectItem;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OPrimitiveOperationsProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;

public class R2OTransformationExpression extends R2OExpression implements Cloneable {
	private R2OPrimitiveOperationsProperties primitiveOperationsProperties;
	
	//	(30) transformation::= primitive-transf (arg-restriction arg-restriction)*
	private String primitiveTransf;
	private String operId;
	private String datatype;
	private String isCollection;
	private String functionName;
	private List<R2OArgumentRestriction> argRestrictions;
	private boolean autoGeneratedDatatype = false;
	
	public R2OTransformationExpression(Element element) throws ParseException {
		this.parse(element);
	}
	
	public R2OTransformationExpression(String operId) {
		this.primitiveTransf = R2OConstants.OPERATION_TAG;
		this.operId = operId;		
	}

	@Override
	public void parse(Element element) throws ParseException {
		//R2OTransformationExpression result = new R2OTransformationExpression();
		Element operationElement = XMLUtility.getFirstElement(element);
		String operationElementNodeName = operationElement.getNodeName();
		this.primitiveTransf = operationElementNodeName;

		if(R2OConstants.OPERATION_TAG.equalsIgnoreCase(operationElementNodeName)) {
			this.operId = operationElement.getAttribute(R2OConstants.OPER_ID_ATTRIBUTE);
		}

		this.datatype = operationElement.getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
		if(this.datatype == null || this.datatype == "") {
			if(this.operId.equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_CONCAT)) {
				this.datatype = R2OConstants.DATATYPE_STRING;
				autoGeneratedDatatype = true;
			} else if(this.operId.equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_SUBSTRING)) {
				this.datatype = R2OConstants.DATATYPE_STRING;
				autoGeneratedDatatype = true;
			}
		}
		
		this.functionName = operationElement.getAttribute(R2OConstants.FUNCTION_NAME_ATTRIBUTE);
		if(this.functionName == "") { this.functionName = null; }
		
		this.isCollection = operationElement.getAttribute(R2OConstants.IS_COLLECTION_ATTRIBUTE);
		if(this.isCollection == "") { this.isCollection = null; }
		
		this.argRestrictions = new ArrayList<R2OArgumentRestriction>();
		List<Element> argumentRestrictionsNodeList = XMLUtility.getChildElementsByTagName(operationElement, R2OConstants.ARG_RESTRICTION_TAG);
		for(int i=0; i<argumentRestrictionsNodeList.size(); i++) {
			Element argumentRestrictionElement = argumentRestrictionsNodeList.get(i);
			R2OArgumentRestriction argumentRestrictionObject = new R2OArgumentRestriction(argumentRestrictionElement);
			this.argRestrictions.add(argumentRestrictionObject);
		}
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		if(R2OConstants.OPERATION_TAG.equalsIgnoreCase(this.primitiveTransf)) {
			result.append("<" + this.primitiveTransf + " ");
			result.append(R2OConstants.OPER_ID_ATTRIBUTE + "=\"" + this.operId + "\"");
		} else {
			result.append("<" + this.primitiveTransf);
		}

		if(this.datatype != null && this.datatype != "" && !this.autoGeneratedDatatype) {
			result.append(" " + R2OConstants.DATATYPE_ATTRIBUTE + "=\"" + this.datatype + "\"");
		}

		if(this.isCollection != null && this.isCollection != "") {
			result.append(" " + R2OConstants.IS_COLLECTION_ATTRIBUTE + "=\"" + this.isCollection + "\"");
		}

		if(this.functionName != null && this.functionName != "") {
			result.append(" " + R2OConstants.FUNCTION_NAME_ATTRIBUTE + "=\"" + this.functionName + "\"");
		}

		result.append(">\n");
		
		if(this.argRestrictions != null) {
			for(R2OArgumentRestriction argRestriction : argRestrictions) {
				result.append(argRestriction.toString() + "\n");
			}			
		}

		result.append("</" + this.primitiveTransf + ">");
		return result.toString();
	}
	
	public Collection<ZSelectItem> getSelectItems() {
		Collection<ZSelectItem> result = new HashSet<ZSelectItem>();
		
		for(R2OArgumentRestriction argRestriction : this.argRestrictions) {
			R2ORestriction restriction = argRestriction.getRestriction();
			result.addAll(restriction.getSelectItems());
		}
			
		return result;
	}

	public Vector<String> getInvolvedTables() {
		Vector<String> result = new Vector<String>();
		
		for(R2OArgumentRestriction argRestriction : argRestrictions) {
			R2ORestriction restriction = argRestriction.getRestriction();
			if(restriction instanceof R2OColumnRestriction) {
				R2OColumnRestriction restrictionColumn = (R2OColumnRestriction) restriction;
				String columnName = restrictionColumn.getDatabaseColumn().getFullColumnName();				
				String tableName = columnName.substring(0, columnName.lastIndexOf("."));
				result.add(tableName);
			}
		}
		return result;
	}

	public String getPrimitiveTransf() {
		return primitiveTransf;
	}

	public List<R2OArgumentRestriction> getArgRestrictions() {
		return argRestrictions;
	}

	public String getOperId() {
		return operId;
	}

	public String getDatatype() {
		return datatype;
	}

	public String getIsCollection() {
		return isCollection;
	}

	public boolean isDelegableTransformationExpression() {
		String operator = this.getOperId();

		//if the root operator is not delegable, then return false
		if(!Utility.inArray(R2ORunner.primitiveOperationsProperties.getDelegableTransformationOperations(), operator)) {
			return false;
		}



		//if one of the arguments has non delegable transformation, then return false
		for(R2OArgumentRestriction argRestriction : this.getArgRestrictions()) {
			R2ORestriction restriction = argRestriction.getRestriction();
			if(restriction instanceof R2OTransformationRestriction) {
				R2OTransformationRestriction rt = (R2OTransformationRestriction) restriction;
				R2OTransformationExpression restrictionValueTransformation = 
					(R2OTransformationExpression) rt.getTransformationExpression();
				if(!restrictionValueTransformation.isDelegableTransformationExpression()) {
					return false;
				}
			}
		}

		return true;
	}

	public String getFunctionName() {
		return functionName;
	}
	

	public R2ORestriction getLastRestriction() {
		return this.argRestrictions.get(this.argRestrictions.size()-1).getRestriction();
	}

	
	public void addRestriction(R2ORestriction restriction) {
		if(this.argRestrictions == null) {
			this.argRestrictions = new ArrayList<R2OArgumentRestriction>();
		}
		
		this.argRestrictions.add(new R2OArgumentRestriction(restriction));
	}
}
