package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZExp;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractAlphaGenerator;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLJoinCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLAlphaGenerator extends AbstractAlphaGenerator {
	private static Logger logger = Logger.getLogger(R2RMLAlphaGenerator.class);
	private R2RMLElementUnfoldVisitor unfolder;
	
	public R2RMLAlphaGenerator(
			Map<Node, Collection<AbstractConceptMapping>> mapNodeConceptMapping,
			AbstractMappingDocument mappingDocument
			, R2RMLElementUnfoldVisitor unfolder) {
		super(mapNodeConceptMapping, mappingDocument);
		this.unfolder = unfolder;
	}

	@Override
	public Collection calculateAlpha(Triple tp) throws Exception {
		Collection result = new Vector();
		
		//alpha subject
		Node tpSubject = tp.getSubject();
		Collection<AbstractConceptMapping> cms = super.mapNodeConceptMapping.get(tpSubject);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();
		SQLLogicalTable alphaSubject = this.calculateAlphaSubject(tpSubject, cm);
		result.add(alphaSubject);
		

		//alpha predicate object
		Node tpPredicate = tp.getPredicate();
		Node tpObject = tp.getObject();
		Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(tpPredicate.getURI());
		if(pms != null && pms.size() > 0) {
			R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) pms.iterator().next();
			
			R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
			if(refObjectMap != null) { 
				SQLQuery alphaPredicateObject = 
						this.calculateAlphaPredicateObject(pm, tp, cm);
				result.add(alphaPredicateObject);
			}
		}

		logger.debug("calculateAlpha = " + result);
		return result;
		

	}

	@Override
	protected SQLQuery calculateAlphaPredicateObject(
			AbstractPropertyMapping abstractPropertyMapping,
			Triple triple, AbstractConceptMapping abstractConceptMapping) {
		Node tpObject = triple.getObject();
		R2RMLTriplesMap cm = (R2RMLTriplesMap) abstractConceptMapping;
		//String tripleMapAlias = cm.getAlias();
		String tripleMapAlias = cm.getLogicalTable().getAlias();
		
		SQLQuery joinQuery = null;
		R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) abstractPropertyMapping;  
		R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
		if(refObjectMap != null) { 
			joinQuery = new SQLQuery();
			joinQuery.setJoinType("INNER");
			String joinQueryAlias = joinQuery.generateAlias();
			joinQuery.setAlias(joinQueryAlias);
			
			//refObjectMap.setAlias(joinQueryAlias);
			R2RMLQueryTranslator.mapTripleAlias.put(triple, joinQueryAlias);
			
			R2RMLLogicalTable parentLogicalTable = refObjectMap.getParentLogicalTable();
			//SQLLogicalTable sqlParentLogicalTable = new R2RMLElementUnfoldVisitor().visit(parentLogicalTable);
			SQLLogicalTable sqlParentLogicalTable = this.unfolder.visit(parentLogicalTable);
			joinQuery.addLogicalTable(sqlParentLogicalTable);

			Collection<R2RMLJoinCondition> joinConditions = refObjectMap.getJoinConditions();
			ZExp onExpression = R2RMLUtility.generateJoinCondition(joinConditions, tripleMapAlias, joinQueryAlias);
			if(onExpression != null) {
				joinQuery.setOnExp(onExpression);
			}
		}

		return joinQuery;
	}

//	@Override
//	public AbstractConceptMapping calculateAlphaCM(Triple tp) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public AbstractConceptMapping calculateAlphaCMTB(Collection<Triple> triples)
//			throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	protected SQLLogicalTable calculateAlphaSubject(Node subject,
			AbstractConceptMapping abstractConceptMapping) {
		R2RMLTriplesMap cm = (R2RMLTriplesMap) abstractConceptMapping;
		R2RMLLogicalTable r2rmlLogicalTable = cm.getLogicalTable();
		//SQLLogicalTable sqlLogicalTable = new R2RMLElementUnfoldVisitor().visit(logicalTable);;
		SQLLogicalTable sqlLogicalTable = this.unfolder.visit(r2rmlLogicalTable);;
		String logicalTableAlias = sqlLogicalTable.generateAlias();
		r2rmlLogicalTable.setAlias(logicalTableAlias);
		sqlLogicalTable.setAlias(logicalTableAlias);
//		cm.setAlias(logicalTableAlias);
		return sqlLogicalTable;
	}

	@Override
	public  Object calculateAlphaTB(Collection<Triple> triples)
			throws Exception {
		Collection result = new Vector();
		
		Triple firstTriple = triples.iterator().next();
		Node tpSubject = firstTriple.getSubject();
		Collection<AbstractConceptMapping> cms = super.mapNodeConceptMapping.get(tpSubject);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();
		SQLLogicalTable alphaSubject = this.calculateAlphaSubject(tpSubject, cm);
		result.add(alphaSubject);
		
		
		//mapping projection of corresponding predicates
		for(Triple tp : triples) {
			Node tpPredicate = tp.getPredicate();
			String tpPredicateURI = tpPredicate.getURI();
			boolean isRDFTypeStatement = RDF.type.getURI().equals(tpPredicateURI);
			if(this.ignoreRDFTypeStatement && isRDFTypeStatement) {
				//do nothing
			} else {
				Node tpObject = tp.getObject();
				Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(tpPredicateURI);
				if(pms != null) {
					R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) pms.iterator().next();
					logger.debug("pm = " + pm);
					R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
					if(refObjectMap != null) { 
						SQLQuery alphaPredicateObject = 
								this.calculateAlphaPredicateObject(pm, tp, cm);
						result.add(alphaPredicateObject);
					}
				}				
			}
		}

		logger.debug("alphaTB = " + result);
		return result;
	}

}
 