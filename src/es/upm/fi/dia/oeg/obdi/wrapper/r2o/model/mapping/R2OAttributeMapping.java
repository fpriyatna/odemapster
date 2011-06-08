package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.IAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.IParseable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;

public class R2OAttributeMapping extends R2OPropertyMapping implements R2OElement, IAttributeMapping, Cloneable {
	private Logger logger = Logger.getLogger(R2OAttributeMapping.class);
	
	//	(33) attributemap-def::= attributemap-def name
	//    (selector* | use-dbcol)
	//    documentation?
	private String useDBCol;
	private String useDBColDatatype;

	private String useSQL;
	private String useSQLAlias;
	private String useSQLDataType;

	private Collection<R2OSelector> selectors;

	private Collection<String> hasDomains;
	private Collection<String> hasRanges;

	private String datatype;
	private String langDBCol;
	private String langDBColDataType;
	private String langHasValue;

	

	public R2OAttributeMapping(Element attributeMappingElement) throws ParseException {
		this.parse(attributeMappingElement);
	}


	public R2OAttributeMapping(R2OConceptMapping parent, Element attributeMappingElement) throws ParseException {
		super(parent);
		this.parse(attributeMappingElement);
	}
	
	@Override
	public void parse(Element attributeMappingElement) throws ParseException {
		//R2OAttributeMapping result = new R2OAttributeMapping();
		this.name = attributeMappingElement.getAttribute(R2OConstants.NAME_ATTRIBUTE);
		logger.info("Parsing attribute " + this.name);
		
		//parse identifiedBy attribute
		this.id = attributeMappingElement.getAttribute(R2OConstants.IDENTIFIED_BY_ATTRIBUTE);
		if(this.id == "") {
			this.id = null;
		}

		//parse datatype attribute
		this.datatype = attributeMappingElement.getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
		if(this.datatype == "") {
			this.datatype = null;
		}

		List<Element> hasDomainElements = XMLUtility.getChildElementsByTagName(
				attributeMappingElement, R2OConstants.HAS_DOMAIN_TAG);
		if(hasDomainElements != null) {
			if(hasDomainElements.size() > 1) {
				String errorMessage = "Unsupported multiple domains!";
				throw new ParseException(errorMessage);			
			}
			this.hasDomains = new ArrayList<String>();
			for(Element hasDomainElement : hasDomainElements) {
				this.hasDomains.add(hasDomainElement.getTextContent());
			}			
		}


		List<Element> hasRangeElements = XMLUtility.getChildElementsByTagName(
				attributeMappingElement, R2OConstants.HAS_RANGE_TAG);
		if(hasRangeElements != null) {
			if(hasRangeElements.size() > 1) {
				String errorMessage = "Unsupported multiple ranges!";
				throw new ParseException(errorMessage);			
			}
			this.hasRanges= new ArrayList<String>();
			for(Element hasRangeElement : hasRangeElements) {
				this.hasRanges.add(hasRangeElement.getTextContent());
			}			
		}


		List<Element> hasLanguageElements = XMLUtility.getChildElementsByTagName(attributeMappingElement, R2OConstants.HAS_LANGUAGE_TAG);
		if(hasLanguageElements != null) {
			if(hasLanguageElements.size() == 1) { //using language
				Element hasLanguageElement = hasLanguageElements.get(0);
				List<Element> hasLanguageChildrenElements = XMLUtility.getChildElements(hasLanguageElement);
				if(hasLanguageChildrenElements == null || hasLanguageChildrenElements.size() == 0)  {
					String errorMessage = "Undefined source of languages!";
					throw new ParseException(errorMessage);				
				}else if(hasLanguageChildrenElements.size() > 1) {
					String errorMessage = "Unsupported multiple sources of languages!";
					throw new ParseException(errorMessage);				
				} else {
					Element hasLanguageChildElement = hasLanguageChildrenElements.get(0);
					if(hasLanguageChildElement.getTagName().equalsIgnoreCase(R2OConstants.USE_DBCOL_TAG)) {
						Element langDBColElement = hasLanguageChildElement; 
						this.langDBCol = langDBColElement.getTextContent();
						if(langDBColElement.getAttribute(R2OConstants.DATATYPE_ATTRIBUTE) != null) {
							this.langDBColDataType = langDBColElement.getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
						}
					} else if(hasLanguageChildElement.getTagName().equalsIgnoreCase(R2OConstants.HAS_VALUE_TAG)) {
						this.langHasValue = hasLanguageChildElement.getTextContent();
					} else {
						String errorMessage = "Unsupported sources of languages!";
						throw new ParseException(errorMessage);					
					}
				}

			} else {
				String errorMessage = "Unsupported multiple languages!";
				throw new ParseException(errorMessage);			
			}			
		}


		int noOfBases = 0;
		List<Element> useDBColElements = XMLUtility.getChildElementsByTagName(attributeMappingElement, R2OConstants.USE_DBCOL_TAG);
		if(useDBColElements != null) {noOfBases++;}
		List<Element> useSQLElements = XMLUtility.getChildElementsByTagName(attributeMappingElement, R2OConstants.USE_SQL_TAG);
		if(useSQLElements != null) {noOfBases++;}
		List<Element> selectorElements = XMLUtility.getChildElementsByTagName(attributeMappingElement, R2OConstants.SELECTOR_TAG);
		if(selectorElements != null) {noOfBases++;}
		if(noOfBases == 0) {
			String errorMessage = "Specify either useDBCol, useSQL, or selector!";
			throw new ParseException(errorMessage);			
		} else if (noOfBases > 1) {
			String errorMessage = "Specify only one of useDBCol, useSQL, or selector!";
			throw new ParseException(errorMessage);			
		}

		if(useDBColElements != null) { //using db col
			if(useDBColElements.size() == 1) { //using db col
				this.useDBCol = useDBColElements.get(0).getTextContent();
				this.useDBColDatatype = useDBColElements.get(0).getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
			} else { 
				String errorMessage = "Unsupported multiple db columns!";
				throw new ParseException(errorMessage);
			}			
		}

		if(useSQLElements != null) { //using sql
			if(useSQLElements.size() == 1) { //using sql
				this.useSQL = useSQLElements.get(0).getTextContent();
				this.useSQLAlias = useSQLElements.get(0).getAttribute(R2OConstants.ALIAS_ATTRIBUTE);
				this.useSQLDataType = useSQLElements.get(0).getAttribute(R2OConstants.DATATYPE_ATTRIBUTE);
			} else {
				String errorMessage = "Unsupported multiple useSQL elements!";
				throw new ParseException(errorMessage);				
			}
		}

		if(selectorElements != null) {
			this.selectors = new ArrayList<R2OSelector>();
			for(Element childElement : selectorElements) {
				R2OSelector selector = new R2OSelector(childElement);
				this.selectors.add(selector);
			}			
		}
		

	}


	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("<" + R2OConstants.ATTRIBUTEMAP_DEF_TAG + " ");
		result.append(R2OConstants.NAME_ATTRIBUTE+"=\"" + this.name + "\" ");
		if(this.id != null && this.id != "") {
			result.append(R2OConstants.IDENTIFIED_BY_ATTRIBUTE +"=\"" + this.id + "\" ");
		}

