package es.upm.fi.dia.oeg.upm.morph.sql
import Zql.ZSelectItem
import scala.collection.JavaConversions._
import org.apache.log4j.Logger
import Zql.ZExp
import es.upm.fi.dia.oeg.morph.base.Constants

class MorphSQLSelectItem(val dbType:String, schema:String, table:String
    , column:String, val columnType:String) 
extends ZSelectItem {
	
	override def setExpression(arg0 : ZExp ) = {
		super.setExpression(arg0);
//		this.schema = super.getSchema();
//		this.table = super.getTable();
//		this.column = super.getColumn();
	}

	override def hashCode() = {
		super.toString().hashCode();
	}

	override def getSchema() = {
	  this.schema
	}

	override def getTable() = {
	  this.table
	}

	
	override def toString() = {
		var result:String = null;
		
		val databaseType = {
			if(this.dbType == null) {
				Constants.DATABASE_MYSQL;
			} else {
			  this.dbType;
			}	 	  
		}

		val enclosedCharacter :String = {
			if(Constants.DATABASE_MONETDB.equalsIgnoreCase(dbType)) {
				Constants.DATABASE_POSTGRESQL_ENCLOSED_CHARACTER;
			} else if(Constants.DATABASE_POSTGRESQL.equalsIgnoreCase(dbType)) {
				Constants.DATABASE_POSTGRESQL_ENCLOSED_CHARACTER;
			} else {
			  ""
			}		  
		}

		if(this.isExpression()) {
			result = this.getExpression().toString();
		} else {
			result = this.getFullyQualifiedName(enclosedCharacter);
		}

		if(this.columnType != null) {
			if(Constants.DATABASE_POSTGRESQL.equalsIgnoreCase(dbType)) {
				result += "::" + this.columnType; 
			} else if(Constants.DATABASE_MONETDB.equalsIgnoreCase(dbType)) {
			  result = "CAST(" + result + " AS "  + this.columnType + ")"; 
			}		  
		}

		
		val alias = this.getAlias();
		if(alias != null && !alias.equals("")) {
			result += " AS \"" + alias + "\"";
		}
		
		result;
	}


	override def getColumn() = {
		val result : String = {
			if(this.column.startsWith("\"") && this.column.endsWith("\"")) {
				this.column.substring(1, this.column.length()-1);
			} else {
				this.column;
			}		  
		}
		result
	}

	def columnToString() = {
		val result = {
			if(Constants.DATABASE_MONETDB.equalsIgnoreCase(this.dbType)) {
				"\"" + this.getColumn() + "\"";
			} else {
				this.column;
			}		  
		}
		
		result;
	}
	
	def main(args:Array[String]) {
		val selectItem1 = MorphSQLSelectItem("benchmark.product.nr");
		
		val selectItem2 = MorphSQLSelectItem("benchmark.product.label");
		val selectItem3 = MorphSQLSelectItem("benchmark.product.nr");
		selectItem3.setAlias("");
		
		val selectItem4 = MorphSQLSelectItem("product.label");
	}


	def getFullyQualifiedName(enclosedCharacter:String ) = {
		var resultList:List[String] = Nil;
		
		if(this.schema != null) {
			resultList = resultList ::: List(enclosedCharacter + this.schema + enclosedCharacter);
		}
		if(this.table != null) {
			resultList = resultList ::: List(enclosedCharacter + this.table + enclosedCharacter);
		}
		if(this.column != null) {
			resultList = resultList ::: List(enclosedCharacter + this.column + enclosedCharacter);
		}
		
		val result2 = resultList.mkString(".");
		result2
	}
}

object MorphSQLSelectItem {
	val logger = Logger.getLogger("MorphSQLSelectItem");

//	def apply() : SQLSelectItem = {
//		val selectItem = new SQLSelectItem(null, null, null, null, null);
//		selectItem;
//	}
	
	def apply(zExp : ZExp) : MorphSQLSelectItem = {
			
		val result = this(zExp, null, null)
		result
	}

	def apply(zExp : ZExp, pDatabaseType:String, pColumnType:String) : MorphSQLSelectItem = {
		val result = new MorphSQLSelectItem(pDatabaseType, null, null, null, pColumnType)
		result.setExpression(zExp);
		result
	}

