package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.core.XMLUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.Constants;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidViewException;
import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

public class R2ODatabaseView implements R2OElement {
	private String name;
	private List<R2OArgumentRestriction> argRestricts;
	private R2OJoin joinsVia;
	private String alias;
	
	public R2ODatabaseView(Element xmlElement) throws ParseException {
		this.parse(xmlElement);
	}
	
	public void parse(Element xmlElement) throws ParseException {
		//R2ODatabaseView result = new R2ODatabaseView();
		
		this.name = xmlElement.getAttribute(R2OConstants.NAME_ATTRIBUTE);

		
		List<Element> argRestrictionElements = XMLUtility.getChildElementsByTagName(
				xmlElement, R2OConstants.ARG_RESTRICTION_TAG);
		this.argRestricts = new ArrayList<R2OArgumentRestriction>();
		if(argRestrictionElements.size() > 2) {
			throw new InvalidViewException("View just takes 2 argument restriction and one joins via.");
		}
		
		for(Element argRestrictionElement : argRestrictionElements) {
			String restrictionNodeName = 
				XMLUtility.getFirstElement(argRestrictionElement).getNodeName();
			if(restrictionNodeName.equalsIgnoreCase(R2OConstants.HAS_TABLE_TAG)) {
				R2OArgumentRestriction argRestriction = new R2OArgumentRestriction(argRestrictionElement);
				this.argRestricts.add(argRestriction);
			} else if(restrictionNodeName.equalsIgnoreCase(R2OConstants.HAS_CONCEPT_TAG)) {
				R2OArgumentRestriction argRestriction = new R2OArgumentRestriction(argRestrictionElement);
				this.argRestricts.add(argRestriction);
			} else {
				throw new InvalidViewException("View just takes has-table or has-column input.");
			}
		}
		
		Element joinsViaElement = XMLUtility.getFirstChildElementByTagName(
				xmlElement, R2OConstants.JOINS_VIA_TAG);
		if(joinsViaElement != null) {
			this.joinsVia = new R2OJoin(joinsViaElement);
		}
		
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("<" + R2OConstants.HAS_VIEW_TAG + " ");
		if(this.name != null && this.name != "") {
			result.append(" " + R2OConstants.NAME_ATTRIBUTE +"=\"" + this.name + "\" ");
		}
		result.append(">\n");

		
		for(R2OArgumentRestriction argRestriction : this.argRestricts) {
			result.append(argRestriction.toString());
		}
		result.append(this.joinsVia.toString() + "\n");
		result.append(XMLUtility.toCloseTag(R2OConstants.HAS_VIEW_TAG));
		return result.toString();
	}



	public List<R2OArgumentRestriction> getArgRestricts() {
		return argRestricts;
	}

	public R2OJoin getJoinsVia() {
		return joinsVia;
	}
	
	public String generateAlias() {
		//String viewAlias = this.name;
		if(this.alias == null || this.alias == "") {
			//viewAlias = R2OConstants.VIEW_ALIAS + this.hashCode();
			this.alias = Constants.VIEW_ALIAS + new Random().nextInt(10000);
		}
		return this.alias;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
