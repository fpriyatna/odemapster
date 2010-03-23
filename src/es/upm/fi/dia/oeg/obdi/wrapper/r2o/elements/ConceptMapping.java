package es.upm.fi.dia.oeg.obdi.wrapper.r2o.elements;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.Constants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;

public class ConceptMapping extends OntologyMapping  {


	//	(18) conceptmapping-definition::= conceptmap-def name
	//    identified-by+
	//    (uri-as selector)?
	//    (applies-if cond-expr)?
	//    (joins-via concept-join-expr)?
	//    documentation?
	//    (described-by propertymap-def)*
	private String identifiedBy;
	private Selector selectorURIAs;//original version
	private TransformationExpression transformationExpressionURIAs;//modified version
	private List<PropertyMapping> describedBy;
	private ConditionalExpression appliesIf;
	private ConditionalExpression appliesIfTop;
	private Logger logger = Logger.getLogger(ConceptMapping.class);

	@Override
	public
	R2OElement parse(Element conceptMappingElement) throws R2OParserException{
		ConceptMapping result = new ConceptMapping();

		//parse name attribute
		result.name = conceptMappingElement.getAttribute(Constants.NAME_ATTRIBUTE);
		logger.info("Parsing concept " + result.name);

		//parse identifiedBy attribute
		result.identifiedBy = conceptMappingElement.getAttribute(Constants.IDENTIFIED_BY_ATTRIBUTE);

		//parse uri-as element
		Element uriAsElement = XMLUtility.getFirstElementByTagName(conceptMappingElement, Constants.URI_AS_TAG);
		Element selectorURIAsElement = XMLUtility.getFirstElementByTagName(uriAsElement, Constants.SELECTOR_TAG);
		//Element transformationExpressionURIAs = XMLUtility.getFirstElement(uriAsElement);
		if(selectorURIAsElement != null) {
			result.selectorURIAs = (Selector) new Selector().parse(selectorURIAsElement);
		} else {
			result.transformationExpressionURIAs = (TransformationExpression) new TransformationExpression().parse(uriAsElement);
		}


		//parse applies-if/applies-if-top element
		Element appliesIfElement = XMLUtility.getFirstElementByTagName(conceptMappingElement, Constants.APPLIES_IF_TAG);
		Element appliesIfTopElement = XMLUtility.getFirstElementByTagName(conceptMappingElement, Constants.APPLIES_IF_TOP_TAG);
		if(appliesIfElement != null) {
			Element conditionElement = XMLUtility.getFirstElement(appliesIfElement);
			result.appliesIf = (ConditionalExpression) new ConditionalExpression().parse(conditionElement);
		}
		if(appliesIfTopElement != null) {
			Element conditionElement = XMLUtility.getFirstElement(appliesIfTopElement);
			result.appliesIfTop = (ConditionalExpression) new ConditionalExpression().parse(conditionElement);				
		}



		//parse documentation attribute
		result.documentation = conceptMappingElement.getAttribute(Constants.DOCUMENTATION_ATTRIBUTE);

		//parse described-by element
		Element describedByElement = XMLUtility.getFirstElementByTagName(conceptMappingElement, Constants.DESCRIBED_BY_TAG);
		result.describedBy = this.parseDescribedByElement(describedByElement);

		//result.uriAs = new Selector().parse(element)
		// TODO Auto-generated method stub
		return result;
	}

	private List<PropertyMapping> parseDescribedByElement(Element describedByElement) throws R2OParserException {
		List<PropertyMapping> result = new ArrayList<PropertyMapping>();

		NodeList propertyMappingElements = describedByElement.getChildNodes();
		for(int i=0; i<propertyMappingElements.getLength();i++) {
			if(propertyMappingElements.item(i) instanceof Element) {
				Element propertyMappingElement = (Element) propertyMappingElements.item(i);
				if(propertyMappingElement.getNodeName().equalsIgnoreCase(Constants.ATTRIBUTEMAP_DEF_TAG)) {
					result.add((AttributeMapping) new AttributeMapping().parse(propertyMappingElement));
				} else if(propertyMappingElement.getNodeName().equalsIgnoreCase(Constants.ATTRIBUTEMAP_DEF_TAG)) {
					result.add((RelationMapping) new RelationMapping().parse(propertyMappingElement));
				} else {
					throw new R2OParserException("undefined mapping type.");
				}
			}

		}

		return result;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("<" + Constants.CONCEPTMAP_DEF_TAG + " ");
		result.append(Constants.NAME_ATTRIBUTE+"=\"" + this.name + "\" ");
		if(this.identifiedBy != null && this.identifiedBy != "") {
			result.append(Constants.IDENTIFIED_BY_ATTRIBUTE +"=\"" + this.identifiedBy + "\" ");
		}
		
		result.append(">\n");

		result.append("<" + Constants.URI_AS_TAG + ">\n");
		if(this.selectorURIAs != null) {
			result.append(this.selectorURIAs.toString() + "\n");
		} else {
			result.append(this.transformationExpressionURIAs.toString() + "\n");
		}
		result.append("</" + Constants.URI_AS_TAG + ">\n");


		if(this.appliesIf != null) {
			result.append("<" + Constants.APPLIES_IF_TAG + ">\n");
			result.append(this.appliesIf.toString());
			result.append("</" + Constants.APPLIES_IF_TAG + ">\n");
		}

		if(this.appliesIfTop != null) {
			result.append("<" + Constants.APPLIES_IF_TOP_TAG + ">\n");
			result.append(this.appliesIfTop.toString());
			result.append("</" + Constants.APPLIES_IF_TOP_TAG + ">\n");			
		}

		if(describedBy != null) {
			result.append("<" + Constants.DESCRIBED_BY_TAG + ">\n");
			for(PropertyMapping propertyMapping : describedBy) {
				result.append(propertyMapping.toString() + "\n");
			}
			result.append("</" + Constants.DESCRIBED_BY_TAG + ">\n");
		}


		result.append("</" + Constants.CONCEPTMAP_DEF_TAG + ">");
		// TODO Auto-generated method stub
		return result.toString();
	}



}
