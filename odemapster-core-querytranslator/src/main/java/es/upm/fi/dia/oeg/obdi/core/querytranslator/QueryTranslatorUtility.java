package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZOrderBy;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.core.DBUtility;
import es.upm.fi.dia.oeg.obdi.core.ODEMapsterUtility;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLFromItem.LogicalTableType;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLLogicalTable;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;


public class QueryTranslatorUtility {
	private static Logger logger = Logger.getLogger(QueryTranslatorUtility.class);
	
	public static boolean isTriplePattern(OpBGP op) {
		int triplesSize = ((OpBGP) op).getPattern().getList().size();
		if(triplesSize == 1) {
			return true;
		} 
		return false;
	}
	
	public static boolean isTriplesSameSubject(List<Triple> triples) {
		if(triples.size() <= 1) {
			return false;
		} else {
			String prevSubject = triples.get(0).getSubject().toString();
			String currSubject;
			for(int i=1; i<triples.size(); i++) {
				currSubject = triples.get(i).getSubject().toString();
				if(!prevSubject.equals(currSubject)) {
					return false;
				} else {
					prevSubject = triples.get(i).getSubject().toString();;
				}
			}
			return true;
		}

	}
	
	public static boolean isTripleBlock(OpBGP bgp) {
		List<Triple> triples = bgp.getPattern().getList();
		return QueryTranslatorUtility.isTriplesSameSubject(triples);
	}

