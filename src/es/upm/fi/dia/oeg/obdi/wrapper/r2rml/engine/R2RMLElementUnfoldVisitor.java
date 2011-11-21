package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ParseException;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZUtils;
import Zql.ZqlParser;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.ILogicalQuery;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2ODataTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.TranslatorUtility;
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
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;

public class R2RMLElementUnfoldVisitor extends AbstractUnfolder implements R2RMLElementVisitor {
	private static Logger logger = Logger.getLogger(R2RMLElementUnfoldVisitor.class);
	
	public R2RMLElementUnfoldVisitor() {
		ZUtils.addCustomFunction("concat", 2);
		ZUtils.addCustomFunction("substring", 3);
		ZUtils.addCustomFunction("convert", 2);
		ZUtils.addCustomFunction("coalesce", 2);
		ZUtils.addCustomFunction("abs", 1);
		ZUtils.addCustomFunction("lower", 1);
	}
	
	@Override
	public Collection<SQLQuery> visit(R2RMLMappingDocument mappingDocument) {
		Collection<SQLQuery> result = new HashSet<SQLQuery>();
		
		Collection<AbstractConceptMapping> triplesMaps = mappingDocument.getTriplesMaps();
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
					
			subjectMap.setAlias(logicalTableAlias);
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
		
		return result;
	}
	

	
	@Override
	public SQLQuery visit(R2RMLTriplesMap triplesMap) {
		logger.info("unfolding triplesMap : " + triplesMap);
		
		R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
		SQLQuery result = new SQLQuery();
		Collection<SQLSelectItem> resultSelectItems = new HashSet<SQLSelectItem>();
		
		//unfold logical table
		R2RMLLogicalTable logicalTable = triplesMap.getLogicalTable();
		String logicalTableAlias = null;
		
		//unfold subjectMap
		result = this.unfoldSubjectMap(logicalTable, subjectMap, result, resultSelectItems);
		logicalTableAlias = subjectMap.getAlias();

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
					objectMap.setAlias(logicalTableAlias);
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
					refObjectMap.setAlias(joinQueryAlias);
					

					R2RMLLogicalTable parentLogicalTable = refObjectMap.getParentLogicalTable();
					SQLLogicalTable sqlParentLogicalTable = 
							(SQLLogicalTable) parentLogicalTable.accept(this);
					joinQuery.addLogicalTable(sqlParentLogicalTable);
//					String parentLogicalTableAlias = sqlParentLogicalTable.generateAlias();
//					sqlParentLogicalTable.setAlias(parentLogicalTableAlias);
					
//					if(parentLogicalTable.getLogicalTableType() == LogicalTableType.SQLQUERY) {
//						parentLogicalTableAlias = sqlParentLogicalTable.generateAlias();
//						sqlParentLogicalTable.setAlias(parentLogicalTableAlias);
//					} else {
//						parentLogicalTableAlias = joinQuery.generateAlias();
//					}
					
					Collection<String> refObjectMapColumnsString = 
							refObjectMap.getParentDatabaseColumnsString();
					if(refObjectMapColumnsString != null ) {
						for(String refObjectMapColumnString : refObjectMapColumnsString) {
							//SQLSelectItem selectItem = new SQLSelectItem(joinQueryAlias + "." + refObjectMapColumnString);
							SQLSelectItem selectItem = new SQLSelectItem(refObjectMapColumnString);
							String selectItemColumn = selectItem.getColumn();
							String selectItem2Name = joinQueryAlias + "." + selectItemColumn;
							SQLSelectItem selectItem2 = new SQLSelectItem(selectItem2Name);
							String selectItemAlias = joinQueryAlias + "_" + refObjectMapColumnString;
							//selectItem2.setAlias(selectItemAlias);
							resultSelectItems.add(selectItem2);
						}
					}
					
					
					Collection<R2RMLJoinCondition> joinConditions = refObjectMap.getJoinConditions();
					ZExp onExpression = R2RMLUtility.generateJoinCondition(joinConditions, logicalTableAlias, joinQueryAlias);
					if(onExpression != null) {
						joinQuery.setOnExp(onExpression);
					}
//					ZExp onExpression = null;
//					if(joinConditions != null) {
//						for(R2RMLJoinCondition joinCondition : joinConditions) {
//							//String childColumnName = logicalTableAlias + "." + joinCondition.getChildColumnName();
//							String childColumnName = joinCondition.getChildColumnName();
//							SQLSelectItem childSelectItem = new SQLSelectItem(childColumnName);  
//							String[] childColumnNameSplit = childColumnName.split("\\.");
//							if(childColumnNameSplit.length == 1) {
//								childColumnName = logicalTableAlias + "." + childColumnName; 
//							} 
//							ZConstant childColumn = new ZConstant(childColumnName, ZConstant.COLUMNNAME);
//
//							 
//							String parentColumnName = joinQueryAlias + "." + joinCondition.getParentColumnName();
//							ZConstant parentColumn = new ZConstant(parentColumnName, ZConstant.COLUMNNAME);
//							
//							ZExp joinConditionExpression = new ZExpression("=", childColumn, parentColumn);
//							if(onExpression == null) {
//								onExpression = joinConditionExpression;
//							} else {
//								onExpression = new ZExpression("AND", onExpression, joinConditionExpression);
//							}
//						}
//						joinQuery.setOnExp(onExpression);
//					}
					
					
					result.addJoinQuery(joinQuery);					
				}

			}
		}
		
