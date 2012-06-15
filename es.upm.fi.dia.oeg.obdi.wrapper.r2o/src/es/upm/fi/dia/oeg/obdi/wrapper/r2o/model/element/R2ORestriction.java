package es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import Zql.ZConstant;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.obdi.core.exception.ParseException;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;

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

	public Collection<ZSelectItem> getSelectItems() {
		Collection<ZSelectItem> result = new HashSet<ZSelectItem>();
		
		if(this instanceof R2OColumnRestriction) {
			R2OColumnRestriction restrictionColumn = (R2OColumnRestriction) this;
			R2ODatabaseColumn dbColumn = restrictionColumn.getDatabaseColumn();
			String fullColumnName = dbColumn.getFullColumnName();
//			ZConstant zColumn = Utility.constructDatabaseColumn(fullColumnName);
			ZSelectItem selectItem = new SQLSelectItem(fullColumnName);
			String alias = dbColumn.getAlias();
			/*
			if(alias == null || alias == "") {
				alias = columnName.replaceAll("\\.", "_");
			}
			*/
			if(alias != null && alias != "") {
				selectItem.setAlias(alias);
			}
			result.add(selectItem);
		} else if(this instanceof R2OSQLRestriction) {
			R2OSQLRestriction restrictionSQL = (R2OSQLRestriction) this;
			ZSelectItem selectItem = new SQLSelectItem(restrictionSQL.getHasSQL());
			ZConstant selectItemExpression = new ZConstant(restrictionSQL.getHasSQL(), ZConstant.UNKNOWN);
			selectItem.setExpression(selectItemExpression);
			String alias = restrictionSQL.getAlias();
			if(alias == null || alias=="") {
				alias = this.generateRestrictionAlias();
			}
			selectItem.setAlias(alias);
			result.add(selectItem);
		} else if(this instanceof R2OTransformationRestriction) {
			R2OTransformationRestriction restrictionTransformation = 
				(R2OTransformationRestriction) this;
			Collection<ZSelectItem> transformationExpressionSelectItems =
				restrictionTransformation.getTransformationExpression().getSelectItems();
			result.addAll(transformationExpressionSelectItems);
		}

		return result;
	}
	
	public String generateRestrictionAlias() {
		return R2OConstants.RESTRICTION_ALIAS + this.hashCode();
	}	
}
