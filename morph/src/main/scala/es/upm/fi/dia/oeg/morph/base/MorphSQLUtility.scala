package es.upm.fi.dia.oeg.morph.base

import Zql.ZExpression
import Zql.ZConstant
import Zql.ZSelectItem
import scala.collection.JavaConversions._
import java.util.Collection
import Zql.ZExp
import com.hp.hpl.jena.graph.Node
import es.upm.fi.dia.oeg.morph.querytranslator.NameGenerator
import org.apache.log4j.Logger
import es.upm.fi.dia.oeg.upm.morph.sql.MorphSQLConstant
import es.upm.fi.dia.oeg.upm.morph.sql.MorphSQLSelectItem

class MorphSQLUtility {
	
}

object MorphSQLUtility {
	val logger = Logger.getLogger("MorphSQLUtility");
  
	def createConstant(v:String, typ:Integer, dbType:String) : ZConstant = {
	  val result = {
		dbType match {
	    	case Constants.DATABASE_MONETDB => {
	    		val monetdbColumn = MorphSQLConstant.apply(v, typ, Constants.DATABASE_MONETDB, null);
	    		monetdbColumn
	    	}
	    	case _ => {
	    		new ZConstant(v, typ);
	    	}
		}	    
	  }
	  result
	}
	
	def getSelectItemByAlias(alias:String , selectItems:Collection[ZSelectItem] , prefix:String ) 
	: Option[ZSelectItem] = {
		var result : Option[ZSelectItem] = None;
		
		if(selectItems != null) {
			for(selectItem <- selectItems) {
			  if(result != null) {
				val selectItemAlias = selectItem.getAlias();
				if(alias.equals(selectItemAlias) || alias.equals(prefix + "." + selectItemAlias)) {
					result = Some(selectItem);
				}				    
			  }
			}
		}
		
		return result;
	}
	
	def addSelectItem(selectItems:Collection[ZSelectItem] , newSelectItem:ZSelectItem) = {
		val newSelectItemAlias = newSelectItem.getAlias();
		val oldSelectItem = this.getSelectItemByAlias(newSelectItemAlias, selectItems, null);
		if (!oldSelectItem.isDefined) {
			selectItems.add(newSelectItem);
		}
	}

	def addSelectItems(selectItems:Collection[ZSelectItem] , newSelectItems:Collection[ZSelectItem]) = {
		for(newSelectItem <- newSelectItems) {
			this.addSelectItem(selectItems, newSelectItem);
		}
	}
	
	def setTableName(selectItems:Collection[MorphSQLSelectItem] , tableName:String) = {
	  for(oldSelectItem <- selectItems) {
		  val dbType = oldSelectItem.dbType;
	      val columnType = oldSelectItem.columnType;
	  }
	}

	def combineExpressions(exp1:ZExp , exp2:ZExp,logicalOperator:String ) : ZExpression = {
		val result:ZExpression = {
			if(exp1 == null && exp2 == null) {
				null;
			} else if(exp1 == null) {
				exp2 match {
				  case zExpression:ZExpression => {
				    zExpression
				  }
				  case _ => {
				    new ZExpression(exp2.toString())
				  }
				}
			} else if (exp2 == null) {
				exp1 match {
				  case zExpression:ZExpression => {
				    zExpression
				  }
				  case _ => {
				    new ZExpression(exp1.toString())
				  }			  
				}
			} else {
				new ZExpression(logicalOperator, exp1, exp2);	
			}		  
		} 
		
		result;
	}
	
	def  combineExpresions(exps:Collection[ZExp] , logicalOperator:String ) : ZExpression = {
		val it = exps.iterator();
		
		val result = {
			if(exps.size() == 0) {
			  null
			} else if(exps.size() == 1) {
				val exp = it.next();
				val resultAux = {
					exp match {
					  case zExpression:ZExpression => {
					    zExpression
					  }
					  case _ => {
					    new ZExpression(it.next().toString());
					  }
					}				  
				}
				resultAux
			} else {
				var resultAux = new ZExpression("AND");
				while(it.hasNext()) {
					resultAux.addOperand(it.next());
				}
				resultAux
			}		  
		}

		result;
	}
	
