package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.TypeInferrer;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLQueryTranslator extends AbstractQueryTranslator {
	private static Logger logger = Logger.getLogger(R2RMLQueryTranslator.class);
	private R2RMLElementUnfoldVisitor unfolder;
	
	static Map<Triple, String> mapTripleAlias= new HashMap<Triple, String>();
	
	
	public R2RMLQueryTranslator(AbstractMappingDocument mappingDocument
			, R2RMLElementUnfoldVisitor unfolder) {
		super(mappingDocument);
		this.unfolder = unfolder;
	}


	@Override
	public SQLQuery translate(Query sparqlQuery) throws Exception {
		
		
		Element queryPattern = sparqlQuery.getQueryPattern();
		Op opQueryPattern = Algebra.compile(queryPattern);
		Op opSparqlQuery = Algebra.compile(sparqlQuery) ;
		
		TypeInferrer typeInferrer = new TypeInferrer(super.mappingDocument);
		super.mapInferredTypes = typeInferrer.infer(opQueryPattern);
		this.alphaGenerator = new R2RMLAlphaGenerator(
				mapInferredTypes, this.mappingDocument, this.unfolder);
		this.alphaGenerator.setIgnoreRDFTypeStatement(this.ignoreRDFTypeStatement);
		this.betaGenerator = new R2RMLBetaGenerator(mapInferredTypes, mappingDocument);
		this.prSQLGenerator = new R2RMLPRSQLGenerator(this.mapInferredTypes);
		this.prSQLGenerator.setIgnoreRDFTypeStatement(this.ignoreRDFTypeStatement);
		this.condSQLGenerator = new R2RMLCondSQLGenerator(betaGenerator, mapInferredTypes);
		this.condSQLGenerator.setIgnoreRDFTypeStatement(this.ignoreRDFTypeStatement);
		logger.debug("opSparqlQuery = " + opSparqlQuery);
		
		long start = System.currentTimeMillis();
		SQLQuery result = this.trans(opSparqlQuery);
		long end = System.currentTimeMillis();
		logger.info("Query translation time = "+ (end-start)+" ms.");

		//logger.info("trans query = \n" + result + "\n");
		return result;
	}




	@Override
	protected SQLQuery transTB(List<Triple> triples) throws Exception {
		SQLQuery result = new SQLQuery();
		
		//AlphaTB
		Vector alphaTables = (Vector) this.alphaGenerator.calculateAlphaTB(triples);
		SQLLogicalTable logicalTable = (SQLLogicalTable) alphaTables.get(0);
		result.addLogicalTable(logicalTable);//alpha subject
		if(alphaTables.size() > 1) {
			for(int i=1; i<alphaTables.size(); i++) {
				SQLQuery joinQuery = (SQLQuery) alphaTables.get(i);
				result.addJoinQuery(joinQuery);//alpha predicate object
			}
		}

		//PRSQLTB
		Collection<ZSelectItem> selectItems = 
				this.prSQLGenerator.genPRSQLTB(triples, betaGenerator, nameGenerator);
		result.setSelectItems(selectItems);

		//CondSQLTB
		ZExpression condSQL = this.condSQLGenerator.genCondSQLTB(triples);
		if(condSQL != null) {
			result.addWhere(condSQL);
		}
		
		logger.debug("transTB = " + result);
		return result;
	}




	@Override
	protected SQLQuery transTP(Triple tp) throws QueryTranslationException {
		SQLQuery result = new SQLQuery();
		try {
			Node tpSubject = tp.getSubject();
			Node tpPredicate = tp.getPredicate();

			if(RDF.type.getURI().equals(tpPredicate.getURI())) {
				if(this.isIgnoreRDFTypeStatement()) {
					return null;
				}
			} else {
				Collection<AbstractConceptMapping> cms = super.mapInferredTypes.get(tpSubject);
				if(cms != null) {
					//alpha
					Vector alphaTables = (Vector) this.alphaGenerator.calculateAlpha(tp);
					SQLLogicalTable logicalTable = (SQLLogicalTable) alphaTables.get(0);
					logger.debug("alpha logicalTable = " + logicalTable);
					result.addLogicalTable(logicalTable);//alpha from subject
					if(alphaTables.size() > 1) {
						for(int i=1; i<alphaTables.size(); i++) {
							SQLQuery joinQuery = (SQLQuery) alphaTables.get(i);
							logger.debug("alpha joinQuery = " + joinQuery);
							result.addJoinQuery(joinQuery);//alpha predicate object
						}
					}

					//PRSQL
					Collection<ZSelectItem> selectItems = this.prSQLGenerator.genPRSQL(tp, betaGenerator, nameGenerator);
					result.setSelectItems(selectItems);

					//CondSQL
					ZExpression condSQL = this.condSQLGenerator.genCondSQL(tp);
					logger.debug("condSQL = " + condSQL);
					if(condSQL != null) {
						result.addWhere(condSQL);
					}
				} else {
					String errorMessage = "Undefined triplesMap for class : " + tpSubject;
					throw new QueryTranslationException(errorMessage);
				}



			}

		} catch (Exception e) {
			logger.error("Error in transTP : " + tp);
			throw new QueryTranslationException(e.getMessage(), e);
		}

		logger.debug("transTP = " + result);
		return result;
	}

	@Override
	protected ZExp transVar(Op op, Var var) {
		String nameVar = nameGenerator.generateName(null, var);
		ZExp zExp = new ZConstant(nameVar, ZConstant.COLUMNNAME);
		return zExp;
	}


	@Override
	protected String generateTermCName(Node termC) {
		String termCName = nameGenerator.generateName(null, termC);
		return termCName;
	}





	@Override
	protected ZExp transIRI(Node node) {
		ZExp result = null;

		Collection<AbstractConceptMapping> cms = super.mapInferredTypes.get(node);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();

		boolean hasWellDefinedURI = cm.hasWellDefinedURIExpression();
		logger.debug("hasWellDefinedURI = " + hasWellDefinedURI);
		if(hasWellDefinedURI) {
			String pkValue = cm.getSubjectMap().getTemplateValue(node.getURI());
			result = new ZConstant(pkValue, ZConstant.UNKNOWN);
		}

		return result;
	}







}