	def apply(pInputColumnName:String) : MorphSQLSelectItem = {
		MorphSQLSelectItem(pInputColumnName, null, null, null);
	}

	def apply(pInputColumnName:String, pPrefix:String, pDBType:String) 
		: MorphSQLSelectItem = {
		this(pInputColumnName, pPrefix, pDBType, null);
	}
	
	def apply(pInputColumnName:String, pPrefix:String, pDBType:String , pColumnType:String) 
	: MorphSQLSelectItem = {
	  
		val prefix = {
		  if(pPrefix == null) {
		    null
		  } else if(!pPrefix.endsWith(".")) {
		    pPrefix + ".";
		  } else {
		    pPrefix
		  }
		}
		
		val inputColumnName = {
			if(prefix != null && !prefix.equals("")) {
				prefix + pInputColumnName;
			} else {
			  pInputColumnName
			}		  
		}
		
		val splitColumns = this.splitAndClean(inputColumnName);
		val splitColumnsSize = splitColumns.size;
		var column:String = null;
		var table:String = null;
		var schema:String = null;
		
		splitColumnsSize match {
		  case 1 => { //nr
		    column = splitColumns(0); 
		  }
		  case 2 => { //product.nr
		    table = splitColumns(0);
		    column = splitColumns(1);
		  }
		  case 3 => { //benchmark.product.nr
			schema = splitColumns(0);
			table = splitColumns(1);
			column = splitColumns(2);
		  }
		  case 4 => { //benchmark.dbo.product.nr
			schema = splitColumns(0);
			table = splitColumns(1) + "." + splitColumns(2);
			column = splitColumns(3);
		  }
		  case _ => {
			  logger.warn("Invalid input")
		  }
		}
		
		val columnType = {
			if(pColumnType == null) {
				val splitColumnType = column.split("::");
				if(splitColumnType.length > 1) {
					splitColumnType(1);	
				} else {
				  null
				}			  
			} else {
			  pColumnType
			}
		}
		
		val result = new MorphSQLSelectItem(pDBType, schema, table, column, columnType);
		result		
	}

	def apply(zSelectItem:ZSelectItem) : MorphSQLSelectItem = {
		this(zSelectItem, null, null)
	}

	def apply(zSelectItem:ZSelectItem, pDatabaseType:String) : MorphSQLSelectItem = {
	  this(zSelectItem, pDatabaseType, null)
	}
	
	def apply(zSelectItem:ZSelectItem, pDatabaseType:String, pColumnType:String) : MorphSQLSelectItem = {
		val alias = zSelectItem.getAlias();
		zSelectItem.setAlias("");
		
		val databaseType :String = {
			if(pDatabaseType == null) {
				zSelectItem match {
				  	case selectItem:MorphSQLSelectItem => {
				  		selectItem.dbType ;
				  	}
				  	case _ => { null }
				}
			} else {
			  pDatabaseType
			}
		} 
		
		val columnType :String = {
			if(pColumnType == null) {
				zSelectItem match {
				  	case selectItem:MorphSQLSelectItem => {
				  		selectItem.columnType ;
				  	}
				  	case _ => { null }
				}
			} else {
			  pColumnType
			}
		}
		
		var result = {
			if(zSelectItem.isExpression()) {
				val selectItemExpression = zSelectItem.getExpression();
				MorphSQLSelectItem(selectItemExpression, databaseType, columnType)
			} else {
				MorphSQLSelectItem(zSelectItem.toString(), null, databaseType, columnType)  
			}
		}

		if(alias != null) {
			result.setAlias(alias);
		}
		result;
	}
	
	def splitAndClean(pStr:String ) : Array[String] = {
		val str = {
			if(pStr != null) {
				pStr.trim();
			} else { null }		   
		}

		val result = {
			if(str == null) {
				Array.empty[String]
			} else {
				val str2 = str.replaceAll("`", "").replaceAll("\"", "");
				val splitColumns = str2.split("\\.");
				splitColumns
			}		  
		}

		result
	}	
}