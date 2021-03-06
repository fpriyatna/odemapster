package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.morph.base.Constants;
import es.upm.fi.dia.oeg.morph.base.SPARQLUtility;
import es.upm.fi.dia.oeg.morph.querytranslator.NameGenerator;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractPRSQLGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AlphaResult;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTriplesMap;
import es.upm.fi.dia.oeg.upm.morph.sql.MorphSQLSelectItem;

public class R2RMLPRSQLGenerator extends AbstractPRSQLGenerator {
	private static Logger logger = Logger.getLogger(R2RMLPRSQLGenerator.class);
	//private R2RMLQueryTranslator owner;
	
	public R2RMLPRSQLGenerator(AbstractQueryTranslator owner) {
		super(owner);
	}
	
//	@Override
//	public Collection<ZSelectItem> genPRSQL(Triple tp, BetaResult betaResult
//			, NameGenerator nameGenerator) throws Exception {
//		Node tpSubject = tp.getSubject();
//		AbstractConceptMapping cmSubject = this.mapInferredTypes.get(tpSubject).iterator().next();
//		return this.genPRSQL(tp, betaResult, nameGenerator, cmSubject);
//	}
	
	@Override
	protected Collection<ZSelectItem> genPRSQLSubject(Triple tp
			, AlphaResult alphaResult, AbstractBetaGenerator betaGenerator
			, NameGenerator nameGenerator, AbstractConceptMapping cmSubject
			) throws QueryTranslationException {
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();
		Node tpSubject = tp.getSubject();
		if(!SPARQLUtility.isBlankNode(tpSubject)) {
			Collection<ZSelectItem> parentResult = super.genPRSQLSubject(tp, alphaResult
					, betaGenerator, nameGenerator, cmSubject);
			result.addAll(parentResult);
			
			Node subject = tp.getSubject();
			Collection<ZSelectItem> selectItemsMappingId = this.genPRSQLSubjectMappingId(subject, cmSubject);
			if(selectItemsMappingId != null) {
				result.addAll(selectItemsMappingId);
			}			
		}

		
		return result;
		//ZSelectItem selectItemSubject = betaGenerator.calculateBetaSubject(cmSubject);
	}
	
	protected Collection<ZSelectItem> genPRSQLSubjectMappingId(Node subject, AbstractConceptMapping cmSubject) {
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();

		if(subject.isVariable()) {
			R2RMLTriplesMap triplesMap = (R2RMLTriplesMap) cmSubject;
			R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
			Collection<ZSelectItem> childResult = new Vector<ZSelectItem>();
			ZConstant mappingHashCodeConstant = new ZConstant(
					subjectMap.hashCode() + "", ZConstant.NUMBER);
			String databaseType = this.getOwner().getDatabaseType();
			ZSelectItem mappingSelectItem = MorphSQLSelectItem.apply(
					mappingHashCodeConstant, databaseType, Constants.POSTGRESQL_COLUMN_TYPE_INTEGER());
			String mappingSelectItemAlias = Constants.PREFIX_MAPPING_ID() + subject.getName();
			mappingSelectItem.setAlias(mappingSelectItemAlias);
			childResult.add(mappingSelectItem);
			result.addAll(childResult);
			this.getOwner().getMapHashCodeMapping().put(subjectMap.hashCode(), subjectMap);
		}	
		
		return result;
	}
	
//	@Override
//	public Collection<ZSelectItem> genPRSQLSTG(List<Triple> tripleBlock,
//			List<BetaResultSet> betaResultSet, NameGenerator nameGenerator, AbstractConceptMapping cm) throws Exception {
//
//		Collection<ZSelectItem> prList = this.genPRSQLSTG(tripleBlock, betaResultSet, nameGenerator, cm);
//		return prList;
//	}

	@Override
	public AbstractQueryTranslator getOwner() {
		return super.owner;
	}

