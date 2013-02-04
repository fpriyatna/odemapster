package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.fusiontables.querytranslator

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator.R2RMLQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator.R2RMLBetaGenerator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator.R2RMLCondSQLGenerator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator.R2RMLPRSQLGenerator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslatorUtility;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AlphaResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.BetaResult;

import com.hp.hpl.jena.graph.Node
import com.hp.hpl.jena.graph.Triple
import com.hp.hpl.jena.vocabulary.RDF;

import Zql.ZExp;

import org.apache.log4j.Logger;


import scala.collection.JavaConversions._


class R2RMLFusionTablesQueryTranslator  extends R2RMLQueryTranslator {
	val logger : Logger = Logger.getLogger("R2RMLFusionQueryTranslator");
	super.setUnfolder(new R2RMLElementUnfoldVisitor());
		
  override protected def buildAlphaGenerator(): Unit = {  
    super.setAlphaGenerator(new R2RMLFusionTablesAlphaGenerator(this));
  }

  override protected def buildBetaGenerator(): Unit = {  
	super.setBetaGenerator(new R2RMLFusionTablesBetaGenerator(this));
  }

  override protected def buildCondSQLGenerator(): Unit = {  
	super.setCondSQLGenerator(new R2RMLFusionTablesCondSQLGenerator(this));
  }

  override protected def buildPRSQLGenerator(): Unit = {  
    super.setPrSQLGenerator(new R2RMLFusionTablesPRSQLGenerator(this));
  }
  
  override protected  def trans(tp: Triple, cm: AbstractConceptMapping): SQLQuery = {
		var result : SQLQuery = null;
		try {
			val tpPredicate = tp.getPredicate();

			if(tpPredicate.isURI() && RDF.`type`.getURI().equals(tpPredicate.getURI()) 
					&& (this.isIgnoreRDFTypeStatement())) {
				result = null;
			} else {
				val alphaResultSet = super.getAlphaGenerator().calculateAlpha(tp, cm);
				val betaResultSet = super.getBetaGenerator().calculateBeta(tp, cm, alphaResultSet);
				if(alphaResultSet != null && alphaResultSet != null) {
					if(alphaResultSet.size() != betaResultSet.size()) {
						val errorMessage = "Number of alpha is not consistent with number of beta.";
						logger.error(errorMessage);
					}

					var unionSQLQueries : List[SQLQuery] = Nil;

					for(i <- 0 until betaResultSet.size()) {
						val alphaResult = alphaResultSet.get(i);
						val betaResult = betaResultSet.get(i);
						try {
							val sqlQuery : SQLQuery = this.trans(tp, alphaResult, betaResult, cm);
							logger.debug("sqlQuery("+ i +") = " + sqlQuery);
							if(sqlQuery != null) {
								unionSQLQueries ::= sqlQuery;  
							}
							
						} catch {
							case e : Exception => e.printStackTrace();logger.warn("Insatisfiable expression : " + e.getMessage());							
						}
					}

					if(!unionSQLQueries.isEmpty()) {
						val it = unionSQLQueries.iterator(); 
						result = it.next();
						while(it.hasNext()) {
							result.addUnionQuery(it.next());
						}						
					}
				}

			}
		} catch {
			case e : Exception => e.printStackTrace();logger.error("Error in transTP : " + tp);		  
		}

		return result;    
}

  
	private def trans(tp : Triple , alphaResult : AlphaResult , betaResult : BetaResult 
			, cm : AbstractConceptMapping ) : SQLQuery = {
		var result = new SQLQuery();
		//result.setComments("Query from TriplesMap : " + cm.toString() + " with predicate " + betaResult.getPredicateURI());

		//alpha
		val alphaSubject = alphaResult.getAlphaSubject();
		result.addLogicalTable(alphaSubject);//alpha from subject
		

		//PRSQL
		val selectItems = super.getPrSQLGenerator().genPRSQL(
				tp, betaResult, super.getNameGenerator(), cm);
		result.setSelectItems(selectItems);

		//CondSQL
		val condSQL = super.getCondSQLGenerator().genCondSQL(tp, alphaResult, betaResult, cm);
		if(condSQL != null) {
			result.addWhere(condSQL.getExpression());
		}

		//subquery elimination
		val optimizer = super.getOptimizer();
		if(optimizer != null && optimizer.isSubQueryElimination()) {
			result = QueryTranslatorUtility.eliminateSubQuery(result);
		}

		logger.debug("transTP = " + result);
		return result;
	}  
}



object R2RMLFusionTablesQueryTranslator {
  
}