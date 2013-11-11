package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import Zql.ZExpression;
import Zql.ZQuery;
import Zql.ZSelectItem;
import es.upm.fi.dia.oeg.morph.base.Constants;
import es.upm.fi.dia.oeg.obdi.core.ILogicalQuery;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLJoinTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLJoinCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLPredicateMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLSQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTermMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTriplesMap;
import es.upm.fi.dia.oeg.upm.morph.sql.MorphSQLSelectItem;

public class R2RMLElementUnfoldVisitor extends AbstractUnfolder implements R2RMLElementVisitor {
	private static Logger logger = Logger.getLogger(R2RMLElementUnfoldVisitor.class);
	private Map<R2RMLRefObjectMap, String> mapRefObjectMapAlias = new HashMap<R2RMLRefObjectMap, String>();
	//private ConfigurationProperties configurationProperties;
	private Constants constants = new Constants();
	
	public Map<R2RMLRefObjectMap, String> getMapRefObjectMapAlias() {
		return mapRefObjectMapAlias;
	}

	public Collection<SQLQuery> visit(R2RMLMappingDocument mappingDocument) {
		Collection<SQLQuery> result = new HashSet<SQLQuery>();

		Collection<AbstractConceptMapping> triplesMaps = mappingDocument.getConceptMappings();
		if(triplesMaps != null) {
			for(AbstractConceptMapping triplesMap : triplesMaps) {
				try {
					SQLQuery triplesMapUnfolded = (SQLQuery) ((R2RMLTriplesMap) triplesMap).accept(this);
					result.add(triplesMapUnfolded);
				} catch(Exception e) {
					logger.error("error while unfolding triplesMap : " + triplesMap);
					logger.error("error message = " + e.getMessage());
				}
			}
		}
		return result;
	}

	private SQLQuery unfoldSubjectMap(R2RMLLogicalTable logicalTable
			, R2RMLSubjectMap subjectMap
			, SQLQuery result, Collection<ZSelectItem> resultSelectItems) {
		SQLFromItem logicalTableUnfolded = null;
		String logicalTableAlias = null;

		if(logicalTable instanceof R2RMLTable) {
			logicalTableUnfolded = (SQLFromItem) logicalTable.accept(this);
		} else if(logicalTable instanceof R2RMLSQLQuery) {
			Object logicalTableAux = logicalTable.accept(this);
			if(logicalTableAux instanceof SQLQuery) {
				ZQuery zQuery = (ZQuery) logicalTable.accept(this);
				logicalTableUnfolded = new SQLFromItem(zQuery.toString(), LogicalTableType.QUERY_STRING);
			} else if(logicalTableAux instanceof SQLFromItem) {
				logicalTableUnfolded = (SQLFromItem) logicalTableAux;
			}
		}
		logicalTableAlias = logicalTableUnfolded.generateAlias();
		logicalTable.setAlias(logicalTableAlias);
		//result.addFrom(logicalTableUnfolded);
		result.addFromItem(new SQLJoinTable(logicalTableUnfolded, null, null));

		Collection<String> subjectMapColumnsString = subjectMap.getDatabaseColumnsString();
		if(subjectMapColumnsString != null) {
			new R2RMLUtility();
			
			for(String subjectMapColumnString : subjectMapColumnsString) {
				ZSelectItem selectItem = MorphSQLSelectItem.apply(subjectMapColumnString, logicalTableAlias, dbType);
				
				if(selectItem != null) {
					if(selectItem.getAlias() == null) {
						String alias = "\"" + selectItem.toString() + "\"";
						//selectItem.setAlias(alias);
					}
					resultSelectItems.add(selectItem);
				}
			}
		}
		return result;
	}

