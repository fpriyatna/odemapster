package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import Zql.ZConstant;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AlphaResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTermMap.TermMapType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTriplesMap;
import es.upm.fi.dia.oeg.upm.morph.sql.MorphSQLSelectItem;

public class R2RMLBetaGenerator extends AbstractBetaGenerator {
//	private static Logger logger = Logger.getLogger(R2RMLBetaGenerator.class);
	protected R2RMLQueryTranslator owner;
	
	public R2RMLBetaGenerator(AbstractQueryTranslator owner) {
		super(owner);
		this.owner = (R2RMLQueryTranslator) owner;
	}


	@Override
	public List<ZSelectItem> calculateBetaSubject(Triple tp,
			AbstractConceptMapping cm, AlphaResult alphaResult) {
		List<ZSelectItem> result = new ArrayList<ZSelectItem>();
		R2RMLTriplesMap triplesMap = (R2RMLTriplesMap) cm;
		R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
		String dbType = this.owner.getDatabaseType();
		

		
		//String logicalTableAlias = triplesMap.getLogicalTable().getAlias();
		String logicalTableAlias = alphaResult.getAlphaSubject().getAlias();
		
		Collection<String> databaseColumnsString = 
				subjectMap.getDatabaseColumnsString();
		if(databaseColumnsString != null) {
			for(String databaseColumnString : databaseColumnsString) {
				ZSelectItem selectItem = MorphSQLSelectItem.apply(databaseColumnString, logicalTableAlias, dbType, null);
				result.add(selectItem);
			}
		}

		
		return result;
	}

	protected AbstractQueryTranslator getOwner() {
		AbstractQueryTranslator result = super.getOwner();
		return result;
	}


	@Override
	public List<ZSelectItem> calculateBetaObject(Triple tp,
			AbstractConceptMapping cm, String predicateURI,
			AlphaResult alphaResult, AbstractPropertyMapping pm)
			throws QueryTranslationException {
		R2RMLPredicateObjectMap predicateObjectMap =(R2RMLPredicateObjectMap) pm;
		R2RMLRefObjectMap refObjectMap = predicateObjectMap.getRefObjectMap(); 
		List<ZSelectItem> betaObjects = new ArrayList<ZSelectItem>();
		String logicalTableAlias = alphaResult.getAlphaSubject().getAlias();
		String dbType = this.owner.getDatabaseType();

		if(refObjectMap == null) {
			R2RMLObjectMap objectMap = predicateObjectMap.getObjectMap();
//			if(object.isVariable()) {
//				this.getOwner().getMapVarMapping2().put(
//						object.getName(), objectMap);
//			}

			if(objectMap.getTermMapType() == TermMapType.CONSTANT) {
				String constantValue = objectMap.getConstantValue();
				ZConstant zConstant = new ZConstant(constantValue, ZConstant.STRING);
//				ZSelectItem selectItem = new SQLSelectItem();
//				selectItem.setExpression(zConstant);
				ZSelectItem selectItem = MorphSQLSelectItem.apply(zConstant);
				betaObjects.add(selectItem);
			} else {
				Collection<String> databaseColumnsString = objectMap.getDatabaseColumnsString();
				for(String databaseColumnString : databaseColumnsString) {
					ZSelectItem selectItem = MorphSQLSelectItem.apply(
							databaseColumnString,logicalTableAlias, dbType, null);
					
					betaObjects.add(selectItem);
				}
			}
		} else {
//			if(object.isVariable()) {
//				this.getOwner().getMapVarMapping2().put(object.getName(), refObjectMap);
//			}
			
			List<String> databaseColumnsString = refObjectMap.getParentDatabaseColumnsString();
			//String refObjectMapAlias = refObjectMap.getAlias(); 
			String refObjectMapAlias = this.owner.getMapTripleAlias().get(tp);

			if(databaseColumnsString != null) {
				for(String databaseColumnString : databaseColumnsString) {
					ZSelectItem selectItem = MorphSQLSelectItem.apply(
							databaseColumnString, refObjectMapAlias, dbType, null);
					
					betaObjects.add(selectItem);
				}
			}
		}	
		return betaObjects;
	}



}
