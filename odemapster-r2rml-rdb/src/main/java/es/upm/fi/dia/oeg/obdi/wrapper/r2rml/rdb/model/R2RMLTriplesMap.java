package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractPropertyMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.IRelationMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.QueryTranslationException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.exception.R2RMLInvalidRefObjectMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.exception.R2RMLInvalidTermMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.exception.R2RMLInvalidTriplesMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.exception.R2RMLJoinConditionException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLPredicateObjectMap.ObjectMapType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTermMap.TermMapType;

public class R2RMLTriplesMap extends AbstractConceptMapping 
implements R2RMLElement, IConceptMapping {
	private static Logger logger = Logger.getLogger(R2RMLTriplesMap.class);

	private String triplesMapName;
	private R2RMLMappingDocument owner;
	private R2RMLLogicalTable logicalTable;
	private R2RMLSubjectMap subjectMap;
	private Collection<R2RMLPredicateObjectMap> predicateObjectMaps;
//	private String alias;
	
	public R2RMLTriplesMap(Resource triplesMap, R2RMLMappingDocument owner) 
			throws R2RMLInvalidTriplesMapException, R2RMLInvalidRefObjectMapException, R2RMLJoinConditionException, R2RMLInvalidTermMapException {
		this.owner = owner;
		this.triplesMapName = triplesMap.getLocalName();

		try {
			Statement logicalTableStatement = triplesMap.getProperty(
					R2RMLConstants.R2RML_LOGICALTABLE_PROPERTY);
			if(logicalTableStatement != null) {
				RDFNode logicalTableStatementObject = logicalTableStatement.getObject();
				Resource logicalTableStatementObjectResource = 
						(Resource) logicalTableStatementObject;
				this.logicalTable = R2RMLLogicalTable.parse(
						logicalTableStatementObjectResource, this);
			} else {
				String errorMessage = "Missing rr:logicalTable";
				logger.error(errorMessage);
				throw new R2RMLInvalidTriplesMapException(errorMessage);
			}
			
		} catch(Exception e) {
			
			throw new R2RMLInvalidTriplesMapException(e);
		}


		//rr:subjectMap
		StmtIterator triplesMaps = triplesMap.listProperties(
				R2RMLConstants.R2RML_SUBJECTMAP_PROPERTY);
		if(triplesMaps == null) {
			String errorMessage = "Missing rr:subjectMap";
			logger.error(errorMessage);
			throw new R2RMLInvalidTriplesMapException(errorMessage);
		}
		Collection<Statement> triplesMapsSet = triplesMaps.toSet();
		if(triplesMapsSet.size() > 1) {
			String errorMessage = "Multiple rr:subjectMap predicates are not allowed";
			logger.error(errorMessage);
			throw new R2RMLInvalidTriplesMapException(errorMessage);
		}
		Statement subjectMapStatement = triplesMap.getProperty(
				R2RMLConstants.R2RML_SUBJECTMAP_PROPERTY);
		if(subjectMapStatement != null) {
			//RDFNode subjectMapStatementObject = subjectMapStatement.getObject();
			Resource subjectMapStatementObjectResource = (Resource) subjectMapStatement.getObject();
			this.subjectMap = new R2RMLSubjectMap(subjectMapStatementObjectResource, this);
		} else {
			String errorMessage = "Missing rr:subjectMap";
			logger.error(errorMessage);
			throw new R2RMLInvalidTriplesMapException(errorMessage);
		}

		//rr:subject
		Statement subjectStatement = triplesMap.getProperty(
				R2RMLConstants.R2RML_SUBJECT_PROPERTY);
		if(subjectStatement != null) {
			String constantValueObject = subjectStatement.getObject().toString();
			this.subjectMap = new R2RMLSubjectMap(constantValueObject);
		}

		//rr:predicateObjectMap
		StmtIterator predicateObjectMapStatements = triplesMap.listProperties(
				R2RMLConstants.R2RML_PREDICATEOBJECTMAP_PROPERTY);
		if(predicateObjectMapStatements != null) {
			this.predicateObjectMaps = new HashSet<R2RMLPredicateObjectMap>();
			while(predicateObjectMapStatements.hasNext()) {
				Resource predicateObjectMapStatementObjectResource = (Resource) predicateObjectMapStatements.nextStatement().getObject();
				R2RMLPredicateObjectMap predicateObjectMap = 
						new R2RMLPredicateObjectMap(predicateObjectMapStatementObjectResource, owner, this); 
				this.predicateObjectMaps.add(predicateObjectMap);
			}
		}



	}

	public Object accept(R2RMLElementVisitor visitor) throws Exception {
		return visitor.visit(this);

	}

	public R2RMLLogicalTable getLogicalTable() {
		return logicalTable;
	}

	public R2RMLSubjectMap getSubjectMap() {
		return subjectMap;
	}

	public Collection<R2RMLPredicateObjectMap> getPredicateObjectMaps() {
		return predicateObjectMaps;
	}

	@Override
	public String toString() {
		return this.triplesMapName;
	}

	@Override
	public String getConceptName() {
		if(this.subjectMap.getClassURIs().size() > 1) {
			logger.warn("only one class URI is returned!");
		}
		return this.subjectMap.getClassURIs().iterator().next();
	}

	public Collection<R2RMLRefObjectMap> getRefObjectMaps() {
		Collection<R2RMLRefObjectMap> result = null;

		if(this.predicateObjectMaps != null) {
			for(R2RMLPredicateObjectMap predicateObjectMap : this.predicateObjectMaps) {
				if(predicateObjectMap.getObjectMapType() == ObjectMapType.RefObjectMap) {
					if(result == null) {
						result = new HashSet<R2RMLRefObjectMap>();
					}
					result.add(predicateObjectMap.getRefObjectMap());
				}
			}
		}

		return result;
	}

	@Override
	public Collection<AbstractPropertyMapping> getPropertyMappings(
			String propertyURI) {
		Collection<AbstractPropertyMapping> result =  new Vector<AbstractPropertyMapping>();
		Collection<R2RMLPredicateObjectMap> predicateObjectMaps = 
				this.getPredicateObjectMaps();
		if(predicateObjectMaps != null && predicateObjectMaps.size() > 0) {
			for(R2RMLPredicateObjectMap predicateObjectMap : predicateObjectMaps) {
				String predicateMapValue = predicateObjectMap.getPredicateMap().getOriginalValue();
				if(predicateMapValue.equals(propertyURI)) {
					result.add(predicateObjectMap);
				}
			}
		}
		return result;

	}

	@Override
	public Collection<AbstractPropertyMapping> getPropertyMappings() {
		Collection<AbstractPropertyMapping> result = new Vector<AbstractPropertyMapping>();
		if(this.predicateObjectMaps != null && this.predicateObjectMaps.size() > 0) {
			result.addAll(this.predicateObjectMaps);
		}
		return result;
	}



	@Override
	public Collection<IRelationMapping> getRelationMappings() {
		Collection<IRelationMapping> result = new Vector<IRelationMapping>();
		if(this.predicateObjectMaps != null) {
			for(AbstractPropertyMapping pm : this.predicateObjectMaps) {
				MappingType mappingType = pm.getPropertyMappingType();
				if(mappingType == MappingType.RELATION) {
					result.add((R2RMLPredicateObjectMap) pm);
				}
			}
		}
		return result;
	}

//	public String getPKColumn() {
//		TermMapValueType termMapValueType = this.subjectMap.getTermMapType();
//		
//		if(termMapValueType == TermMapValueType.COLUMN) {
//			return this.subjectMap.getColumnName();
//		} else if(termMapValueType == TermMapValueType.TEMPLATE) {
//			String stringTemplate = this.subjectMap.getTemplate();
//			Collection<String> attributes = 
//					R2RMLUtility.getAttributesFromStringTemplate(stringTemplate);
//
//			return attributes.iterator().next();
//		} else {
//			return null;
//		}
//	}
//
//	public String getPKValue(String uri) {
//		String result = null;
//		
//		TermMapValueType termMapValueType = this.subjectMap.getTermMapType();
//		
//		if(termMapValueType == TermMapValueType.TEMPLATE) {
//			String stringTemplate = this.subjectMap.getTemplate();
//			int beginIndex = stringTemplate.indexOf("{");
//			int endIndex = stringTemplate.indexOf("}");
//			
//			result = uri.substring(beginIndex);
//			//result = uri.substring(beginIndex -1 , uri.length());
//		}
//
//		return result;
//	}
	
	@Override
	public boolean hasWellDefinedURIExpression() {
		boolean result = this.subjectMap.hasWellDefinedURIExpression();
		return result;
	}

//	public void setLogicalTable(R2RMLLogicalTable logicalTable) {
//		this.logicalTable = logicalTable;
//	}

	public R2RMLMappingDocument getOwner() {
		return owner;
	}

//	@Override
//	public String getLogicalTableAlias() {
//		return this.logicalTable.getAlias();
//	}
//
//	@Override
//	public void setLogicalTableAlias(String logicalTableAlias) {
//		this.logicalTable.setAlias(logicalTableAlias);
//	}

	@Override
	public boolean isPossibleInstance(String uri) throws Exception {
		boolean result = false;
		
		TermMapType subjectMapTermMapType = this.subjectMap.getTermMapType();
		if(subjectMapTermMapType == TermMapType.TEMPLATE) {
			String pkValue = this.subjectMap.getTemplateValue(uri);
			if(pkValue != null) {
				result = true;
			}
		} else {
			String errorMessage = "Can't determine whether " + uri + " is a possible instance of " + this.toString();
			throw new QueryTranslationException(errorMessage);
		}
		
		return result;
	}


}
