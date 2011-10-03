package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.TranslatorUtility;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZOrderBy;
import Zql.ZQuery;
import Zql.ZSelectItem;

public class R2OQuery extends ZQuery {
	private static Logger logger = Logger.getLogger(TranslatorUtility.class);
	private Collection<R2OJoinQuery> joinQueries;
	private Collection<R2OQuery> unionQueries;
	private String alias;
	private long slice = -1;
	private boolean distinct = false;

	public R2OQuery() {
		super();
		this.addSelect(new Vector<ZSelectItem>());
		this.addFrom(new Vector<ZFromItem>()); 
	}
	
	public void addFrom(ZFromItem fromItem) {
		if(this.getFrom() == null) {
			this.addFrom(new Vector<ZFromItem>());
		}
		this.getFrom().add(fromItem);
	}

	public void addJoinQuery(R2OJoinQuery joinQuery) {
		if(this.joinQueries == null) {
			this.joinQueries = new Vector<R2OJoinQuery>();
		}

		this.joinQueries.add(joinQuery);
	}
	
	public void addSelect(ZSelectItem newSelectItem) {
		Collection<ZSelectItem> selectItems = this.getSelect();
		if(selectItems == null) {
			this.addSelect(new Vector<ZSelectItem>());
		}
		
		String newSelectItemAlias = newSelectItem.getAlias();
		newSelectItem.setAlias("");
		
		boolean alreadySelected = false;
		for(ZSelectItem selectItem : selectItems) {
			String selectItemAlias = selectItem.getAlias();
			selectItem.setAlias("");
			if(selectItemAlias != null && !selectItemAlias.equals("")) {
				if(selectItemAlias.equalsIgnoreCase(newSelectItemAlias)) {
					alreadySelected = true;
					logger.warn(selectItemAlias + " already selected");
				}
				selectItem.setAlias(selectItemAlias);
			}			
		}
		
		if(newSelectItemAlias != null && !newSelectItemAlias.equals("")) {
			newSelectItem.setAlias(newSelectItemAlias);
		}
		
		if(!alreadySelected) {
			selectItems.add(newSelectItem);
		}
		
	}

	public void addUnionQuery(R2OQuery unionQuery) {
		if(this.unionQueries == null) {
			this.unionQueries = new Vector<R2OQuery>();
		}

		this.unionQueries.add(unionQuery);
	}
	
	public void addWhere(Collection<ZExp> newWheres) {
		for(ZExp newWhere : newWheres) {
			this.addWhere(newWhere);
		}
		
	}
	
	public void addWhere(ZExp newWhere) {
		ZExp oldWhere = this.getWhere();
		if(newWhere != null) {
			if(oldWhere == null) {
				super.addWhere(newWhere);
			} else {
				ZExp combinedWhere = new ZExpression("AND", oldWhere, newWhere);
				super.addWhere(combinedWhere);
			}
		}
	}
	
	public void clearSelectItems() {
		Collection<ZSelectItem> selectItems = this.getSelect();
		if(selectItems != null) {
			selectItems.clear();
		}
		selectItems = new Vector<ZSelectItem>();
	}

	public String generateAlias() {
		//return R2OConstants.VIEW_ALIAS + this.hashCode();
		if(this.alias == null) {
			this.alias = R2OConstants.VIEW_ALIAS + new Random().nextInt(10000);
		}
		return this.alias;
	}
	
	public ZSelectItem getSelectItemByAlias(String alias) {
		Iterator selectItems = this.getSelect().iterator();
		while(selectItems.hasNext()) {
			ZSelectItem selectItem = (ZSelectItem) selectItems.next();
			String selectItemAlias = selectItem.getAlias();
			if(alias.equals(selectItemAlias)) {
				return selectItem;
			}
		}
		return null;
	}
	

	
	public ZSelectItem getSelectItemByColumnValue(String value) {
		Iterator selectItems = this.getSelect().iterator();
		while(selectItems.hasNext()) {
			ZSelectItem selectItem = (ZSelectItem) selectItems.next();
			String selectItemFullValue = Utility.getValueWithoutAlias(selectItem);
			String splitValues[] = selectItemFullValue.split("\\.");
			String columnValue = splitValues[splitValues.length-1];
			if(value.equals(columnValue)) {
				return selectItem;
			}
		}
		return null;
	}
	
	public ZSelectItem getSelectItemByValue(String value) {
		Iterator selectItems = this.getSelect().iterator();
		while(selectItems.hasNext()) {
			ZSelectItem selectItem = (ZSelectItem) selectItems.next();
			if(value.equals(Utility.getValueWithoutAlias(selectItem))) {
				return selectItem;
			}
		}
		return null;
	}

