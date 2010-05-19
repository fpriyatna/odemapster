package es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationExpression;

public class R2OConceptMapping extends AbstractConceptMapping implements R2OElement {


	//	(18) conceptmapping-definition::= conceptmap-def name
	//    identified-by+
	//    (uri-as selector)?
	//    (applies-if cond-expr)?
	//    (joins-via concept-join-expr)?
	//    documentation?
	//    (described-by propertymap-def)*

	//	(18) conceptmapping-definition::= conceptmap-def name
	//    (has-table literal)*		
	//    identified-by+
	//    (uri-as selector)?
	//    (applies-if cond-expr)?
	//    (joins-via concept-join-expr)?
	//    documentation?
	//    (described-by propertymap-def)*
	private Vector<R2ODatabaseTable> hasTables;
	private R2OSelector selectorURIAs;//original version
	private R2OTransformationExpression transformationExpressionURIAs;//odemapster 1 version
	private List<R2OPropertyMapping> describedBy;
	private R2OConditionalExpression appliesIf;//original version
	private R2OConditionalExpression appliesIfTop;//odemapster 1 version
	private Logger logger = Logger.getLogger(R2OConceptMapping.class);

	public R2OSelector getSelectorURIAs() {
		return selectorURIAs;
	}

	public R2OTransformationExpression getTransformationExpressionURIAs() {
		return transformationExpressionURIAs;
	}

	public R2OConceptMapping parse(Element conceptMappingElement) throws ParseException{
		R2OConceptMapping result = new R2OConceptMapping();

		//parse name attribute
		result.name = conceptMappingElement.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		logger.info("Parsing concept " + result.name);

		//parse documentation
		result.documentation = conceptMappingElement.getAttribute(R2OConstants.DOCUMENTATION_ATTRIBUTE);

		//parse identifiedBy attribute
		result.id = conceptMappingElement.getAttribute(R2OConstants.IDENTIFIED_BY_ATTRIBUTE);

		//parse has-table
		List<Element> hasTableElements = XMLUtility.getChildElementsByTagName(conceptMappingElement, R2OConstants.HAS_TABLE_TAG);
		if(hasTableElements != null && hasTableElements.size() > 0) {
			if(hasTableElements.size() > 1) {
				throw new ParseException("Multiple hasTables is not implemented yet!");
			}
			result.hasTables = new Vector<R2ODatabaseTable>();
			for(Element hasTableElement : hasTableElements) {
				result.hasTables.add(new R2ODatabaseTable().parse(hasTableElement));
			}
		} else {
			logger.warn("Deprecated use mode. hasTable elements should be defined.");
			throw new ParseException("hasTables element should be defined on a concept mapping!");
		}

		//parse uri-as element
		Element uriAsElement = XMLUtility.getFirstChildElementByTagName(conceptMappingElement, R2OConstants.URI_AS_TAG);
		Element selectorURIAsElement = XMLUtility.getFirstChildElementByTagName(uriAsElement, R2OConstants.SELECTOR_TAG);
		//Element transformationExpressionURIAs = XMLUtility.getFirstElement(uriAsElement);
		if(selectorURIAsElement != null) {
			result.selectorURIAs = new R2OSelector().parse(selectorURIAsElement);
		} else {
			result.transformationExpressionURIAs = new R2OTransformationExpression().parse(uriAsElement);
		}


		//parse applies-if/applies-if-top element
		Element appliesIfElement = XMLUtility.getFirstChildElementByTagName(conceptMappingElement, R2OConstants.APPLIES_IF_TAG);
		Element appliesIfTopElement = XMLUtility.getFirstChildElementByTagName(conceptMappingElement, R2OConstants.APPLIES_IF_TOP_TAG);
		if(appliesIfElement != null) {
			Element conditionElement = XMLUtility.getFirstElement(appliesIfElement);
			result.appliesIf = new R2OConditionalExpression().parse(conditionElement);
		} else {
			if(appliesIfTopElement != null) {
				logger.warn("applies-if-top is deprecated, use applies-if instead!");
				Element conditionElement = XMLUtility.getFirstElement(appliesIfTopElement);
				result.appliesIfTop = new R2OConditionalExpression().parse(conditionElement);				
			}
		}
			



		//parse documentation attribute
		result.documentation = conceptMappingElement.getAttribute(R2OConstants.DOCUMENTATION_ATTRIBUTE);

		//parse described-by element
		Element describedByElement = XMLUtility.getFirstChildElementByTagName(conceptMappingElement, R2OConstants.DESCRIBED_BY_TAG);
		if(describedByElement != null) {
			//logger.warn("Deprecated use mode. Property mappings should be defined under r2o element, not under concept mapping element!");
			result.describedBy = this.parseDescribedByElement(describedByElement);
		}


		return result;
	}

