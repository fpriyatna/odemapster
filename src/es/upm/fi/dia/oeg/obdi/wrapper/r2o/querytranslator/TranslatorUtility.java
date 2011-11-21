package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZOrderBy;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.URIUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConstantRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OJoin;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2ORelationMapping;

public class TranslatorUtility {
	private static Logger logger = Logger.getLogger(TranslatorUtility.class);
	private R2OMappingDocument mappingDocument;

	public TranslatorUtility(R2OMappingDocument mappingDocument) {
		super();
		this.mappingDocument = mappingDocument;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {



	}





	public static ZSelectItem generateCoalesceSelectItem(ZSelectItem selectItem1, ZSelectItem selectItem2, String alias) {

		ZExpression expression = new ZExpression("coalesce");

		String selectItem1Alias = selectItem1.getAlias();
		selectItem1.setAlias("");
		expression.addOperand(new ZConstant(selectItem1.toString(), ZConstant.COLUMNNAME));
		if(selectItem1Alias != null) {selectItem1.setAlias(selectItem1Alias);}

		String selectItem2Alias = selectItem2.getAlias();
		selectItem2.setAlias("");
		expression.addOperand(new ZConstant(selectItem2.toString(), ZConstant.COLUMNNAME));
		if(selectItem2Alias != null) {selectItem2.setAlias(selectItem2Alias);}

		ZSelectItem result = new ZSelectItem();
		result.setExpression(expression);
		result.setAlias(alias);

		return result;
	}

	public static ZSelectItem generateCoalesceSelectItem(String columnName, String r1, String r2) {
		ZExpression expression = new ZExpression("coalesce");

		ZConstant operand1 = new ZConstant(r1 + "." + columnName, ZConstant.COLUMNNAME);
		expression.addOperand(operand1);
		ZConstant operand2 = new ZConstant(r2 + "." + columnName, ZConstant.COLUMNNAME);
		expression.addOperand(operand2);

		ZSelectItem result = new ZSelectItem();
		result.setExpression(expression);
		result.setAlias(columnName);

		return result;
	}









	public static R2ORelationMapping processObjectPredicateObjectURI(
			Node object , R2ORelationMapping rm, R2OMappingDocument md) 
					throws QueryTranslationException
					{
		String toConcept = rm.getToConcept();
		R2OConceptMapping rangeConceptMapping = 
				(R2OConceptMapping) md.getConceptMappingByMappingId(toConcept);

		R2OTransformationExpression rangeUriAs = rangeConceptMapping.getURIAs();
		String objectURI = object.getURI();

		R2OCondition condition = TranslatorUtility.generateEquityCondition(rangeUriAs, objectURI);

		R2OJoin joinVia = rm.getJoinsVia();
		R2OJoin joinVia2 = joinVia.clone();
		R2OConditionalExpression joinsViaCE1 = joinVia.getJoinConditionalExpression();
		R2OConditionalExpression joinsViaCE2 = 
				R2OConditionalExpression.addCondition(joinsViaCE1, R2OConstants.AND_TAG, condition);
		joinVia2.setJoinConditionalExpression(joinsViaCE2);

		R2ORelationMapping rm2 = rm.clone();
		rm2.setJoinsVia(joinVia2);

		return rm2;
					}

	public static R2OCondition generateEquityCondition(R2OTransformationExpression te, String value) {
		R2OArgumentRestriction arConstant;
		R2OConstantRestriction cr;
		R2OArgumentRestriction arTE;
		if(URIUtility.isWellDefinedURIExpression(te)) {
			R2OColumnRestriction pkColumnRestriction = (R2OColumnRestriction) te.getLastRestriction();
			R2ODatabaseColumn pkColumn = pkColumnRestriction.getDatabaseColumn();
			String pkDataType = pkColumn.getDataType();

			arTE = new R2OArgumentRestriction(pkColumnRestriction);
			cr = new R2OConstantRestriction();
			int subjectURILengthWithoutPK = URIUtility.getIRILengthWithoutPK(te);
			String subjectURIWithoutPK = value.substring(0, subjectURILengthWithoutPK);
			String subjectPKOnly = value.substring(subjectURIWithoutPK.length(), value.length()); 
			cr.setConstantValue(subjectPKOnly);
			if(pkDataType != null && !pkDataType.equals("")) {
				cr.setDatatype(pkDataType);
			}

		} else {
			cr = new R2OConstantRestriction();
			cr.setConstantValue(value);

			R2OTransformationRestriction tr = new R2OTransformationRestriction(te);
			arTE = new R2OArgumentRestriction(tr);
		}
		arConstant = new R2OArgumentRestriction(cr);
		List<R2OArgumentRestriction> argRestrictions = new ArrayList<R2OArgumentRestriction>();
		argRestrictions.add(arConstant);
		argRestrictions.add(arTE);
		R2OCondition condition = new R2OCondition(R2OConstants.CONDITION_TAG
				, argRestrictions, R2OConstants.CONDITIONAL_OPERATOR_EQUALS_NAME);
		return condition;
	}

	public String generatePKColumnAlias(R2OConceptMapping cm, Node node) {
		return cm.getId() + R2OConstants.KEY_SUFFIX + node.hashCode();
	}




}
