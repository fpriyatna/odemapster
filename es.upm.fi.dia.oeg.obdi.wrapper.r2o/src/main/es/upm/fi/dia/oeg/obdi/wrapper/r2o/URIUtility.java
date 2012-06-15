package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.util.List;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConstantRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;

public class URIUtility {
	public static boolean isWellDefinedURIExpression(R2OTransformationExpression te) {
		String operator = te.getOperId();

		if(operator.equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_CONCAT)) {
			List<R2OArgumentRestriction> argumentRestrictions = te.getArgRestrictions();

			for(int i=0; i<argumentRestrictions.size()-1; i++) {
				R2OArgumentRestriction ar = argumentRestrictions.get(i);
				R2ORestriction restriction = ar.getRestriction();
				if((restriction instanceof R2OConstantRestriction) == false) {
					return false;
				}
			}

			R2ORestriction lastRestriction = argumentRestrictions.get(argumentRestrictions.size()-1).getRestriction();
			if(!(lastRestriction instanceof R2OColumnRestriction)) {
				return false;
			}

			return true;
		} else {
			return false;
		}
	}
	
	public static int getIRILengthWithoutPK(R2OTransformationExpression te) {
		int result=0;
		List<R2OArgumentRestriction> argumentRestrictions = te.getArgRestrictions();
		for(int i=0; i<argumentRestrictions.size()-1; i++) {
			R2OArgumentRestriction ar = argumentRestrictions.get(i);
			R2OConstantRestriction restriction = (R2OConstantRestriction) ar.getRestriction();
			result += restriction.getConstantValueAsString().length();
		}
		return result;
	}
	
	public static String getIRIWithoutPK(R2OTransformationExpression wellDefinedURIAs) {
		String result="";
		List<R2OArgumentRestriction> argumentRestrictions = wellDefinedURIAs.getArgRestrictions();
		for(int i=0; i<argumentRestrictions.size()-1; i++) {
			R2OArgumentRestriction ar = argumentRestrictions.get(i);
			R2OConstantRestriction restriction = (R2OConstantRestriction) ar.getRestriction();
			result += restriction.getConstantValueAsString();
		}
		return result;
	}
	
	public static String getPKTable(R2OTransformationExpression te) {
		int result=0;
		List<R2OArgumentRestriction> argumentRestrictions = te.getArgRestrictions();
		R2OArgumentRestriction lastAR = argumentRestrictions.get(argumentRestrictions.size() - 1);
		R2ORestriction restriction = lastAR.getRestriction();
		R2OColumnRestriction restrictionColumn = (R2OColumnRestriction) restriction;
		String tableName = restrictionColumn.getDatabaseColumn().getTableName();
		return tableName;
	}
	

	public static R2OColumnRestriction getLastRestriction(R2OTransformationExpression wellDefinedURIAs) {
		List<R2OArgumentRestriction> argumentRestrictions = wellDefinedURIAs.getArgRestrictions();
		R2OArgumentRestriction lastAR = argumentRestrictions.get(argumentRestrictions.size() - 1);
		R2ORestriction restriction = lastAR.getRestriction();
		R2OColumnRestriction restrictionColumn = (R2OColumnRestriction) restriction;
		return restrictionColumn;
	}
	
	public static R2OAttributeMapping generatePKColumnAttributeMapping(R2OConceptMapping cm, String attributeMappingName) {
		R2OTransformationExpression uriAs = cm.getURIAs();

		R2OColumnRestriction pkColumnRestriction = (R2OColumnRestriction) uriAs.getLastRestriction();
		R2ODatabaseColumn pkColumn = pkColumnRestriction.getDatabaseColumn();
		R2ODatabaseColumn newDBColumn = new R2ODatabaseColumn(pkColumn.getFullColumnName());
		newDBColumn.setAlias(pkColumn.getAlias());
		newDBColumn.setDataType(pkColumn.getDataType());
		R2OTransformationExpression te = new R2OTransformationExpression(R2OConstants.TRANSFORMATION_OPERATOR_CONSTANT);
		R2ORestriction restriction = new R2OColumnRestriction(newDBColumn);
		te.addRestriction(restriction);
		R2OSelector selector = new R2OSelector(null, te);

		R2OAttributeMapping pkAttributeMapping = new R2OAttributeMapping(attributeMappingName);
		pkAttributeMapping.addSelector(selector);
		pkAttributeMapping.setMappedPKColumn(true);

		return pkAttributeMapping;
	}
}