		if(this.datatype != null && this.datatype != "") {
			result.append(R2OConstants.DATATYPE_ATTRIBUTE +"=\"" + this.datatype + "\" ");
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

		if(this.langDBCol != null) {
			result.append(XMLUtility.toOpenTag(R2OConstants.HAS_LANGUAGE_TAG)+ "\n");
			result.append("<" + R2OConstants.USE_DBCOL_TAG + " ");
			if(this.langDBColDataType != null && this.langDBColDataType != "") {
				result.append(R2OConstants.DATATYPE_ATTRIBUTE+"=\"" + this.langDBColDataType+ "\" ");
			}
			result.append(">\n");
			result.append(this.langDBCol);
			result.append("\n");
			result.append(XMLUtility.toCloseTag(R2OConstants.USE_DBCOL_TAG)+ "\n");
			result.append(XMLUtility.toCloseTag(R2OConstants.HAS_LANGUAGE_TAG)+ "\n");
		}

		if(this.langHasValue != null) {
			result.append(XMLUtility.toOpenTag(R2OConstants.HAS_LANGUAGE_TAG)+ "\n");
			result.append(XMLUtility.toOpenTag(R2OConstants.HAS_VALUE_TAG)+ "\n");
			result.append(this.langHasValue);
			result.append(XMLUtility.toCloseTag(R2OConstants.HAS_VALUE_TAG)+ "\n");
			result.append(XMLUtility.toCloseTag(R2OConstants.HAS_LANGUAGE_TAG)+ "\n");
		}

		if(this.useDBCol != null) {
			result.append("<" + R2OConstants.USE_DBCOL_TAG + " ");
			if(this.useDBColDatatype != null && this.useDBColDatatype != "") {
				result.append(R2OConstants.DATATYPE_ATTRIBUTE+"=\"" + this.useDBColDatatype+ "\" ");
			}
			result.append(">\n");
			result.append(this.useDBCol);
			result.append("\n");
			result.append(XMLUtility.toCloseTag(R2OConstants.USE_DBCOL_TAG)+ "\n");
		}

		if(this.useSQL != null) {
			result.append("<" + R2OConstants.USE_SQL_TAG + " ");
			if(this.useSQLAlias != null && this.useSQLAlias != "") {
				result.append(R2OConstants.ALIAS_ATTRIBUTE+"=\"" + this.useSQLAlias+ "\" ");
			}
			if(this.useSQLDataType != null && this.useSQLDataType != "") {
				result.append(R2OConstants.DATATYPE_ATTRIBUTE+"=\"" + this.useSQLDataType+ "\" ");
			}			
			result.append(" >\n");
			result.append(this.useSQL);

			result.append(XMLUtility.toCloseTag(R2OConstants.USE_SQL_TAG)+ "\n");
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


	public String getUseSQL() {
		return useSQL;
	}


	public String getUseSQLAlias() {
		return useSQLAlias;
	}


	public String getUseSQLDataType() {
		return useSQLDataType;
	}


	public String getDatatype() {
		return datatype;
	}



	public String getLangDBCol() {
		return langDBCol;
	}


	public String getLangHasValue() {
		return langHasValue;
	}


	public String getLangDBColDataType() {
		return langDBColDataType;
	}


	@Override
	public R2OAttributeMapping clone() {
		try {
			R2OAttributeMapping am2 = (R2OAttributeMapping) super.clone();
			am2.selectors = new ArrayList<R2OSelector>();
			for(R2OSelector selector : this.selectors) {
				am2.selectors.add(selector.clone());
			}
			return am2;
				
		} catch(Exception e) {
			logger.error("Error occured while cloning R2OAttributeMapping object.");
			logger.error("Error message = " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

}
