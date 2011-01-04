package es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidTransfomationExperessionException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OPrimitiveOperationsProperties;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.MonetDBColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConstantRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSQLRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.RestrictionValue;

public class R2ORestrictionUnfolder {
	private R2ORestriction restriction;

	public R2ORestrictionUnfolder(R2ORestriction restriction) {
		super();
		this.restriction = restriction;
	}


	ZExp unfoldRestriction() throws InvalidTransfomationExperessionException {
		ZExp result = null;
		if(restriction instanceof R2OColumnRestriction) {
			result = this.unfoldHasColumnRestriction((R2OColumnRestriction) restriction);
		} else if(restriction instanceof R2OConstantRestriction) {
			R2OConstantRestriction restrictionConstant = 
				(R2OConstantRestriction) restriction;
			String restrictionDataType = restrictionConstant.getDatatype();
			String restrictionConstantValue = restrictionConstant.getConstantValueAsString();
			if(restrictionDataType == null) {
				restrictionDataType = R2OConstants.DATATYPE_STRING;
			}

			if(restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_STRING)) {
				result = new ZConstant(restrictionConstantValue, ZConstant.STRING);
			} else if(restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_DOUBLE)) {
				result = new ZConstant(restrictionConstantValue, ZConstant.NUMBER);
			} else if(restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_NUMBER)) {
				result = new ZConstant(restrictionConstantValue, ZConstant.NUMBER);				
			} else if(restrictionDataType.equalsIgnoreCase(R2OConstants.DATATYPE_INTEGER)) {
				result = new ZConstant(restrictionConstantValue, ZConstant.NUMBER);				
			} else {
				result = new ZConstant(restrictionConstantValue, ZConstant.STRING);
			}
		} else if(restriction instanceof R2OTransformationRestriction) {
			R2OTransformationRestriction rt = (R2OTransformationRestriction) restriction;
			R2OTransformationExpression restrictionTransformation = rt.getTransformationExpression();
			if(restrictionTransformation.isDelegableTransformationExpression()) {
				R2OTransformationExpressionUnfolder r2oTransformationExpressionUnfolder =
					new R2OTransformationExpressionUnfolder(restrictionTransformation);
				result = r2oTransformationExpressionUnfolder.unfoldDelegableTransformationExpression();
			} else {
				throw new InvalidTransfomationExperessionException("Unsupported transformation expression!");
			}
		} else if(restriction instanceof R2OSQLRestriction) {
			R2OSQLRestriction restrictionSQL = (R2OSQLRestriction) restriction;
			String useSql = restrictionSQL.getHasSQL();
			result = new ZConstant(useSql, ZConstant.UNKNOWN);
		}

		return result;
	}


	private ZConstant unfoldHasColumnRestriction(R2OColumnRestriction restrictionColumn) {
		R2ODatabaseColumn restrictionValue = 
			(R2ODatabaseColumn) restrictionColumn.getDatabaseColumn();
		ZConstant result = Utility.constructDatabaseColumn(restrictionValue.getColumnName()); 
		
		return result;
	}
}