	protected Collection<ZSelectItem> genPRSQLObjectMappingId(Node object
			, AbstractConceptMapping cmSubject, String predicateURI) {
		Collection<ZSelectItem> childResult = new Vector<ZSelectItem>();
		
		if(object.isVariable() && !SPARQLUtility.isBlankNode(object)) {
			
			Collection<AbstractPropertyMapping> propertyMappings = 
					cmSubject.getPropertyMappings(predicateURI);
			if(propertyMappings == null || propertyMappings.isEmpty()) {
				logger.warn("no property mappings defined for predicate: " + predicateURI);
			} else if (propertyMappings.size() > 1) {
				logger.warn("multiple property mappings defined for predicate: " + predicateURI);
			} else {
				AbstractPropertyMapping propertyMapping = propertyMappings.iterator().next();
				if(propertyMapping instanceof R2RMLPredicateObjectMap) {
					R2RMLPredicateObjectMap pom = (R2RMLPredicateObjectMap) propertyMapping;
					R2RMLObjectMap om = pom.getObjectMap();
					int mappingHashCode = -1;
					if(om != null) {
						mappingHashCode = om.hashCode();
						this.getOwner().getMapHashCodeMapping().put(mappingHashCode, om);
					} else {
						R2RMLRefObjectMap rom = pom.getRefObjectMap();
						if(rom != null) {
							mappingHashCode = rom.hashCode();
							this.getOwner().getMapHashCodeMapping().put(mappingHashCode, rom);
						}
					}
					
					if(mappingHashCode != -1) {
						ZConstant mappingHashCodeConstant = new ZConstant(
								mappingHashCode + "", ZConstant.NUMBER);
						String dbType = this.getOwner().getDatabaseType();
//						SQLSelectItem mappingSelectItem = new SQLSelectItem();
//						mappingSelectItem.setExpression(mappingHashCodeConstant);
//						mappingSelectItem.setDbType(dbType);
//						mappingSelectItem.setColumnType(Constants.POSTGRESQL_COLUMN_TYPE_INTEGER());
						ZSelectItem mappingSelectItem = MorphSQLSelectItem.apply(
								mappingHashCodeConstant, dbType, Constants.POSTGRESQL_COLUMN_TYPE_INTEGER());
						String mappingSelectItemAlias = Constants.PREFIX_MAPPING_ID() + object.getName();
						mappingSelectItem.setAlias(mappingSelectItemAlias);

						childResult.add(mappingSelectItem);
					}
				}				
			}

		}
		
		return childResult;
	}
	
	@Override
	protected Collection<ZSelectItem> genPRSQLObject(Triple tp,
			AlphaResult alphaResult, AbstractBetaGenerator betaGenerator,
			NameGenerator nameGenerator, AbstractConceptMapping cmSubject,
			String predicateURI, String columnType
			)
			throws QueryTranslationException {
		Collection<ZSelectItem> result = new Vector<ZSelectItem>();
		
		Node tpObject = tp.getObject();
		if(!SPARQLUtility.isBlankNode(tpObject)) {
			if(RDF.type.getURI().equalsIgnoreCase(predicateURI)) {
				R2RMLTriplesMap tm = (R2RMLTriplesMap) cmSubject;
				Collection<String> classURIs = tm.getSubjectMap().getClassURIs();
				for(String classURI : classURIs) {
					ZConstant zConstant = new ZConstant(classURI, ZConstant.STRING);
					ZSelectItem selectItem = new ZSelectItem();
					selectItem.setExpression(zConstant);
					String selectItemAlias = nameGenerator.generateName(tpObject);
					selectItem.setAlias(selectItemAlias);
					result.add(selectItem);
				}
			} else {
				Collection<ZSelectItem> parentResult = super.genPRSQLObject(tp, alphaResult, betaGenerator
						, nameGenerator, cmSubject, predicateURI, columnType);
				result.addAll(parentResult);
				
				Collection<ZSelectItem> childResult = this.genPRSQLObjectMappingId(tpObject, cmSubject, predicateURI);
				if(childResult != null) {
					result.addAll(childResult);
				}				
			}
			
		}

		return result;
	}
}
