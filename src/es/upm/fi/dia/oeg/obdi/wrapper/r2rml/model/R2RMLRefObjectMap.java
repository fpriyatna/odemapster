package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.exception.R2RMLInvalidRefObjectMapException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.exception.R2RMLJoinConditionException;

public class R2RMLRefObjectMap {
	private R2RMLMappingDocument owner;
	private static Logger logger = Logger.getLogger(R2RMLObjectMap.class);
	
	private String parentTriplesMap;
	private Collection<R2RMLJoinCondition> joinConditions;
	private R2RMLLogicalTable intermediateLogicalTable;
	private String alias;
	
	public static boolean isRefObjectMap(Resource resource) {

		boolean hasParentTriplesMap = false;
		Statement parentTriplesMapStatement = resource.getProperty(
				R2RMLConstants.R2RML_PARENTTRIPLESMAP_PROPERTY);
		if(parentTriplesMapStatement != null)  {
			hasParentTriplesMap = true;
		}

//		boolean hasJoinCondition = false;
//		StmtIterator joinConditionsStatements = resource.listProperties(R2RMLConstants.R2RML_JOINCONDITION_PROPERTY);
//		if(joinConditionsStatements != null && joinConditionsStatements.hasNext()) {
//			hasJoinCondition = true;
//		}
		
		return hasParentTriplesMap;
		
	}
	
	public R2RMLRefObjectMap(Resource resource, R2RMLMappingDocument owner) 
			throws R2RMLInvalidRefObjectMapException, R2RMLJoinConditionException {
		this.owner = owner;
		
		Statement parentTriplesMapStatement = resource.getProperty(R2RMLConstants.R2RML_PARENTTRIPLESMAP_PROPERTY);
		if(parentTriplesMapStatement != null)  {
			this.parentTriplesMap = parentTriplesMapStatement.getObject().toString();
		}
		
		this.joinConditions = new HashSet<R2RMLJoinCondition>();
		StmtIterator joinConditionsStatements = resource.listProperties(R2RMLConstants.R2RML_JOINCONDITION_PROPERTY);
		if(joinConditionsStatements != null && joinConditionsStatements.hasNext()) {
			while(joinConditionsStatements.hasNext()) {
				Statement joinConditionStatement = joinConditionsStatements.nextStatement();
				Resource joinConditionResource = (Resource) joinConditionStatement.getObject();
				R2RMLJoinCondition joinCondition = new R2RMLJoinCondition(joinConditionResource); 
				this.joinConditions.add(joinCondition);
			}
		} else {
//			R2RMLJoinCondition joinCondition = new R2RMLJoinCondition(
//					"1", "1");
//			this.joinConditions.add(joinCondition);
			
			String errorMessage = "No join conditions defined!";
			logger.warn(errorMessage);
//			throw new R2RMLInvalidRefObjectMapException(errorMessage);			
		}
	}

	@Override
	public String toString() {
		return this.parentTriplesMap;
	}

	public R2RMLLogicalTable getParentLogicalTable() {
		R2RMLTriplesMap triplesMap = this.getParentTriplesMap();
		return triplesMap.getLogicalTable();
	}

	public String getParentTripleMapName() {
		return this.parentTriplesMap;
	}
	public R2RMLTriplesMap getParentTriplesMap() {
		R2RMLTriplesMap triplesMap = (R2RMLTriplesMap) this.owner.getConceptMappingByMappingId(this.parentTriplesMap);
		return triplesMap;
	}

	public Collection<R2RMLJoinCondition> getJoinConditions() {
		return joinConditions;
	}

	public Collection<String> getParentDatabaseColumnsString() {
		Collection<String> result = new HashSet<String>();
		
		R2RMLSubjectMap parentSubjectMap = this.getParentTriplesMap().getSubjectMap();
		result.addAll(parentSubjectMap.getDatabaseColumnsString());
		return result;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
