package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidRelationMappingException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseView;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OJoin;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;


public class R2ORelationMapping extends R2OPropertyMapping 
implements R2OElement, IRelationMapping, Cloneable
{
	private static Logger logger = Logger.getLogger(R2ORelationMapping.class);

	private String toConcept;
	private String rangeTableAlias;
	private Vector<R2ODatabaseTable> hasTables;
	private Collection<R2OSelector> rmSelectors;
	private R2OJoin joinsVia;
	private R2ODatabaseView hasView;
	private String rangeURIAlias;
	
	public R2ORelationMapping(Element rmElement, R2OConceptMapping parent) throws ParseException {
		super(parent);
		this.parse(rmElement);
	}
	
	@Override
	public void parse(Element rmElement) throws ParseException {
		int noOfBases = 0;

		//R2ORelationMapping result = new R2ORelationMapping();
		this.name = rmElement.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		logger.info("Parsing attribute " + this.name);
		
		//parse identifiedBy attribute
		this.id = rmElement.getAttribute(R2OConstants.IDENTIFIED_BY_ATTRIBUTE);


		this.toConcept = rmElement.getAttribute(R2OConstants.TO_CONCEPT_ATTRIBUTE);
		if(this.toConcept == "") { this.toConcept = null; }
		if(this.toConcept != null) { noOfBases++;}

		this.rangeTableAlias = rmElement.getAttribute(R2OConstants.ALIAS_ATTRIBUTE);
		if(this.rangeTableAlias == "") { this.rangeTableAlias = null; }
		
		//parse has-intermediate-table
		List<Element> hasIntermediateTable = XMLUtility.getChildElementsByTagName(
				rmElement, R2OConstants.HAS_TABLE_TAG);
		if(hasIntermediateTable != null && hasIntermediateTable.size() > 0) {
			if(hasIntermediateTable.size() > 1) {
				throw new InvalidRelationMappingException("Multiple tables is not implemented yet!");
			}
			this.hasTables = new Vector<R2ODatabaseTable>();
			for(Element hasTableElement : hasIntermediateTable) {
				R2ODatabaseTable dbTable = new R2ODatabaseTable(hasTableElement);
				this.hasTables.add(dbTable);
			}
		} 

		List<Element> rmSelectorsElements = XMLUtility.getChildElementsByTagName(rmElement, R2OConstants.SELECTOR_TAG);
		if(rmSelectorsElements != null) { noOfBases++;}

		if(noOfBases == 0) {
			String errorMessage = "Error parsing " + this.name + " ,specify either toConcept+joinsVia or selector!";
			logger.error(errorMessage);

			throw new InvalidRelationMappingException(errorMessage);			
		} else if (noOfBases > 1) {
			String errorMessage = "Specify only one of toConcept or selector!";
			logger.error(errorMessage);
			throw new InvalidRelationMappingException(errorMessage);			
		}

		Element joinsViaElement = 
			XMLUtility.getFirstChildElementByTagName(rmElement, R2OConstants.JOINS_VIA_TAG);
		if(joinsViaElement != null) {
			this.joinsVia = new R2OJoin(joinsViaElement);
		}

		if(rmSelectorsElements != null) {
			this.rmSelectors = new ArrayList<R2OSelector>();
			for(Element childElement : rmSelectorsElements) {
				R2OSelector selector = new R2OSelector(childElement);
				this.rmSelectors.add(selector);
			}						
		}

		Element hasViewElement = XMLUtility.getFirstChildElementByTagName(
				rmElement, R2OConstants.HAS_VIEW_TAG);
		if(hasViewElement != null) {
			this.hasView = new R2ODatabaseView(hasViewElement);
		}
	}

	@Override
	public String getRelationName() {
		return this.name;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("<" + R2OConstants.DBRELATION_DEF_TAG + " ");
		result.append(R2OConstants.NAME_ATTRIBUTE+"=\"" + this.name + "\" ");
		if(this.toConcept != null) {
			result.append(R2OConstants.TO_CONCEPT_ATTRIBUTE +"=\"" + this.toConcept + "\" ");
		}

		if(this.rangeTableAlias != null) {
			result.append(R2OConstants.ALIAS_ATTRIBUTE  +"=\"" + this.rangeTableAlias + "\" ");
		}

		if(this.id != null && this.id != "") {
			result.append(R2OConstants.IDENTIFIED_BY_ATTRIBUTE +"=\"" + this.id + "\" ");
		}
		
		
		result.append(">\n");

		if(this.hasView != null) {
			result.append(this.hasView.toString() + "\n");
		}

		if(this.hasTables != null && this.hasTables.size() > 0) {
			for(R2ODatabaseTable hasIntermediateTable : this.hasTables) {
				result.append(hasIntermediateTable.toString() + "\n");
			}
		}

		if(this.joinsVia != null) {
			result.append(this.joinsVia.toString()  + "\n");

		}


		if(this.rmSelectors != null) {
			for(R2OSelector selector : this.rmSelectors) {
				result.append(selector.toString() + "\n");
			}
		}

		result.append(XMLUtility.toCloseTag(R2OConstants.DBRELATION_DEF_TAG)+ "\n");
		return result.toString();
	}

	public R2OJoin getJoinsVia() {
		return joinsVia;
	}

	public String getToConcept() {
		return toConcept;
	}

	public Collection<R2OSelector> getRmSelectors() {
		return rmSelectors;
	}

	public Vector<R2ODatabaseTable> getHasTables() {
		return hasTables;
	}

	public R2ODatabaseView getHasView() {
		return hasView;
	}

	public void setJoinsVia(R2OJoin joinsVia) {
		this.joinsVia = joinsVia;
	}

	@Override
	public R2ORelationMapping clone(){
		try {
			R2ORelationMapping result = (R2ORelationMapping) super.clone();
			//regenerate range uri alias
			result.rangeURIAlias = R2OConstants.RELATIONMAPPING_ALIAS + new Random().nextInt(10000);
			return result;
		} catch(Exception e) {
			logger.error("Error occured while cloning R2ORelationMapping object.");
			logger.error("Error message = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		
	}


	public String generateRangeURIAlias() {
		if(this.rangeURIAlias == null) {
			this.rangeURIAlias = R2OConstants.RELATIONMAPPING_ALIAS + new Random().nextInt(10000);
		}
		return this.rangeURIAlias;
	}

	public String getRangeTableAlias() {
		return rangeTableAlias;
	}

}
