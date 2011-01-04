package es.upm.fi.dia.oeg.obdi.wrapper.r2o.element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import es.upm.fi.dia.oeg.obdi.XMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.ParseException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OParserException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2ORelationMapping;

public abstract class R2ORestriction implements R2OElement {
	private static Logger logger = Logger.getLogger(R2ORestriction.class);

	//	(28) restriction::= has-value constant-value |
	//    has-column literal |
	//    has-transform transformation
	
	@Override
	public abstract void parse(Element element) throws ParseException;
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(this.toString());
		
		return result.toString();
	}


	
	
}
