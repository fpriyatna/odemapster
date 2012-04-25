package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZOrderBy;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractAlphaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.NameGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslatorUtility;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.TypeInferrer;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.URIUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OConceptMappingUnfolder;

public class R2OQueryTranslator extends AbstractQueryTranslator {
	private static Logger logger = Logger.getLogger(R2OQueryTranslator.class);
	private Map<Triple, R2OConceptMapping> mapTripleCM = new HashMap<Triple, R2OConceptMapping>();
	protected Map<Node, String> mapNodeKey = new HashMap<Node, String>();
	
	public R2OQueryTranslator(AbstractMappingDocument mappingDocument) {
		super(mappingDocument);
		this.nameGenerator = new NameGenerator();
	}

	@Override
	protected ZExp transIRI(Node node) {
		ZExp result = null;
		
		Collection<AbstractConceptMapping> cms = this.mapInferredTypes.get(node);
		if(cms != null) {
			R2OConceptMapping cm = (R2OConceptMapping) cms.iterator().next();
			if(cm != null) {
				R2OTransformationExpression uriAs = cm.getURIAs();
				if(URIUtility.isWellDefinedURIExpression(uriAs)) {
					int index = URIUtility.getIRILengthWithoutPK(uriAs);
					String pk = node.getURI().substring(index);
					
					R2OColumnRestriction lastRestriction = URIUtility.getLastRestriction(uriAs);
					String dataType = lastRestriction.getDatabaseColumn().getDataType();
					if(R2OConstants.DATATYPE_DOUBLE.equals(dataType) ||
							R2OConstants.DATATYPE_INTEGER.equals(dataType) ||
							R2OConstants.DATATYPE_NUMBER.equals(dataType)) {
						result = new ZConstant(pk, ZConstant.NUMBER);
					} else {
						result = new ZConstant(pk, ZConstant.STRING);
					}
				} else {
					result = new ZConstant(node.getLocalName(), ZConstant.COLUMNNAME);
				}
			} else {
				result = new ZConstant(node.getLocalName(), ZConstant.COLUMNNAME);
			}					
		} else {
			result = new ZConstant(node.getLocalName(), ZConstant.COLUMNNAME);
		}
		
		return result;
	}
	
	


	@Override
	public SQLQuery translate(Query sparqlQuery) throws Exception  {
		
		Element queryPattern = sparqlQuery.getQueryPattern();
		Op opQueryPattern = Algebra.compile(queryPattern);
		Op opSparqlQuery = Algebra.compile(sparqlQuery) ;

		TranslatorUtility tu = new TranslatorUtility((R2OMappingDocument) this.mappingDocument);
		TypeInferrer typeInferrer = new TypeInferrer(this.mappingDocument);
		super.mapInferredTypes = typeInferrer.infer(opQueryPattern);
		AbstractAlphaGenerator alphaGenerator = 
			new R2OAlphaGenerator(mapInferredTypes, this.mappingDocument);
		this.alphaGenerator = alphaGenerator;
		AbstractBetaGenerator betaGenerator = 
			new R2OBetaGenerator(mapInferredTypes, this.mappingDocument);
		this.betaGenerator = betaGenerator;
		this.prSQLGenerator = new R2OPRSQLGenerator(mapInferredTypes, mapTripleCM, mapNodeKey);
		this.condSQLGenerator = new R2OCondSQLGenerator(betaGenerator, mapInferredTypes, mapTripleCM);
		SQLQuery sparql2SQLQuery =  this.trans(opSparqlQuery);
		logger.debug("translate = \n" + sparql2SQLQuery);
		return sparql2SQLQuery;
	}








	@Override
	public SQLQuery translateFromFile(String queryFilePath) throws Exception {
		// TODO Auto-generated method stub
		logger.warn("Implement this!");
		return null;
	}


	


	@Override
	protected SQLQuery transTB(List<Triple> triples) throws Exception {
		SQLQuery tbQuery = new SQLQuery();

		R2OAlphaGenerator r2oAlphaGenerator = (R2OAlphaGenerator) alphaGenerator;
		R2OConceptMapping cm = r2oAlphaGenerator.calculateAlphaCMTB(triples);
		for(Triple tp : triples) {
			this.mapTripleCM.put(tp, cm);
		}
		R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cm, (R2OMappingDocument) this.mappingDocument);
		SQLQuery alphaTB = cmu.unfoldConceptMapping();
		logger.debug("alphaTB = \n" + alphaTB);

		Vector<ZSelectItem> prSQLList = (Vector<ZSelectItem>) this.prSQLGenerator.genPRSQLTB(
				triples, betaGenerator, nameGenerator);
		logger.debug("genPRSQLTB = \n" + prSQLList);

		ZExpression condSQL2 = this.condSQLGenerator.genCondSQLTB(triples);
		logger.debug("genCondSQLTB = " + condSQL2);