	public SQLQuery visit(R2RMLTriplesMap triplesMap) throws Exception {
		logger.info("unfolding triplesMap : " + triplesMap);

		R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
		SQLQuery result = new SQLQuery();
		Collection<ZSelectItem> resultSelectItems = new HashSet<ZSelectItem>();

		//unfold logical table
		R2RMLLogicalTable logicalTable = triplesMap.getLogicalTable();
		String logicalTableAlias = null;

		//unfold subjectMap
		result = this.unfoldSubjectMap(logicalTable, subjectMap, result, resultSelectItems);
		//logicalTableAlias = subjectMap.getAlias();
		logicalTableAlias = triplesMap.getLogicalTable().getAlias();

		Collection<R2RMLPredicateObjectMap> predicateObjectMaps = 
				triplesMap.getPredicateObjectMaps();
		if(predicateObjectMaps != null) {
			for(R2RMLPredicateObjectMap predicateObjectMap : predicateObjectMaps) {
				//unfold predicateMap
				R2RMLPredicateMap predicateMap = predicateObjectMap.getPredicateMap();
				Collection<String> predicateMapColumnsString = predicateMap.getDatabaseColumnsString();
				//if(predicateMapColumnsString != null && logicalTable instanceof R2RMLTable) {
				if(predicateMapColumnsString != null) {
					
					for(String predicateMapColumnString : predicateMapColumnsString) {
						ZSelectItem selectItem = MorphSQLSelectItem.apply(predicateMapColumnString
								, logicalTableAlias, dbType);						
						if(selectItem != null) {
							resultSelectItems.add(selectItem);
						}
					}
				}

				//unfold objectMap
				R2RMLObjectMap objectMap = predicateObjectMap.getObjectMap();
				if(objectMap != null) {
					//objectMap.setAlias(logicalTableAlias);
					Collection<String> objectMapColumnsString = objectMap.getDatabaseColumnsString();
					//if(objectMapColumnsString != null && logicalTable instanceof R2RMLTable) {
					if(objectMapColumnsString != null) {
						for(String objectMapColumnString : objectMapColumnsString) {
							ZSelectItem selectItem = MorphSQLSelectItem.apply(
									objectMapColumnString, logicalTableAlias, this.dbType);
							if(selectItem != null) {
								if(selectItem.getAlias() == null) {
									String alias = "\"" + selectItem.toString() + "\"";
									//selectItem.setAlias(alias);
								}
								resultSelectItems.add(selectItem);
							}
						}
					}
				}

				//unfold refObjectMap
				R2RMLRefObjectMap refObjectMap = predicateObjectMap.getRefObjectMap();
				if(refObjectMap != null) {
					R2RMLLogicalTable parentLogicalTable = refObjectMap.getParentLogicalTable();
					if(parentLogicalTable == null) {
						String errorMessage = "Parent logical table is not found for RefObjectMap : " + predicateObjectMap.getMappedPredicateName();
						throw new Exception(errorMessage);
					}
					SQLLogicalTable sqlParentLogicalTable = 
							(SQLLogicalTable) parentLogicalTable.accept(this);
					
					SQLJoinTable joinQuery = new SQLJoinTable(sqlParentLogicalTable);
					joinQuery.setJoinType("LEFT");
					String joinQueryAlias = sqlParentLogicalTable.generateAlias();
					sqlParentLogicalTable.setAlias(joinQueryAlias);
					//refObjectMap.setAlias(joinQueryAlias);
					this.mapRefObjectMapAlias.put(refObjectMap, joinQueryAlias);
					predicateObjectMap.setAlias(joinQueryAlias);
					
					Collection<String> refObjectMapColumnsString = 
							refObjectMap.getParentDatabaseColumnsString();
					if(refObjectMapColumnsString != null ) {
						for(String refObjectMapColumnString : refObjectMapColumnsString) {
							ZSelectItem selectItem2 = MorphSQLSelectItem.apply(
									refObjectMapColumnString, joinQueryAlias, dbType, null);
							resultSelectItems.add(selectItem2);
						}
					}


					ZExpression onExpression;
					Collection<R2RMLJoinCondition> joinConditions = 
							refObjectMap.getJoinConditions();
					if(joinConditions != null && joinConditions.size() > 0) {
						onExpression = R2RMLUtility.generateJoinCondition(joinConditions
								, logicalTableAlias, joinQueryAlias, dbType);
					} else {
						onExpression = constants.SQL_EXPRESSION_TRUE();
					}
					joinQuery.setOnExpression(onExpression);
					//result.addJoinQuery(joinQuery);		
					result.addFromItem(joinQuery);
				}

			}
		}

		if(resultSelectItems != null) {
			for(ZSelectItem selectItem : resultSelectItems) {
				result.addSelect(selectItem);
			}
		}
		logger.info(triplesMap + " unfolded = \n" + result);

		return result;
	}

	public SQLLogicalTable visit(R2RMLLogicalTable logicalTable) {
		SQLLogicalTable result;
		
		Enum<LogicalTableType> logicalTableType = logicalTable.getLogicalTableType();
		if(logicalTableType == LogicalTableType.TABLE_NAME) {
			result = new SQLFromItem(logicalTable.getValue(), LogicalTableType.TABLE_NAME);
		} else if(logicalTableType == LogicalTableType.QUERY_STRING) {
			String sqlString = logicalTable.getValue();
			try {
				String sqlString2 = sqlString;
				if(!sqlString2.endsWith(";")) {
					sqlString2 += ";";
				}
				result = R2RMLUtility.toSQLQuery(sqlString2);
			} catch(Exception e) {
				logger.warn("Not able to parse the query, string will be used.");
				result = new SQLFromItem(sqlString, LogicalTableType.QUERY_STRING);
			}
		} else {
			result = null;
			logger.warn("Invalid logical table type");
		}

		return result;
	}

	public Object visit(R2RMLRefObjectMap refObjectMap) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(R2RMLObjectMap objectMap) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Set<String> unfold(Set<ILogicalQuery> logicalQueries,
			AbstractMappingDocument mapping) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String unfoldConceptMapping(AbstractConceptMapping triplesMap)
			throws Exception {
		return this.visit((R2RMLTriplesMap) triplesMap).toString();
	}

	@Override
	protected Collection<String> unfold(AbstractMappingDocument mappingDocument) throws Exception {
		Collection<String> result = new HashSet<String>();
		Collection<SQLQuery> queries = this.visit((R2RMLMappingDocument) mappingDocument);
		for(SQLQuery query : queries) {
			result.add(query.toString());
		}
		return result;
	}

	public Object visit(R2RMLTermMap r2rmlTermMap) {
		// TODO Auto-generated method stub
		return null;
	}



}