	public static boolean isTripleBlock(Op op) {
		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			return QueryTranslatorUtility.isTripleBlock(bgp);
		} else {
			return false;
		}
	}
	
	public static int getFirstTBEndIndex(List<Triple> triples) {
		int result = 1;
		for(int i=1; i<triples.size()+1; i++) {
			List<Triple> sublist = triples.subList(0, i);
			if(QueryTranslatorUtility.isTriplesSameSubject(sublist)) {
				result = i;
			}
		}

		return result;
	}
	
	public static List<OpBGP> splitBGP(List<Triple> triples) {
		List<OpBGP> result = new Vector<OpBGP>();
		int startIndex = 0;
		int triplesSize = triples.size();
		while(startIndex < triplesSize) {
			List<Triple> triplesSubList = triples.subList(startIndex, triplesSize);
			int endIndex = QueryTranslatorUtility.getFirstTBEndIndex(triplesSubList);
			List<Triple> triplesSubListBGP = triplesSubList.subList(0, endIndex); 
			BasicPattern bp = BasicPattern.wrap(triplesSubListBGP);
			result.add(new OpBGP(bp));
			startIndex += endIndex;
		}
				
		return result;
	}

	public static Op bgpsToJoin(List<OpBGP> bgps) {
		Op result = null;
		if(bgps.size() == 2) {
			result = OpJoin.create(bgps.get(0), bgps.get(1));
		} else if(bgps.size() > 2) {
			Op right = QueryTranslatorUtility.bgpsToJoin(bgps.subList(1, bgps.size()));
			result = OpJoin.create(bgps.get(0), right);
		} else {
			logger.warn("This method should not be used for bgps.size < 2!");
		}
		return result;
	}
	
	public static String getFullyQualifiedName(ZSelectItem selectItem) {
		String result = "";
		
		if(selectItem.getSchema() != null) {
			result += selectItem.getSchema() + ".";
		}
		
		if(selectItem.getTable() != null) {
			result += selectItem.getTable() + ".";
		}

		if(selectItem.getColumn() != null) {
			result += selectItem.getColumn() + ".";
		}

		result = result.substring(0, result.length() - 1);
		return result;
	}
	
	public static SQLQuery eliminateSubQuery(SQLQuery query) throws Exception {
		SQLQuery result = query;
		Collection<SQLLogicalTable> fromItems = query.getLogicalTables();
		
		if(fromItems.size() == 1) {
			SQLLogicalTable fromItem = fromItems.iterator().next();
			if(fromItem instanceof SQLQuery) {
				Map<String, ZSelectItem> mapInnerSelectItems = new HashMap<String, ZSelectItem>();
				SQLQuery fromItemSQLQuery = (SQLQuery) fromItem;
				Vector<ZSelectItem> innerSelectItems = fromItemSQLQuery.getSelect();
				for(ZSelectItem innerSelectItem : innerSelectItems) {
					String innerSelectItemAlias = innerSelectItem.getAlias();
					ZSelectItem innerSelectItemValue = null;
					if(innerSelectItem.isExpression()) {
						innerSelectItemValue = new ZSelectItem();
						innerSelectItemValue.setExpression(innerSelectItem.getExpression());
						if(innerSelectItemAlias == null || innerSelectItemAlias.equals("")) {
							innerSelectItemAlias = innerSelectItem.getExpression().toString();
						}
					} else {
						innerSelectItemValue = innerSelectItem;
						if(innerSelectItemAlias == null || innerSelectItemAlias.equals("")) {
							innerSelectItemAlias = QueryTranslatorUtility.getFullyQualifiedName(innerSelectItem);
						}
					}
					mapInnerSelectItems.put(innerSelectItemAlias, innerSelectItemValue);
				}

				Vector<ZSelectItem> outerSelectItems = query.getSelect();
				Vector<ZSelectItem> newSelectItems = new Vector<ZSelectItem>();
				for(ZSelectItem outerSelectItem : outerSelectItems) {
					ZSelectItem newSelectItem = null;
					if(outerSelectItem.isExpression()) {
						newSelectItem = outerSelectItem;
					} else {
						String outerSelectItemColumn = outerSelectItem.getColumn();
						ZSelectItem innerSelectItemValue = mapInnerSelectItems.get(outerSelectItemColumn);
						newSelectItem = innerSelectItemValue;
						newSelectItem.setAlias(outerSelectItem.getAlias());
					}
					newSelectItems.add(newSelectItem);
				}
				result.setSelectItems(newSelectItems);
				
				Collection<ZFromItem> subQueryFromItems = fromItemSQLQuery.getFrom();
				Collection<SQLLogicalTable> logicalTables = new Vector<SQLLogicalTable>();
				for(ZFromItem subQueryFromItem : subQueryFromItems) {
					SQLLogicalTable logicalTable = new SQLFromItem(
							subQueryFromItem.toString(), LogicalTableType.TABLE_NAME);
					logicalTables.add(logicalTable);
				}
				result.setLogicalTables(logicalTables);
				
				ZExp outerWhereCondition = query.getWhere();
				ZExp newOuterWhereCondition = outerWhereCondition; 
				Iterator<String> mapInnerSelectItemsKeysIterator = mapInnerSelectItems.keySet().iterator(); 
				while(mapInnerSelectItemsKeysIterator.hasNext()) {
					String mapInnerSelectItemKey = mapInnerSelectItemsKeysIterator.next();
					String oldValue = mapInnerSelectItemKey;
					String newValue = "";
					ZSelectItem mapInnerSelectItemValue = mapInnerSelectItems.get(mapInnerSelectItemKey);
					if(mapInnerSelectItemValue.isExpression()) {
						newValue = mapInnerSelectItemValue.getExpression().toString();
					} else {
						newValue = mapInnerSelectItemValue.getTable();
					}
					if(fromItem.getAlias() != null) {
						oldValue = fromItem.getAlias() + "." + oldValue;
					}
					
					newOuterWhereCondition = QueryTranslatorUtility.replaceColumnNames(
							newOuterWhereCondition, oldValue, newValue);
				}
				ZExpression newOuterWhereConditionExpression = (ZExpression) newOuterWhereCondition;
				ZExpression innerWhereCondition = (ZExpression) fromItemSQLQuery.getWhere();
				ZExp newWhereCondition = QueryTranslatorUtility.combineExpressions(
						innerWhereCondition, newOuterWhereConditionExpression);
				//result.set
				result.setWhere(newWhereCondition);
				
			}
			
		}

		return result;
		
	}
	
	private static ZExp replaceColumnNames(ZExp exp, String oldName, String newName) {
		ZExp result = exp;

		if(exp instanceof ZExpression) {
			ZExpression expression = (ZExpression) exp;
			Vector<ZExp> operands = expression.getOperands();
			ZExpression newExpression = new ZExpression(expression.getOperator());
			for(ZExp operand : operands) {
				ZExp newOperand = QueryTranslatorUtility.replaceColumnNames(operand, oldName, newName);
				newExpression.addOperand(newOperand);
			}
			result = newExpression;
		} else if(exp instanceof ZConstant) {
			ZConstant constant = (ZConstant) exp;
			if(constant.getType() == ZConstant.COLUMNNAME) {
				String oldColumnName = constant.getValue();
				if(oldName.equals(oldColumnName)) {
					ZConstant newConstant = new ZConstant(newName, ZConstant.COLUMNNAME);
					result = newConstant;
				}
			}
		} else {
			result = exp;
		}
		
		return result;
	}
	
	public static SQLQuery eliminateSubQuery(Collection<ZSelectItem> newSelectItems
			, SQLQuery query, ZExpression newWhereCondition, Vector<ZOrderBy> orderByConditions) 
					throws Exception {
		Map<String, String> mapOldNewAlias = new HashMap<String, String>();
		SQLQuery result = null;
		
		Collection<SQLQuery> unionQueries = query.getUnionQueries();
		if(unionQueries == null) {
			Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
			Vector<ZSelectItem>	oldSelectItems = query.getSelect();
			
			//SELECT *
			if(newSelectItems.size() == 1 
					&& newSelectItems.iterator().next().toString().equals(("*"))) {
				selectItems2 = new Vector<ZSelectItem>(query.getSelect());
				
				for(ZSelectItem selectItem : oldSelectItems) {
					String selectItemWithoutAlias = DBUtility.getValueWithoutAlias(selectItem);
					String selectItemAlias = selectItem.getAlias();
					newWhereCondition = ODEMapsterUtility.renameColumns(newWhereCondition, selectItemAlias, selectItemWithoutAlias, true); 
				}
			} else {
				String queryAlias = query.generateAlias();
				
				for(ZSelectItem newSelectItem : newSelectItems) {
					String newSelectItemAlias = newSelectItem.getAlias();
					
					String newSelectItemValue = DBUtility.getValueWithoutAlias(newSelectItem);
					
//					String newSelectItemValue2 = queryAlias + "." + newSelectItemValue;
//					newSelectItem = new ZSelectItem(newSelectItemValue2);
//					newSelectItem.setAlias(newSelectItemAlias);
//							
					ZSelectItem oldSelectItem = QueryTranslatorUtility.getSelectItemByAlias(newSelectItemValue, oldSelectItems, queryAlias);
					
					if(oldSelectItem == null) {
						selectItems2.add(newSelectItem);
					} else {
						String oldSelectItemAlias = oldSelectItem.getAlias();
						
						mapOldNewAlias.put(oldSelectItemAlias, newSelectItemAlias);
						
						String oldSelectItemValue = DBUtility.getValueWithoutAlias(oldSelectItem);
						oldSelectItem.setAlias(newSelectItemAlias);
						selectItems2.add(oldSelectItem);
						if(newWhereCondition != null) {
							newWhereCondition = ODEMapsterUtility.renameColumns(newWhereCondition, newSelectItemValue, oldSelectItemValue, true);
						}
					}
				}
				query.setSelectItems(selectItems2);
			}

			query.addWhere(newWhereCondition);

			result = query;
		} else {
			query.setUnionQueries(null);
			SQLQuery query2 = QueryTranslatorUtility.eliminateSubQuery(newSelectItems, query, newWhereCondition, orderByConditions);
			logger.debug("query2 = \n" + query2);
			for(SQLQuery unionQuery : unionQueries) {
				SQLQuery unionQuery2 = QueryTranslatorUtility.eliminateSubQuery(newSelectItems, unionQuery, newWhereCondition, orderByConditions);
				logger.debug("unionQuery2 = \n" + unionQuery2);
				query2.addUnionQuery(unionQuery2);
			}
			
			result = query2;
		}


		
		return result;

	}
	
	private static ZSelectItem getSelectItemByAlias(String alias, Collection<ZSelectItem> selectItems, String prefix) {
		if(selectItems != null) {
			for(ZSelectItem selectItem : selectItems) {
				String selectItemAlias = selectItem.getAlias();
				if(alias.equals(selectItemAlias) || alias.equals(prefix + "." + selectItemAlias)) {
					return selectItem;
				}
			}
		}
		return null;
	}
	
	public static Collection<Node> terms(Op op, boolean ignoreRDFTypeStatement) {
		Collection<Node> result = new HashSet<Node>();

		if(op instanceof OpBGP) {
			OpBGP bgp = (OpBGP) op;
			result = QueryTranslatorUtility.terms(bgp, ignoreRDFTypeStatement);
		} else if(op instanceof OpLeftJoin ) {
			OpLeftJoin leftJoin = (OpLeftJoin) op;
			result.addAll(terms(leftJoin.getLeft(), ignoreRDFTypeStatement));
			result.addAll(terms(leftJoin.getRight(), ignoreRDFTypeStatement));
		} else if(op instanceof OpJoin ) {
			OpJoin opJoin = (OpJoin) op;
			result.addAll(terms(opJoin.getLeft(), ignoreRDFTypeStatement));
			result.addAll(terms(opJoin.getRight(), ignoreRDFTypeStatement));			
		} else if(op instanceof OpFilter) {
			OpFilter filter = (OpFilter) op;
			result.addAll(terms(filter.getSubOp(), ignoreRDFTypeStatement));
		} else if(op instanceof OpUnion) {
			OpUnion opUnion = (OpUnion) op;
			result.addAll(terms(opUnion.getLeft(), ignoreRDFTypeStatement));
			result.addAll(terms(opUnion.getRight(), ignoreRDFTypeStatement));
		}

		return result;
	}

	public static Collection<Node> terms(OpBGP bgp, boolean ignoreRDFTypeStatement) {
		List<Triple> triples = bgp.getPattern().getList();
		return QueryTranslatorUtility.terms(triples, ignoreRDFTypeStatement);
	}

	public static Collection<Node> terms(Collection<Triple> triples, boolean ignoreRDFTypeStatement) {
		Collection<Node> result = new HashSet<Node>();

		for(Triple tp : triples) {
			result.addAll(QueryTranslatorUtility.terms(tp, ignoreRDFTypeStatement));
		}

		return result;
	}

	public static Set<Node> terms(Triple tp, boolean ignoreRDFTypeStatement) {
		Set<Node> result = new HashSet<Node>();
		if(tp.getPredicate().isURI()) {
			if(RDF.type.getURI().equals(tp.getPredicate().getURI())) {
				return result;
			}
		}

		Node subject = tp.getSubject();
		if(subject.isURI() || subject.isBlank() || subject.isLiteral() || subject.isVariable()) {
			result.add(subject);
		}

		Node predicate = tp.getPredicate();
		if(predicate.isURI() || predicate.isBlank() || predicate.isLiteral() || predicate.isVariable()) {
			result.add(predicate);
		}

		Node object = tp.getObject();
		if(object.isURI() || object.isBlank() || object.isLiteral() || object.isVariable()) {
			result.add(object);
		}

		return result;
	}
	
	public static ZExpression combineExpresions(Collection<ZExpression> exps) {
		Iterator<ZExpression> it = exps.iterator();
		ZExpression result = null;
		int expsSize = exps.size();
		
		if(exps.size() == 1) {
			result = it.next();
		} else if(exps.size() > 1) {
			result = new ZExpression("AND");
			while(it.hasNext()) {
				result.addOperand(it.next());
			}
		}
		
		return result;
	}

	
	public static ZExpression combineExpressions(ZExpression exp1, ZExpression exp2) {
		if(exp1 == null) {
			return exp2;
		} 
		if(exp2 == null) {
			return exp1;
		}
		
		return new ZExpression("AND", exp1, exp2);
	}
	
	public static Collection<Node> getSubjects(Collection<Triple> triples) {
		Collection<Node> result = new ArrayList<Node>();
		if(triples != null) {
			for(Triple triple : triples) {
				result.add(triple.getSubject());
			}
		}
		
		return result;
	}
	
	public static Collection<Node> getObjects(Collection<Triple> triples) {
		Collection<Node> result = new ArrayList<Node>();
		if(triples != null) {
			for(Triple triple : triples) {
				result.add(triple.getObject());
			}
		}
		
		return result;
	}
	
	public static int getColumnType(ResultSetMetaData rsmd, String columnName) 
			throws SQLException{
		int result = -1;
		
		int columnCount = rsmd.getColumnCount();
		for(int i=1; i<=columnCount; i++) {
			String rsmdColumnName = rsmd.getColumnName(i);
			if(columnName.equalsIgnoreCase(rsmdColumnName)) {
				result = rsmd.getColumnType(i);
			}
		}
		
		return result;
	}

	public static String getColumnTypeName(ResultSetMetaData rsmd, String columnName) 
			throws SQLException{
		String result = null;
		
		int columnCount = rsmd.getColumnCount();
		for(int i=1; i<=columnCount; i++) {
			String rsmdColumnName = rsmd.getColumnName(i);
			if(columnName.equalsIgnoreCase(rsmdColumnName)) {
				result = rsmd.getColumnTypeName(i);
			}
		}
		
		return result;
	}

	public static boolean isRDFSTypeStatement(Triple tp) {
		Node tpPredicate = tp.getPredicate();
		if(tpPredicate.isURI() && RDF.type.getURI().equals(tpPredicate.getURI())) {
			return true;
		} else {
			return false;
		}
	}

	public static OpBGP reorderTriplesBySubject(OpBGP bgp) {
		try {
			BasicPattern basicPattern = bgp.getPattern();
			Map<Integer, List<Triple>> mapTripleHashCode = new HashMap<Integer, List<Triple>>();
			
			for(Triple tp : basicPattern) {
				Integer tripleSubjectHashCode = new Integer(tp.getSubject().hashCode());
				List<Triple> triplesByHashCode;
				if(mapTripleHashCode.containsKey(tripleSubjectHashCode)) {
					triplesByHashCode = mapTripleHashCode.get(tripleSubjectHashCode);
					
				} else {
					triplesByHashCode = new Vector<Triple>();
					mapTripleHashCode.put(tripleSubjectHashCode, triplesByHashCode);
				}
				triplesByHashCode.add(tp);
			}
			List<Triple> triplesReordered = new Vector<Triple>();
			for(Integer key : mapTripleHashCode.keySet()) {
				List<Triple> triplesByHashCode = mapTripleHashCode.get(key);
				triplesReordered.addAll(triplesByHashCode);
			}
			BasicPattern basicPattern2 = BasicPattern.wrap(triplesReordered);
			OpBGP bgp2 = new OpBGP(basicPattern2);
			return bgp2;			 
		} catch(Exception e) {
			String errorMessage = "Error while reordering triples, original triples will be returned.";
			logger.warn(errorMessage);
			return bgp;
		}
	}
	
	public static OpBGP reorderTriplesBySubjectUsingTransformation(OpBGP bgp) {
		BasicPattern basicPattern = bgp.getPattern();
		ReorderTransformation reorderTransformation = new ReorderSubject();
		BasicPattern basicPattern2 = reorderTransformation.reorder(basicPattern);
		OpBGP bgp2 = new OpBGP(basicPattern2);
		return bgp2;
	}

	public static <T> Set<T> setsIntersection(List<Set<T>> listMapNodeTypes) {
		int listSize = listMapNodeTypes.size();
		
		if(listMapNodeTypes == null || listMapNodeTypes.isEmpty()) {
			return null;
		} else if(listSize == 1) {
			return listMapNodeTypes.get(0);
		} else if(listSize == 2) {
			return QueryTranslatorUtility.setsIntersection(listMapNodeTypes.get(0), listMapNodeTypes.get(1));
		} else {
			Set<T> head = listMapNodeTypes.get(0);
			List<Set<T>> tail = listMapNodeTypes.subList(1, listSize);
			Set<T> tailMerged = QueryTranslatorUtility.setsIntersection(tail);
			return QueryTranslatorUtility.setsIntersection(head, tailMerged);
		}
	}

	public static <T> Set<T> setsIntersection(Set<T> set1, Set<T> set2) {
		Set<T> intersection = new HashSet<T>();
		intersection.addAll(set1);
		intersection.retainAll(set2); 
		return intersection;
	}
	
	public static <T, K> Map<K, Set<T>> mergeMaps(List<Map<K, Set<T>>> maps) {
		int size = maps.size();
		
		if(maps == null || maps.isEmpty()) {
			return null;
		} else if(size == 1) {
			return maps.get(0);
		} else if(size == 2) {
			return QueryTranslatorUtility.mergeMaps(maps.get(0), maps.get(1));
		} else {
			Map<K, Set<T>> head = maps.get(0);
			List<Map<K, Set<T>>> tail = maps.subList(1, size);
			Map<K, Set<T>> tailMerged = QueryTranslatorUtility.mergeMaps(tail);
			return QueryTranslatorUtility.mergeMaps(head, tailMerged);
		}		
	}
			
	public static <T, K> Map<K, Set<T>> mergeMaps(
			Map<K, Set<T>> map1, 
			Map<K, Set<T>> map2) {
		Map<K, Set<T>> result = new HashMap<K, Set<T>>();
		result.putAll(map1);
		
		Set<K> map2Key = map2.keySet();
		for(K map2KeyNode : map2Key) {
			Set<T> map2Values = map2.get(map2KeyNode);
			if(result.containsKey(map2KeyNode)) {
				Set<T> map1Values = map1.get(map2KeyNode);
				Set<T> intersection = QueryTranslatorUtility.setsIntersection(map1Values, map2Values);
				result.put(map2KeyNode, intersection);
			} else {
				result.put(map2KeyNode, map2Values);
			}
		}
		
		return result;
	}

	public static SQLQuery queriesToUnionQuery(List<SQLQuery> sqlQueries) {
		SQLQuery result = null;
		if(sqlQueries != null && sqlQueries.size() > 0) {
			if(sqlQueries.size() == 1) {
				return sqlQueries.get(0);
			} else {
				result = sqlQueries.get(0);
				for(int i = 1; i <sqlQueries.size(); i++) {
					result.addUnionQuery(sqlQueries.get(i));
				}
			}
		}
		
		return result;
	}
}
