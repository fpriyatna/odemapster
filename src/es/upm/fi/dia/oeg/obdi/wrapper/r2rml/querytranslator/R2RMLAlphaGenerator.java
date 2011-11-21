package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractAlphaGenerator;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.R2OBetaGenerator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementUnfoldVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLAlphaGenerator extends AbstractAlphaGenerator {
	private static Logger logger = Logger.getLogger(R2RMLAlphaGenerator.class);
	
	public R2RMLAlphaGenerator(
			Map<Node, Collection<AbstractConceptMapping>> mapNodeConceptMapping,
			AbstractMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SQLLogicalTable calculateAlpha(Triple tp) throws Exception {
		SQLLogicalTable sqlLogicalTable = null;
		Node tpSubject = tp.getSubject();
		Node tpPredicate = tp.getPredicate();
		String predicateURI =tpPredicate.getURI();
		Collection<AbstractConceptMapping> cms = super.mapNodeConceptMapping.get(tpSubject);
		R2RMLTriplesMap cm = (R2RMLTriplesMap) cms.iterator().next();
		Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(predicateURI);
		
		R2RMLLogicalTable logicalTable = cm.getLogicalTable();
		sqlLogicalTable = new R2RMLElementUnfoldVisitor().visit(logicalTable);;
		String logicalTableAlias = sqlLogicalTable.generateAlias();
		logicalTable.setAlias(logicalTableAlias);
		sqlLogicalTable.setAlias(logicalTableAlias);
		
//		if(pms == null) {
//			R2RMLLogicalTable logicalTable = cm.getLogicalTable();
//			sqlLogicalTable = new R2RMLElementUnfoldVisitor().visit(logicalTable);;
//			String logicalTableAlias = sqlLogicalTable.generateAlias();
//			logicalTable.setAlias(logicalTableAlias);
//			sqlLogicalTable.setAlias(logicalTableAlias);
//		} else {
//			if(pms.size() > 1) {
//				logger.warn("Multiple mappings defined for property : " + predicateURI);
//			} 
//			R2RMLPredicateObjectMap pm = (R2RMLPredicateObjectMap) pms.iterator().next();
//			R2RMLRefObjectMap refObjectMap = pm.getRefObjectMap();
//			if(refObjectMap == null) {
//				R2RMLLogicalTable logicalTable = cm.getLogicalTable();
//				sqlLogicalTable = new R2RMLElementUnfoldVisitor().visit(logicalTable);;
//				String logicalTableAlias = sqlLogicalTable.generateAlias();
//				logicalTable.setAlias(logicalTableAlias);
//				sqlLogicalTable.setAlias(logicalTableAlias);
//			} else {
//				
//			}
//		}
		
		return sqlLogicalTable;
		

	}

	@Override
	public  Object calculateAlphaTB(Collection<Triple> triples)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractConceptMapping calculateAlphaCM(Triple tp) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractConceptMapping calculateAlphaCMTB(Collection<Triple> triples)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
 