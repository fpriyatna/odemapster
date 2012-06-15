package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZExp;
import Zql.ZExpression;
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
import com.hp.hpl.jena.vocabulary.RDF;

import es.upm.fi.dia.oeg.obdi.Utility;
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
	
	public static boolean isTripleBlock(List<Triple> triples) {
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
		return QueryTranslatorUtility.isTripleBlock(triples);
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
		for(int i=0; i<triples.size(); i++) {
			List<Triple> sublist = triples.subList(0, i);
			if(QueryTranslatorUtility.isTripleBlock(sublist)) {
				result = i;
			}
		}

		return result;
	}
	
	public static SQLQuery eliminateSubQuery(Collection<ZSelectItem> newSelectItems, SQLQuery query
			, ZExpression newWhereCondition, Vector<ZOrderBy> orderByConditions) throws Exception {
		Map<String, String> mapOldNewAlias = new HashMap<String, String>();
		SQLQuery result = null;
		
		Collection<SQLQuery> unionQueries = query.getUnionQueries();
		if(unionQueries == null) {
			Vector<ZSelectItem> selectItems2 = new Vector<ZSelectItem>();
			Vector<ZSelectItem>	oldSelectItems = query.getSelect();
			
			//SELECT *
			if(newSelectItems.size() == 1 && newSelectItems.iterator().next().toString().equals(("*"))) {
				selectItems2 = new Vector<ZSelectItem>(query.getSelect());
				
				for(ZSelectItem selectItem : oldSelectItems) {
					String selectItemWithoutAlias = Utility.getValueWithoutAlias(selectItem);
					String selectItemAlias = selectItem.getAlias();
					newWhereCondition = Utility.renameColumns(newWhereCondition, selectItemAlias, selectItemWithoutAlias, true); 
				}
			} else {
				String queryAlias = query.generateAlias();
				
				
				for(ZSelectItem newSelectItem : newSelectItems) {
					String newSelectItemAlias = newSelectItem.getAlias();
					String newSelectItemValue = Utility.getValueWithoutAlias(newSelectItem);
					
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
						
						String oldSelectItemValue = Utility.getValueWithoutAlias(oldSelectItem);
						oldSelectItem.setAlias(newSelectItemAlias);
						selectItems2.add(oldSelectItem);
						if(newWhereCondition != null) {
							newWhereCondition = Utility.renameColumns(newWhereCondition, newSelectItemValue, oldSelectItemValue, true);
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
		if(RDF.type.getURI().equals(tp.getPredicate().getURI())) {
			return result;
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
	
	public static ZExp combineExpressions(ZExp exp1, ZExp exp2) {
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
}