	private List<R2OPropertyMapping> parseDescribedByElement(Element describedByElement) throws ParseException {
		List<R2OPropertyMapping> result = new ArrayList<R2OPropertyMapping>();

		Collection<Element> propertyMappingElements = XMLUtility.getChildElements(describedByElement);
		if(propertyMappingElements != null) {
			for(Element propertyMappingElement : propertyMappingElements) {
				if(propertyMappingElement.getNodeName().equalsIgnoreCase(R2OConstants.ATTRIBUTEMAP_DEF_TAG)) {
					result.add(new R2OAttributeMapping().parse(propertyMappingElement));
				} else if(propertyMappingElement.getNodeName().equalsIgnoreCase(R2OConstants.DBRELATION_DEF_TAG)) {
					result.add(new R2ORelationMapping().parse(propertyMappingElement));
				} else {
					throw new ParseException("undefined mapping type.");
				}

			}
			
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<" + R2OConstants.CONCEPTMAP_DEF_TAG + " ");
		result.append(R2OConstants.NAME_ATTRIBUTE+"=\"" + this.name + "\" ");
		
		if(this.id != null && this.id != "") {
			result.append(R2OConstants.IDENTIFIED_BY_ATTRIBUTE +"=\"" + this.id + "\" ");
		}

		result.append(">\n");

		if(this.hasTables != null && this.hasTables.size() > 0) {
			for(R2ODatabaseTable hasTable : this.hasTables) {
				result.append(hasTable.toString() + "\n");
			}
		}

		
		result.append("<" + R2OConstants.URI_AS_TAG + ">\n");
		if(this.selectorURIAs != null) {
			result.append(this.selectorURIAs.toString() + "\n");
		} else {
			result.append(this.transformationExpressionURIAs.toString() + "\n");
		}
		result.append("</" + R2OConstants.URI_AS_TAG + ">\n");


		if(this.appliesIf != null) {
			result.append("<" + R2OConstants.APPLIES_IF_TAG + ">\n");
			result.append(this.appliesIf.toString());
			result.append("</" + R2OConstants.APPLIES_IF_TAG + ">\n");
		} else {
			if(this.appliesIfTop != null) {
				result.append("<" + R2OConstants.APPLIES_IF_TOP_TAG + ">\n");
				result.append(this.appliesIfTop.toString());
				result.append("</" + R2OConstants.APPLIES_IF_TOP_TAG + ">\n");			
			}
		}


		if(describedBy != null) {
			result.append("<" + R2OConstants.DESCRIBED_BY_TAG + ">\n");
			for(R2OPropertyMapping propertyMapping : describedBy) {
				result.append(propertyMapping.toString() + "\n");
			}
			result.append("</" + R2OConstants.DESCRIBED_BY_TAG + ">\n");
		}


		result.append("</" + R2OConstants.CONCEPTMAP_DEF_TAG + ">");
		return result.toString();
	}

	public R2OConditionalExpression getAppliesIf() {
		return appliesIf;
	}

	public R2OConditionalExpression getAppliesIfTop() {
		return appliesIfTop;
	}

	public List<R2OPropertyMapping> getDescribedBy() {
		return describedBy;
	}


	public List<R2OPropertyMapping> getPropertyMappings() {
		return describedBy;
	}
	
	public List<R2OAttributeMapping> getAttributeMappings() {
		List<R2OAttributeMapping> result = new ArrayList<R2OAttributeMapping>();
		
		List<R2OPropertyMapping> propertyMappings = this.getDescribedBy();
		if(propertyMappings != null) {
			for(R2OPropertyMapping propertyMapping : propertyMappings) {
				if(propertyMapping instanceof R2OAttributeMapping) {
					result.add((R2OAttributeMapping) propertyMapping);
				}
				
			}			
		}

		return result;
	}

	public List<R2ORelationMapping> getRelationMappings() {
		List<R2ORelationMapping> result = new ArrayList<R2ORelationMapping>();
		
		List<R2OPropertyMapping> propertyMappings = this.getDescribedBy();
		for(R2OPropertyMapping propertyMapping : propertyMappings) {
			if(propertyMapping instanceof R2ORelationMapping) {
				result.add((R2ORelationMapping) propertyMapping);
			}
			
		}
		return result;
	}

	@Override
	public String getConceptName() {
		return this.name;
	}

	public Vector<R2ODatabaseTable> getHasTables() {
		return hasTables;
	}
	



}