	def replaceExp(oldExp : ZExp, mapReplacement : java.util.Map[ZConstant, ZConstant])  : ZExp= {
		var newExpression : ZExp = null;

		if(mapReplacement.size == 0) { 
			newExpression = oldExp;
		} else {
			val mapReplacementHead = mapReplacement.head;
			val newExpressionAux = this.replaceExp(oldExp, mapReplacementHead);
			newExpression = this.replaceExp(newExpressionAux, mapReplacement.tail);
		}

		newExpression
	}

	private def replaceExp(oldExp : ZExp, replacementTuple : (ZConstant, ZConstant)) : ZExp = {
		val newExp : ZExp = oldExp match {
		  case oldExpression:ZExpression => {
			val oldExpression = oldExp.asInstanceOf[ZExpression];
			val operator = oldExpression.getOperator();
			val oldOperands = oldExpression.getOperands();
			var newOperands = List[ZExp]();

			for(oldOperand <- oldOperands) {
				val newOperand : ZExp = oldOperand match  {
				  case oldOperandConstant:ZConstant => {
				    this.replaceConstant(oldOperandConstant, replacementTuple);
				  }
				  case oldOperandExpression:ZExpression => {
				    this.replaceExp(oldOperandExpression, replacementTuple);
				  }
				  case _ => {
				    null
				  }
				}
				
				newOperands = newOperand :: newOperands; 
			}

			val newExpression = new ZExpression(operator);
			for(newOperand <- newOperands reverse) yield {
				newExpression.addOperand(newOperand);
			}
			newExpression;
		  }
		  case oldConstant:ZConstant => {
		    this.replaceConstant(oldConstant, replacementTuple);
		  }
		  case _ => {
		    oldExp;
		  }
		}

		newExp;
	}

	private def replaceConstant(oldExp : ZConstant, replacementTuple : (ZConstant, ZConstant) ) : ZConstant = {
		val replacementTuple1 =  replacementTuple._1;
		val replacementTuple2 =  replacementTuple._2;

		val oldExpValue = oldExp.getValue().trim().replaceAll("`", "").replaceAll("\"", "");
		val replacedValue = replacementTuple1.getValue().trim().replaceAll("`", "").replaceAll("\"", "");

		val newConstant : ZConstant = {
			if(oldExpValue.equals(replacedValue)) {
				MorphSQLConstant(replacementTuple2);
			} else {
				oldExp;
			}
		}
		newConstant;
	}
	  
	

	def printSelectItems(selectItems : Collection[ZSelectItem] , distinct : Boolean ) = {
		var selectSQL = "SELECT ";
		
		if(distinct) {
			selectSQL += " DISTINCT ";
		}

		if(selectItems != null && selectItems.size()!=0) {
			var selectItemStringList = List[String]();
			
			for(selectItem <- selectItems) {
				val selectItemAlias = selectItem.getAlias();
				selectItem.setAlias("");
				val aggregationFunction = selectItem.getAggregate();
				
				val selectItemString = {
					if(aggregationFunction == null) {
						selectItem.toString();
					} else {
						aggregationFunction + "(" + selectItem.toString() + ") ";
					}				  
				}

				
				val selectItemStringWithAlias = {
					if(selectItemAlias != null && !selectItemAlias.equals("")) {
						selectItem.setAlias(selectItemAlias);
						selectItemString + " AS \"" + selectItemAlias + "\"";
					} else {
						selectItemString
					}				   
				}
				
				selectItemStringList = selectItemStringList ::: List(selectItemStringWithAlias);
			}
			val selectItemStrings = selectItemStringList.mkString(",");
			selectSQL = selectSQL + selectItemStrings;
		} else {
			selectSQL += " * ";
		}

		selectSQL;		
	}	
}