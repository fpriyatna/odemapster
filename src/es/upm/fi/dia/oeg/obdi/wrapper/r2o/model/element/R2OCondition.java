package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import Zql.ZSelectItem;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;

public class R2OCondition implements R2OElement, Cloneable{
	private Logger logger = Logger.getLogger(R2OCondition.class);
	
	//(24) condition::= primitive-condition (arg-restriction arg-restrict)*
	private String primitiveCondition;
	private List<R2OArgumentRestriction> argRestricts;
	private String operId;
	
	public R2OCondition(Element element) throws ParseException {
		this.parse(element);
	}
	
	public R2OCondition(String primitiveCondition,
			List<R2OArgumentRestriction> argRestricts, String operId) {
		super();
		this.primitiveCondition = primitiveCondition;
		this.argRestricts = argRestricts;
		this.operId = operId;
	}

	@Override
	public void parse(Element element) throws ParseException {
		//R2OCondition result = new R2OCondition();
		
		this.primitiveCondition = element.getNodeName();
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(this.primitiveCondition)) {
			this.operId = element.getAttribute(R2OConstants.OPER_ID_ATTRIBUTE);
		}
		
		List<Element> argRestrictionElements = XMLUtility.getChildElementsByTagName(
				element, R2OConstants.ARG_RESTRICTION_TAG);
		
		this.argRestricts = new ArrayList<R2OArgumentRestriction>();
		for(int i=0; i<argRestrictionElements.size();i++) {
			Element argRestrictionElement = argRestrictionElements.get(i);
			
			R2OArgumentRestriction argRestrictionObject = new R2OArgumentRestriction(argRestrictionElement);
			this.argRestricts.add(argRestrictionObject);
		}
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(this.primitiveCondition)) {
			result.append("<" + this.primitiveCondition+ " ");
			result.append(R2OConstants.OPER_ID_ATTRIBUTE + "=\"" + this.operId + "\">\n");
		} else {
			result.append("<" + this.primitiveCondition + ">\n");
		}
		
		for(R2OArgumentRestriction argRestrict : this.argRestricts) {
			result.append(argRestrict.toString() + "\n");
		}
		result.append("</" + this.primitiveCondition + ">");
		return result.toString();
	}
	
//	public Collection<String> getInvolvedTables() {
//		
//		Vector<String> result = new Vector<String>();
//		
//		Collection<String> involvedColumns = this.getInvolvedColumns();
//		for(String columnName : involvedColumns) {
//			String tableName = columnName.substring(0, columnName.lastIndexOf("."));
//			result.add(tableName);
//		}
//		
//		return result;
//	}
	
//	public Collection<String> getInvolvedColumns() {
//		Vector<String> result = new Vector<String>();
//		
//		for(R2OArgumentRestriction argRestriction : this.argRestricts) {
//			R2ORestriction restriction = argRestriction.getRestriction();
//			if(restriction instanceof R2OColumnRestriction) {
//				R2OColumnRestriction restrictionColumn = (R2OColumnRestriction) restriction;
//				String columnName = restrictionColumn.getDatabaseColumn().getFullColumnName(); 
//				result.add(columnName);
//			}
//		}
//		return result;
//	}

	public Collection<ZSelectItem> getSelectItems() {
		Collection<ZSelectItem> result = new HashSet<ZSelectItem>();
		
		for(R2OArgumentRestriction argRestriction : this.argRestricts) {
			R2ORestriction restriction = argRestriction.getRestriction();
			Collection<ZSelectItem> restrictionSelectItems = restriction.getSelectItems();
//			logger.debug("restrictionSelectItems = " + restrictionSelectItems);
			result.addAll(restrictionSelectItems);
//			logger.debug("result = " + result);
			/*
			if(restriction instanceof R2OColumnRestriction) {
				R2OColumnRestriction restrictionColumn = (R2OColumnRestriction) restriction;
				String fullColumnName = restrictionColumn.getDatabaseColumn().getFullColumnName();
				ZSelectItem zSelectItem = new ZSelectItem(fullColumnName);
				result.add(zSelectItem);
			}
			*/
		}
		return result;
	}
	
	public String getPrimitiveCondition() {
		return primitiveCondition;
	}

	public String getOperId() {
		return operId;
	}

	public List<R2OArgumentRestriction> getArgRestricts() {
		return argRestricts;
	}	
	
	public R2ORestriction getArgRestricts(String onParam) {
		R2ORestriction result = null;
		
		if(onParam != null) {
			for(R2OArgumentRestriction argRestrict : argRestricts) {
				if(onParam.equals(argRestrict.getOnParam())) {
					result = argRestrict.getRestriction();
				}
			}			
		}

		return result;
	}
	
	public boolean isDelegableCondition() {
		String operationId = null;
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(this.getPrimitiveCondition())) {
			operationId = this.getOperId();
		} else {
			operationId = this.getPrimitiveCondition();
		}

		//R2OProperties r2oProperties = (R2OProperties) super.unfolderProperties;
		if(Utility.inArray(R2ORunner.primitiveOperationsProperties.getDelegableConditionalOperations(), operationId)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected R2OCondition clone() {
		try {
			return (R2OCondition) super.clone();
		} catch(Exception e) {
			logger.error("Error occured while cloning R2OCondition object.");
			logger.error("Error message = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}

	public void setPrimitiveCondition(String primitiveCondition) {
		this.primitiveCondition = primitiveCondition;
	}

	public void setOperId(String operId) {
		this.operId = operId;
	}

	public void setArgRestricts(List<R2OArgumentRestriction> argRestricts) {
		this.argRestricts = argRestricts;
	}
}
