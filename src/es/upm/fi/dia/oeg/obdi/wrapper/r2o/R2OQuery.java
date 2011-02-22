package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.util.Collection;
import java.util.Vector;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

public class R2OQuery extends ZQuery {
	private Collection<R2OJoinQuery> joinQueries;

	public void addSubQuery(R2OJoinQuery joinQuery) {
		if(this.joinQueries == null) {
			this.joinQueries = new Vector<R2OJoinQuery>();
		}

		this.joinQueries.add(joinQuery);


	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		String selectSQL = "";
		Vector<ZSelectItem> mainQuerySelectItems = this.getSelect();
		for(ZSelectItem mainQuerySelectItem : mainQuerySelectItems) {
			String selectItem = mainQuerySelectItem.getExpression().toString();
			String selectItemAlias = mainQuerySelectItem.getAlias();
			if(selectItemAlias != null && selectItemAlias != "") {
				selectItem += " AS " + selectItemAlias;
			}
			selectSQL = selectSQL + selectItem + ", "; 
		}
		//remove the last coma and space
		selectSQL = selectSQL.substring(0, selectSQL.length() - 2);
		result.append("SELECT " + selectSQL + "\n");

		String fromSQL = "";
		Vector<ZFromItem> mainQueryFromItems = this.getFrom();
		for(ZFromItem mainQueryFromItem : mainQueryFromItems) {
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
		//remove the last coma and space
		fromSQL = fromSQL.substring(0, fromSQL.length() - 2);
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

		return result.toString();

	}


}