	public Collection<R2OQuery> getUnionQueries() {
		return unionQueries;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public void setSelectItems(Collection<ZSelectItem> newSelectItems) {
		this.clearSelectItems();
		
		for(ZSelectItem newSelectItem : newSelectItems) {
			this.addSelect(newSelectItem);
		}
	}

	public void setSlice(long slice) {
		this.slice = slice;
	}
	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		Vector<ZSelectItem> mainQuerySelectItems = (Vector<ZSelectItem>) this.getSelect();
		
		String selectSQL = "SELECT ";
		if(this.distinct) {
			selectSQL += " DISTINCT ";
		}
		
		if(mainQuerySelectItems != null && mainQuerySelectItems.size()!=0) {
			for(ZSelectItem mainQuerySelectItem : mainQuerySelectItems) {
				String selectItemAlias = mainQuerySelectItem.getAlias();
				mainQuerySelectItem.setAlias("");
				String selectItemString = mainQuerySelectItem.toString();
				if(selectItemAlias != null && !selectItemAlias.equals("")) {
					selectItemString += " AS " + selectItemAlias;
					mainQuerySelectItem.setAlias(selectItemAlias);
				}
				selectSQL = selectSQL + selectItemString + ", "; 
			}
			//remove the last coma and space
			selectSQL = selectSQL.substring(0, selectSQL.length() - 2);
		}
		result.append(selectSQL + "\n");


		String fromSQL = "";
		Vector<ZFromItem> mainQueryFromItems = this.getFrom();
		if(mainQueryFromItems != null && mainQueryFromItems.size()!=0) {
			for(ZFromItem mainQueryFromItem : mainQueryFromItems) {
				if(mainQueryFromItem instanceof R2OFromItem) {


					String alias = mainQueryFromItem.getAlias();
					mainQueryFromItem.setAlias("");
					fromSQL += "(" + mainQueryFromItem + ") " + alias + ", ";
					if(alias != null) {
						mainQueryFromItem.setAlias(alias);
					}
				} else {
					//fromSQL += mainQueryFromItem.getTable();
					//fromSQL += mainQueryFromItem.toString();
					
					if(mainQueryFromItem.getSchema() != null) {
						fromSQL += mainQueryFromItem.getSchema() + ".";
					}
					if(mainQueryFromItem.getTable() != null) {
						fromSQL += mainQueryFromItem.getTable() + ".";
					}
					if(mainQueryFromItem.getColumn() != null) {
						fromSQL += mainQueryFromItem.getColumn() + ".";
					}
					fromSQL = fromSQL.substring(0, fromSQL.length()-1);
								
					
					String mainQueryFromItemAlias = mainQueryFromItem.getAlias();
					if(mainQueryFromItemAlias != null && mainQueryFromItemAlias.length() > 0) {
						//fromSQL += " AS " + mainQueryFromItem.getAlias() + ", ";
						
						fromSQL += " " + mainQueryFromItem.getAlias() + ", ";
					} else {
						fromSQL += ", ";
					}
				}

			}
			//remove the last coma and space
			fromSQL = fromSQL.substring(0, fromSQL.length() - 2);
		}
		result.append("FROM " + fromSQL + "\n");

		if(this.joinQueries != null) {
			for(R2OJoinQuery joinQuery : this.joinQueries) {
				result.append(joinQuery.toString() + "\n");
			}				
		}

		String whereSQL = null;
		if(this.getWhere() != null) {
			whereSQL = this.getWhere().toString();
			result.append("WHERE " + whereSQL + "\n"); 
		}

		String unionSQL = "UNION ";
		if(this.unionQueries != null) {
			for(R2OQuery unionQuery : this.unionQueries) {
				result.append(unionSQL + unionQuery.toString());
			}
		}
		
		Vector<ZOrderBy> orderByConditions = this.getOrderBy(); 
		if(orderByConditions != null) {
			String orderBySQL = "";
			for(Object orderByObject : orderByConditions) {
				ZOrderBy orderBy = (ZOrderBy) orderByObject;
				orderBy.getAscOrder();
				ZExp orderByExpression = orderBy.getExpression();
				orderBySQL += orderByExpression;
				if(! orderBy.getAscOrder()) {
					orderBySQL += " DESC";
				}
				orderBySQL += ", ";
			}
			orderBySQL = orderBySQL.substring(0, orderBySQL.length() - 2);
			orderBySQL = "ORDER BY " + orderBySQL ; 
			result.append(orderBySQL + "\n");
			
		}
		
		if(this.slice != -1) {
			result.append("LIMIT " + this.slice);
		}
		return result.toString();

	}

	public void setUnionQueries(Collection<R2OQuery> unionQueries) {
		this.unionQueries = unionQueries;
	}
	
}