		if(this.subQueryElimination) {
			tbQuery = QueryTranslatorUtility.eliminateSubQuery(prSQLList, alphaTB, condSQL2, null);
		} else {
			tbQuery.addSelect(prSQLList);
			
			//SQLFromItem fromItem = new SQLFromItem(alphaTB.toString(), SQLFromItem.FORM_QUERY);
			SQLFromItem fromItem = new SQLFromItem(alphaTB.toString(), LogicalTableType.SQLQUERY);
			fromItem.setAlias(fromItem.generateAlias());
			tbQuery.addFrom(fromItem);	
			
			if(condSQL2 != null) {
				tbQuery.addWhere(condSQL2);
			}
		}
		
		logger.debug("transTB = \n" + tbQuery + "\n");
		return tbQuery;
	}


	@Override
	protected SQLQuery transTP(Triple tp) throws QueryTranslationException  {
		logger.debug("transTP : " + tp);
		SQLQuery tpQuery = new SQLQuery();
		tpQuery.addSelect(new Vector<ZSelectItem>());
		tpQuery.addFrom(new Vector<ZFromItem>());

		try {
			R2OAlphaGenerator r2oAlphaGenerator = (R2OAlphaGenerator) alphaGenerator;
			R2OConceptMapping cm = r2oAlphaGenerator.calculateAlphaCM(tp);
			this.mapTripleCM.put(tp, cm);
			R2OConceptMappingUnfolder cmu = new R2OConceptMappingUnfolder(cm, (R2OMappingDocument) this.mappingDocument);
			SQLQuery alpha = cmu.unfoldConceptMapping();
			logger.debug("alpha(tp) = \n" + alpha);



			ZExpression condSQL = this.condSQLGenerator.genCondSQL(tp);
			
			Vector<ZSelectItem> prSQLList = (Vector<ZSelectItem>) this.prSQLGenerator.genPRSQL(tp, betaGenerator, nameGenerator);
			logger.debug("genPRSQL(tp,beta,name) = \n" + prSQLList);
			
			if(this.subQueryElimination) {
				tpQuery = QueryTranslatorUtility.eliminateSubQuery(prSQLList, alpha, condSQL, null);
			} else {
				tpQuery.addSelect(prSQLList);

				//SQLFromItem fromItem = new SQLFromItem(alpha.toString(), SQLFromItem.FORM_QUERY);
				SQLFromItem fromItem = new SQLFromItem(alpha.toString(), LogicalTableType.SQLQUERY);
				tpQuery.addFrom(fromItem);
				fromItem.setAlias(fromItem.generateAlias());

				if(condSQL != null) {
					tpQuery.addWhere(condSQL);
				}
			}
			
			logger.debug("transTP = \n" + tpQuery + "\n");
			return tpQuery;
		} catch(Exception e) {
			//e.printStackTrace();
			logger.error("Error processing tp : " + tp);
			throw new QueryTranslationException(e.getMessage(), e);
		}
	}


	@Override
	protected ZExp transVar(Op op, Var var) {
		String colName = nameGenerator.generateName(null, var);
		Node node = var.asNode();
		String nodePKColumn = this.mapNodeKey.get(node); 
		
		if(op == null) {
			if(nodePKColumn != null) {
				colName = nodePKColumn;
			}
		} else {
			Collection<Node> termsC = this.mapTermsC.get(op);
			if(nodePKColumn != null && termsC != null && termsC.contains(node)) {
				//colName = this.transGP1Aliases.iterator().next() + "." + nodePKColumn;
				colName = this.mapTransGP1Alias.get(op) + "." + nodePKColumn;
			} else {
				Collection<AbstractConceptMapping> cms = this.mapInferredTypes.get(node);
				if(cms != null) {
					AbstractConceptMapping cm = cms.iterator().next();
					boolean isWellDefinedURI = cm.hasWellDefinedURIExpression();
					if(isWellDefinedURI) {
						colName += R2OConstants.KEY_SUFFIX;
					}
				}
			}			
		}


		ZExp result = new ZConstant(colName, ZConstant.COLUMNNAME); 
		return result;
	}


	@Override
	protected String generateTermCName(Node termC) {
		String termCName = null;
		Collection<AbstractConceptMapping> cmTermCs = this.mapInferredTypes.get(termC);
		if(cmTermCs != null) {
			AbstractConceptMapping cmTermC = cmTermCs.iterator().next();
			if(cmTermC != null && cmTermC.hasWellDefinedURIExpression()) {
				//termCName = pkColumnAlias;
				termCName = this.mapNodeKey.get(termC);
			} else {
				termCName = nameGenerator.generateName(null, termC);
			}
		} else {
			termCName = nameGenerator.generateName(null, termC);
		}
		
		return termCName;
	}
	
	@Override
	protected Collection<ZSelectItem> generateSelectItems(Collection<Node> nodes, String prefix) {
		Collection<ZSelectItem> result = super.generateSelectItems(nodes, prefix);

		for(Node node : nodes) {
			String nodeKey = this.mapNodeKey.get(node);
			if(nodeKey != null) {
				if(prefix != null) {
					nodeKey = prefix + nodeKey;
				}
				ZSelectItem selectItem2 = new SQLSelectItem(nodeKey);
				result.add(selectItem2);
			}
		}

		return result;
	}
}
