package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.querytranslator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap.TermMapType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLBetaGenerator extends AbstractBetaGenerator {
	private static Logger logger = Logger.getLogger(R2RMLBetaGenerator.class);
	
	public R2RMLBetaGenerator(AbstractQueryTranslator owner,
			Map<Node, Set<AbstractConceptMapping>> mapNodeConceptMapping,
			AbstractMappingDocument mappingDocument) {
		super(owner, mapNodeConceptMapping, mappingDocument);
	}



	@Override
	public ZSelectItem calculateBetaObject(
			AbstractConceptMapping cm, Triple triple)
			throws QueryTranslationException {
		Node object = triple.getObject();
		String predicateURI = triple.getPredicate().getURI();
		
		ZSelectItem selectItem = null;
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
				if(pms.size() > 1) {
					logger.warn("Multiple property mappings defined, result may be wrong!");
				}
				
				for(AbstractPropertyMapping pm : pms) {
					R2RMLPredicateObjectMap predicateObjectMap =(R2RMLPredicateObjectMap) pm;
					R2RMLRefObjectMap refObjectMap = predicateObjectMap.getRefObjectMap(); 
					String databaseColumnString = null;
					Collection<String> databaseColumnsString = null;

					if(refObjectMap == null) {
						R2RMLObjectMap objectMap = predicateObjectMap.getObjectMap();
						if(object.isVariable()) {
							this.getOwner().getMapVarMapping().put(object.getName(), objectMap);
						}
						
						
						if(objectMap.getTermMapType() == TermMapType.CONSTANT) {
							String constantValue = objectMap.getConstantValue();
							selectItem = new ZSelectItem();
							ZConstant zConstant = new ZConstant(constantValue, ZConstant.STRING);
							selectItem.setExpression(zConstant);
						} else {
							databaseColumnsString = objectMap.getDatabaseColumnsString();
							if(databaseColumnsString != null) {
								if(databaseColumnsString.size() > 1) {
									logger.warn("Multiple database columns in objectMap is not supported, result may be wrong!");
								}
								databaseColumnString = databaseColumnsString.iterator().next();
							}
							selectItem = R2RMLUtility.toSelectItem(databaseColumnString, logicalTableAlias);							
						}

					} else {
						databaseColumnsString = refObjectMap.getParentDatabaseColumnsString();
						//String refObjectMapAlias = refObjectMap.getAlias(); 
						String refObjectMapAlias = R2RMLQueryTranslator.mapTripleAlias.get(triple);
						
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
		logger.debug("calculateBetaCMObject = " + selectItem);
		return selectItem;
	}



	@Override
	public SQLSelectItem calculateBetaSubject(AbstractConceptMapping cm) {
		SQLSelectItem selectItem = null;
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
		
		logger.debug("calculateBetaCMSubject = " + selectItem);
		return selectItem;
	}



	public R2RMLQueryTranslator getOwner() {
		// TODO Auto-generated method stub
		return (R2RMLQueryTranslator) super.getOwner();
	}



}
