package es.upm.fi.dia.oeg.core.utility.test

import scala.util.matching.Regex
import scala.collection.mutable.HashMap
import es.upm.fi.dia.oeg.obdi.core.utility.RegexUtility

object RegexUtilityTest extends App {
	val regexUtility = new RegexUtility();
	
	val templateString0 = "http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature/{nr}";
	println("templateString0 = " + templateString0);

	val templateColumns = regexUtility.getTemplateColumns(templateString0, true);
	println("templateColumns = " + templateColumns);
	
	val uriString = "http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductFeature/168";
	println("uriString = " + uriString);
	
	
	val templateValues = regexUtility.getTemplateMatching(templateString0, uriString);
	println("templateValues = " + templateValues);
	
}