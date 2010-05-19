package es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.IParseable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSelector;

public class R2OAttributeMapping extends R2OPropertyMapping implements R2OElement, IAttributeMapping {
//	(33) attributemap-def::= attributemap-def name
//    (selector* | use-dbcol)
//    documentation?
	private String useDBCol;
	private String useDBColDatatype;
	private Collection<R2OSelector> selectors;
	private Collection<String> hasDomains;
	private Collection<String> hasRanges;
	
	@Override
	public R2OAttributeMapping parse(Element attributeMappingElement) throws ParseException {
		R2OAttributeMapping result = new R2OAttributeMapping();
		result.name = attributeMappingElement.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		
		//parse identifiedBy attribute
		result.id = attributeMappingElement.getAttribute(R2OConstants.IDENTIFIED_BY_ATTRIBUTE);
		
		List<Element> hasDomainElements = XMLUtility.getChildElementsByTagName(
				attributeMappingElement, R2OConstants.HAS_DOMAIN_TAG);
		if(hasDomainElements.size() > 1) {
			String errorMessage = "Unsupported multiple domains!";
			throw new ParseException(errorMessage);			
		}
		result.hasDomains = new ArrayList<String>();
		for(Element hasDomainElement : hasDomainElements) {
			result.hasDomains.add(hasDomainElement.getTextContent());
		}

		List<Element> hasRangeElements = XMLUtility.getChildElementsByTagName(
				attributeMappingElement, R2OConstants.HAS_RANGE_TAG);
		if(hasRangeElements.size() > 1) {
			String errorMessage = "Unsupported multiple ranges!";
			throw new ParseException(errorMessage);			
		}
		result.hasRanges= new ArrayList<String>();
		for(Element hasRangeElement : hasRangeElements) {
			result.hasRanges.add(hasRangeElement.getTextContent());
		}
		
		
		List<Element> useDBColElements = XMLUtility.getChildElementsByTagName(attributeMappingElement, R2OConstants.USE_DBCOL_TAG);
		if(useDBColElements.size() == 1) { //using db col
			result.useDBCol = useDBColElements.get(0).getTextContent();
			result.useDBColDatatype = useDBColElements.get(0).getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
		} else { //using selector
			List<Element> selectorElements = XMLUtility.getChildElementsByTagName(attributeMappingElement, R2OConstants.SELECTOR_TAG);
			
			if(selectorElements.size() > 1) {
				String errorMessage = "Unsupported multiple selectors!";
				throw new ParseException(errorMessage);
			}
			result.selectors = new ArrayList<R2OSelector>();
			for(Element childElement : selectorElements) {
				R2OSelector selector = new R2OSelector().parse(childElement);
				result.selectors.add(selector);
			}			
			
		}
		

		return result;
	}


	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("<" + R2OConstants.ATTRIBUTEMAP_DEF_TAG + " ");
		result.append(R2OConstants.NAME_ATTRIBUTE+"=\"" + this.name + "\" ");
		if(this.id != null && this.id != "") {
			result.append(R2OConstants.IDENTIFIED_BY_ATTRIBUTE +"=\"" + this.id + "\" ");
		}
		result.append(">\n");

		if(this.hasDomains != null) {
			for(String hasDomain : this.hasDomains) {
				result.append(XMLUtility.toOpenTag(R2OConstants.HAS_DOMAIN_TAG)+ "\n");
				result.append(hasDomain);
				result.append(XMLUtility.toCloseTag(R2OConstants.HAS_DOMAIN_TAG)+ "\n");				
			}
		}

		if(this.hasRanges != null) {
			for(String hasRange : this.hasRanges) {
				result.append(XMLUtility.toOpenTag(R2OConstants.HAS_RANGE_TAG)+ "\n");
				result.append(hasRange);
				result.append(XMLUtility.toCloseTag(R2OConstants.HAS_RANGE_TAG)+ "\n");				
			}
		}

		if(this.useDBCol != null) {
			result.append(XMLUtility.toOpenTag(R2OConstants.USE_DBCOL_TAG)+ "\n");
			result.append(this.useDBCol);
			result.append(XMLUtility.toCloseTag(R2OConstants.USE_DBCOL_TAG)+ "\n");
		}
		
		if(this.selectors != null) {
			for(R2OSelector selector : this.selectors) {
				result.append(selector.toString() + "\n");
			}
			
		}
		
		result.append(XMLUtility.toCloseTag(R2OConstants.ATTRIBUTEMAP_DEF_TAG)+ "\n");
		return result.toString();
	}


	public Collection<R2OSelector> getSelectors() {
		return selectors;
	}


	public String getUseDBCol() {
		return useDBCol;
	}


	@Override
	public String getAttributeName() {
		return this.name;
	}


	public String getUseDBColDatatype() {
		return useDBColDatatype;
	}

	
	
}
