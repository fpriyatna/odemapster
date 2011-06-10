package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator.TranslatorUtility;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

public class R2OQuery extends ZQuery {
	private static Logger logger = Logger.getLogger(TranslatorUtility.class);
	private Collection<R2OJoinQuery> joinQueries;
	private Collection<R2OQuery> unionQueries;

	
	public R2OQuery() {
		super();
		this.addSelect(new Vector<ZSelectItem>());
		this.addFrom(new Vector<ZFromItem>()); 
	}

	public void addSelect(ZSelectItem selectItem) {
		if(this.getSelect() == null) {
			this.addSelect(new Vector<ZSelectItem>());
		}
		
		this.getSelect().add(selectItem);
	}
	
	public void addJoinQuery(R2OJoinQuery joinQuery) {
		if(this.joinQueries == null) {
			this.joinQueries = new Vector<R2OJoinQuery>();
		}

		this.joinQueries.add(joinQuery);
	}

	public void addUnionQuery(R2OQuery unionQuery) {
		if(this.unionQueries == null) {
			this.unionQueries = new Vector<R2OQuery>();
		}

		this.unionQueries.add(unionQuery);
	}
	
	public void addFrom(ZFromItem fromItem) {
		if(this.getFrom() == null) {
			this.addFrom(new Vector<ZFromItem>());
		}
		this.getFrom().add(fromItem);
	}
	

	
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		Vector<ZSelectItem> mainQuerySelectItems = (Vector<ZSelectItem>) this.getSelect();
		
		String selectSQL = "SELECT ";
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
		
		return result.toString();

	}
	
	public String generateAlias() {
		return R2OConstants.VIEW_ALIAS + this.hashCode();
	}


}