//		Collection<R2RMLRefObjectMap> refObjectMaps = triplesMap.getRefObjectMaps();
//		if(refObjectMaps != null) {
//			for(R2RMLRefObjectMap refObjectMap : refObjectMaps) {
//				R2RMLLogicalTable parentLogicalTable = refObjectMap.getParentLogicalTable();
//				SQLLogicalTable sqlParentLogicalTable = (SQLLogicalTable) parentLogicalTable.accept(this);
//				String parentLogicalTableAlias = sqlParentLogicalTable.generateAlias();
//				sqlParentLogicalTable.setAlias(parentLogicalTableAlias);
//				
//				Collection<String> refObjectMapColumnsString = refObjectMap.getParentDatabaseColumnsString();
//				if(refObjectMapColumnsString != null) {
//					for(String refObjectMapColumnString : refObjectMapColumnsString) {
//						ZSelectItem selectItem = new ZSelectItem(parentLogicalTableAlias + "." + refObjectMapColumnString);
//						result.addSelect(selectItem);
//					}
//				}
//				
//				SQLQuery joinQuery = new SQLQuery();
//				joinQuery.setJoinType("LEFT");
//				joinQuery.addLogicalTable(sqlParentLogicalTable);
//				
//				Collection<R2RMLJoinCondition> joinConditions = refObjectMap.getJoinConditions();
//				if(joinConditions != null) {
//					for(R2RMLJoinCondition joinCondition : joinConditions) {
//						String childColumnName = logicalTableAlias + "." + joinCondition.getChildColumnName();
//						ZConstant childColumn = new ZConstant(childColumnName, ZConstant.COLUMNNAME);
//						
//						String parentColumnName = joinCondition.getParentColumnName();
//						ZConstant parentColumn = new ZConstant(parentLogicalTableAlias + "." + parentColumnName, ZConstant.COLUMNNAME);
//						
//						ZExp joinConditionExpression = new ZExpression("=", childColumn, parentColumn);
//						joinQuery.setOnExp(joinConditionExpression);
//					}
//				}
//				logger.debug("joinQuery = " + joinQuery);
//				result.addJoinQuery(joinQuery);
//			}
//			
//		}
	
		if(resultSelectItems != null) {
			for(ZSelectItem selectItem : resultSelectItems) {
				result.addSelect(selectItem);
			}
		}
		logger.info(triplesMap + " unfolded = " + result);
		
		return result;
	}

	@Override
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

	@Override
	public Object visit(R2RMLRefObjectMap refObjectMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(R2RMLObjectMap objectMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
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

}
