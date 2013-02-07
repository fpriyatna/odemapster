package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator;

import java.util.Collection;

import org.apache.log4j.Logger;

import Zql.ZConstant;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AlphaResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTriplesMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTermMap.TermMapType;

public class R2RMLBetaGenerator extends AbstractBetaGenerator {
	private static Logger logger = Logger.getLogger(R2RMLBetaGenerator.class);

	public R2RMLBetaGenerator(AbstractQueryTranslator owner) {
		super(owner);
	}


	@Override
	protected SQLSelectItem calculateBetaObject(Triple tp, AbstractConceptMapping cm
			, String predicateURI, AlphaResult alphaResult) throws QueryTranslationException {
		SQLSelectItem selectItem = null;
		Node object = tp.getObject();
		
		R2RMLTriplesMap triplesMap = (R2RMLTriplesMap) cm;
		//String logicalTableAlias = triplesMap.getLogicalTable().getAlias();
		String logicalTableAlias = alphaResult.getAlphaSubject().getAlias();
		
		Collection<AbstractPropertyMapping> pms = cm.getPropertyMappings(predicateURI);
		if(pms == null || pms.isEmpty()) {
			String errorMessage = "Undefined mappings for : " + predicateURI 
					+ " for class " + cm.getConceptName();
			logger.warn(errorMessage);
		} else if (pms.size() > 1) {
			String errorMessage = "Multiple property mappings defined, result may be wrong!";
			throw new QueryTranslationException(errorMessage);			
		} else {//if(pms.size() == 1)
			AbstractPropertyMapping pm = pms.iterator().next();
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
					selectItem = new SQLSelectItem();
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
				if(object.isVariable()) {
					this.getOwner().getMapVarMapping().put(object.getName(), refObjectMap);
				}
				
				databaseColumnsString = refObjectMap.getParentDatabaseColumnsString();
				//String refObjectMapAlias = refObjectMap.getAlias(); 
				String refObjectMapAlias = R2RMLQueryTranslator.mapTripleAlias.get(tp);

				if(databaseColumnsString != null) {
					if(databaseColumnsString.size() > 1) {
						logger.warn("Multiple database columns in objectMap is not supported, result may be wrong!");
					}
					databaseColumnString = databaseColumnsString.iterator().next();
				}
				selectItem = R2RMLUtility.toSelectItem(databaseColumnString, refObjectMapAlias);						

			}			
		}


		return selectItem;
	}


	@Override
	protected SQLSelectItem calculateBetaSubject(Triple tp, AbstractConceptMapping cm, AlphaResult alphaResult) {
		SQLSelectItem selectItem = null;
		R2RMLTriplesMap triplesMap = (R2RMLTriplesMap) cm;
		R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
		//String logicalTableAlias = triplesMap.getLogicalTable().getAlias();
		String logicalTableAlias = alphaResult.getAlphaSubject().getAlias();
		
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

	protected AbstractQueryTranslator getOwner() {
		AbstractQueryTranslator result = super.getOwner();
		return result;
	}



}
