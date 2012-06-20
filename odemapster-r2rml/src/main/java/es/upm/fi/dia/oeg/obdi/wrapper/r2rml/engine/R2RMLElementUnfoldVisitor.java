package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZUtils;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.engine.ILogicalQuery;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLJoinCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLSQLQuery;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLElementUnfoldVisitor extends AbstractUnfolder implements R2RMLElementVisitor {
	private static Logger logger = Logger.getLogger(R2RMLElementUnfoldVisitor.class);
	private ConfigurationProperties properties;
	private Map<R2RMLRefObjectMap, String> mapRefObjectMapAlias = new HashMap<R2RMLRefObjectMap, String>();
	
	public Map<R2RMLRefObjectMap, String> getMapRefObjectMapAlias() {
		return mapRefObjectMapAlias;
	}

	private R2RMLElementUnfoldVisitor() {
		ZUtils.addCustomFunction("concat", 2);
		ZUtils.addCustomFunction("substring", 3);
		ZUtils.addCustomFunction("convert", 2);
		ZUtils.addCustomFunction("coalesce", 2);
		ZUtils.addCustomFunction("abs", 1);
		ZUtils.addCustomFunction("lower", 1);
	}

	public R2RMLElementUnfoldVisitor(String configurationDirectory, String configurationFile) {
		this();

		try {
			this.properties = new ConfigurationProperties(configurationDirectory, configurationFile);
		} catch (IOException e) {
			logger.error("IO error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			e.printStackTrace();
		} catch (InvalidConfigurationPropertiesException e) {
			logger.error("invalid configuration error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error("Database error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			//e.printStackTrace();
		}
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
			, SQLQuery result, Collection<SQLSelectItem> resultSelectItems) {
		String logicalTableAlias = null;
		if(logicalTable instanceof R2RMLTable) {
			SQLFromItem logicalTableUnfolded = (SQLFromItem) logicalTable.accept(this);
			logicalTableAlias = logicalTableUnfolded.generateAlias();
			logicalTableUnfolded.setAlias(logicalTableAlias);
			result.addFrom(logicalTableUnfolded);
					
			//subjectMap.setAlias(logicalTableAlias);
			Collection<String> subjectMapColumnsString = subjectMap.getDatabaseColumnsString();
			if(subjectMapColumnsString != null) {
				for(String subjectMapColumnString : subjectMapColumnsString) {
					SQLSelectItem selectItem = R2RMLUtility.toSelectItem(subjectMapColumnString, logicalTableAlias);
					if(selectItem != null) {
						resultSelectItems.add(selectItem);
					}
				}
			}
		} else if(logicalTable instanceof R2RMLSQLQuery) {
			ZQuery zQuery = (ZQuery) logicalTable.accept(this);
			result = new SQLQuery(zQuery);

			if(result.getFrom().size() == 1) {
				ZFromItem fromItem = (ZFromItem) result.getFrom().iterator().next();
				if(fromItem.getAlias() != null) {
					logicalTableAlias = fromItem.getAlias(); 
				} else {
					logicalTableAlias = result.generateAlias();
					//fromItem.setAlias(logicalTableAlias);
					Vector<ZSelectItem> selectItems = result.getSelect();
					for(ZSelectItem selectItem : selectItems) {
						if(!selectItem.isWildcard()) {
							String selectItemAlias = selectItem.getAlias(); 
							if(selectItemAlias != null && !selectItemAlias.equals("")) {
								//selectItem.setAlias(logicalTableAlias + "_" + selectItemAlias);
							} else {
								//selectItem.setAlias(logicalTableAlias + "_" + selectItem.toString());
							}							
						}
							

					}
				}
			}
			
			//logicalTableAlias = result.generateAlias();
//			if(logicalTableAlias != null) {
//				result.setAlias(logicalTableAlias);
//				subjectMap.setAlias(logicalTableAlias);
//			}
			
			
			
			
//
//			Collection<ZSelectItem> selectItems = result.getSelect();
//			for(ZSelectItem selectItem : selectItems) {
//				String selectItemAlias = selectItem.getAlias();
//				if(selectItemAlias != null && selectItemAlias != "") {
//					selectItemAlias = logicalTableAlias + "." + selectItemAlias;
//					selectItem.setAlias(selectItemAlias);
//				} else {
//						selectItemAlias = logicalTableAlias + "." + selectItem.toString();
//						selectItem.setAlias(selectItemAlias);
//				}
//				
//			}
			

//			logicalTableAlias = R2RMLSQLQuery.generateAlias();
//			result = (ZQuery) logicalTable.accept(this);
		}
		logicalTable.setAlias(logicalTableAlias);
		
		return result;
	}
	

	
	public SQLQuery visit(R2RMLTriplesMap triplesMap) {
		logger.info("unfolding triplesMap : " + triplesMap);
		String dbType = this.properties.getDatabaseType();
		
		R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
		SQLQuery result = new SQLQuery();
		Collection<SQLSelectItem> resultSelectItems = new HashSet<SQLSelectItem>();
		
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
				if(predicateMapColumnsString != null && logicalTable instanceof R2RMLTable) {
					for(String predicateMapColumnString : predicateMapColumnsString) {
						SQLSelectItem selectItem = R2RMLUtility.toSelectItem(predicateMapColumnString, logicalTableAlias);
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
					if(objectMapColumnsString != null && logicalTable instanceof R2RMLTable) {
						for(String objectMapColumnString : objectMapColumnsString) {
							SQLSelectItem selectItem = R2RMLUtility.toSelectItem(objectMapColumnString, logicalTableAlias);
							if(selectItem != null) {
								resultSelectItems.add(selectItem);
							}
						}
					}
				}

				
				//unfold refObjectMap
				R2RMLRefObjectMap refObjectMap = predicateObjectMap.getRefObjectMap();
				if(refObjectMap != null) {
					SQLQuery joinQuery = new SQLQuery();
					joinQuery.setJoinType("LEFT");
					String joinQueryAlias = joinQuery.generateAlias();
					joinQuery.setAlias(joinQueryAlias);
					//refObjectMap.setAlias(joinQueryAlias);
					this.mapRefObjectMapAlias.put(refObjectMap, joinQueryAlias);

					R2RMLLogicalTable parentLogicalTable = refObjectMap.getParentLogicalTable();
					SQLLogicalTable sqlParentLogicalTable = 
							(SQLLogicalTable) parentLogicalTable.accept(this);
					joinQuery.addLogicalTable(sqlParentLogicalTable);
					
					Collection<String> refObjectMapColumnsString = 
							refObjectMap.getParentDatabaseColumnsString();
					if(refObjectMapColumnsString != null ) {
						for(String refObjectMapColumnString : refObjectMapColumnsString) {
							SQLSelectItem selectItem = SQLSelectItem.createSQLItem(dbType, refObjectMapColumnString);
							
							String selectItemColumn = selectItem.getColumn();
							String selectItem2Name = joinQueryAlias + "." + selectItemColumn;
							SQLSelectItem selectItem2 = SQLSelectItem.createSQLItem(dbType, selectItem2Name);
							
							String selectItemAlias = joinQueryAlias + "_" + refObjectMapColumnString;
							resultSelectItems.add(selectItem2);
						}
					}
					
					
					ZExp onExpression;
					Collection<R2RMLJoinCondition> joinConditions = 
							refObjectMap.getJoinConditions();
					if(joinConditions != null && joinConditions.size() > 0) {
						onExpression = R2RMLUtility.generateJoinCondition(joinConditions, logicalTableAlias, joinQueryAlias);
					} else {
						ZConstant constantOne = new ZConstant("1", ZConstant.NUMBER);
						onExpression = new ZExpression("=", constantOne, constantOne);
					}
					joinQuery.setOnExp(onExpression);
					result.addJoinQuery(joinQuery);					
				}

			}
		}
		

	
		if(resultSelectItems != null) {
			for(ZSelectItem selectItem : resultSelectItems) {
				result.addSelect(selectItem);
			}
		}
		logger.info(triplesMap + " unfolded = " + result);
		
		return result;
	}

	public SQLLogicalTable visit(R2RMLLogicalTable logicalTable) {
		SQLLogicalTable result = null;
		
		Enum<LogicalTableType> logicalTableType = logicalTable.getLogicalTableType();
		if(logicalTableType == LogicalTableType.TABLE) {
			result = new SQLFromItem(logicalTable.getValue(), LogicalTableType.TABLE);
		} else if(logicalTableType == LogicalTableType.SQLQUERY) {
			String sqlString = logicalTable.getValue();
			if(!sqlString.endsWith(";")) {
				sqlString += ";";
			}
			
			result = R2RMLUtility.toSQLQuery(sqlString);
		} else {
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
