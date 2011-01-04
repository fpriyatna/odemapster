package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidViewException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

public class R2ODatabaseView implements R2OElement {
	private String name;
	private List<R2OArgumentRestriction> argRestricts;
	private R2OJoin joinsVia;
	
	public R2ODatabaseView(Element xmlElement) throws ParseException {
		this.parse(xmlElement);
	}
	
	@Override
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
	
	public String generateViewAlias() {
		String viewAlias = this.name;
		if(viewAlias == null || viewAlias == "") {
			viewAlias = R2OConstants.VIEW_ALIAS + this.hashCode();
		}
		return viewAlias;
	}

}
