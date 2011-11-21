package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator.POS;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLBetaGenerator extends AbstractBetaGenerator {
	private static Logger logger = Logger.getLogger(R2RMLBetaGenerator.class);
	
	public R2RMLBetaGenerator(
			Map<Node, Collection<AbstractConceptMapping>> mapNodeConceptMapping,
			AbstractMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
	}



	@Override
	public SQLSelectItem calculateBetaCMObject(String predicateURI,
			AbstractConceptMapping cm, Node object)
			throws QueryTranslationException {
		SQLSelectItem selectItem = null;
		R2RMLTriplesMap triplesMap = (R2RMLTriplesMap) cm;
		R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
		String logicalTableAlias = triplesMap.getLogicalTable().getAlias();
		
		if(RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
			ZConstant conceptNameConstant = new ZConstant(cm.getConceptName()
					, ZConstant.STRING);
			selectItem = new SQLSelectItem();
			selectItem.setExpression(conceptNameConstant);
		} else {
			Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(predicateURI);
			if(pms != null) {
				for(AbstractPropertyMapping pm : pms) {
					R2RMLPredicateObjectMap predicateObjectMap =(R2RMLPredicateObjectMap) pm;
					R2RMLRefObjectMap refObjectMap = predicateObjectMap.getRefObjectMap(); 
					String databaseColumnString = null;
					Collection<String> databaseColumnsString = null;

					if(refObjectMap == null) {
						R2RMLObjectMap objectMap = predicateObjectMap.getObjectMap();
						databaseColumnsString = objectMap.getDatabaseColumnsString();
						if(databaseColumnsString != null) {
							if(databaseColumnsString.size() > 1) {
								logger.warn("Multiple database columns in objectMap is not supported, result may be wrong!");
							}
							databaseColumnString = databaseColumnsString.iterator().next();
						}
						selectItem = R2RMLUtility.toSelectItem(databaseColumnString, logicalTableAlias);
					} else {
						databaseColumnsString = refObjectMap.getParentDatabaseColumnsString();
						String refObjectMapAlias = refObjectMap.getAlias(); 
						if(databaseColumnsString != null) {
							if(databaseColumnsString.size() > 1) {
								logger.warn("Multiple database columns in objectMap is not supported, result may be wrong!");
							}
							databaseColumnString = databaseColumnsString.iterator().next();
						}
						selectItem = R2RMLUtility.toSelectItem(databaseColumnString, refObjectMapAlias);						
								
					}

					

				}
			}
			
			
		}
		// TODO Auto-generated method stub
		logger.warn("Implement calculateBetaCMObject!");
		logger.info("calculateBetaCMObject = " + selectItem);
		return selectItem;
	}



	@Override
	public ZSelectItem calculateBetaCMSubject(AbstractConceptMapping cm) {
		ZSelectItem selectItem = null;
		R2RMLTriplesMap triplesMap = (R2RMLTriplesMap) cm;
		R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
		String logicalTableAlias = triplesMap.getLogicalTable().getAlias();
		Collection<String> databaseColumnsString = subjectMap.getDatabaseColumnsString();
		if(databaseColumnsString != null) {
			if(databaseColumnsString.size() > 1) {
				logger.warn("Multiple columns for subject maps is not supported, result might be wrong!");
			} 
			
			String databaseColumnString = databaseColumnsString.iterator().next();
			selectItem = R2RMLUtility.toSelectItem(databaseColumnString, logicalTableAlias);
		}
		
		logger.info("calculateBetaCMSubject = " + selectItem);
		return selectItem;
	}



}
