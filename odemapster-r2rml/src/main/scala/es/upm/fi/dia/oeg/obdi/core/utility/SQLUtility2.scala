package es.upm.fi.dia.oeg.obdi.core.utility

import Zql.ZExpression
import Zql.ZConstant
import scala.collection.JavaConversions._
import java.util.Collection
import Zql.ZExp

class SQLUtility2 {

	def replaceExp(oldExp : ZExp, mapReplacement : java.util.Map[ZConstant, ZConstant])  : ZExp= {

			var newExpression : ZExp = null;

			if(mapReplacement.size == 0) { 
				newExpression = oldExp;
			} else {
				val newExpressionAux = SQLUtility2.this.replaceExp(oldExp, mapReplacement.head);
				newExpression = SQLUtility2.this.replaceExp(newExpressionAux, mapReplacement.tail);
			}

			newExpression
	}

	private def replaceExp(oldExp : ZExp, replacementTuple : (ZConstant, ZConstant)) : ZExp = {
		var newExp : ZExp = null;

	val oldExpIsExpression = oldExp.isInstanceOf[ZExpression];
	if(oldExp.isInstanceOf[ZExpression]) {
		val oldExpression = oldExp.asInstanceOf[ZExpression];
		val operator = oldExpression.getOperator();
		val oldOperands = oldExpression.getOperands();
		var newOperands = List[ZExp]();

		for(oldOperand <- oldOperands) {
			var newOperand : ZExp = null;

		if(oldOperand.isInstanceOf[ZConstant]) {
			newOperand = SQLUtility2.this.replaceConstant(oldOperand.asInstanceOf[ZConstant], replacementTuple);
		} else if(oldOperand.isInstanceOf[ZExpression]) {
			newOperand = SQLUtility2.this.replaceExp(oldOperand.asInstanceOf[ZExpression], replacementTuple);
		} else {
			newOperand = null;
		}

		newOperands = newOperand :: newOperands; 
		}

		val newExpression = new ZExpression(operator);
		for(newOperand <- newOperands reverse) yield {
			newExpression.addOperand(newOperand);
		}
		newExp = newExpression;
	} else if(oldExp.isInstanceOf[ZConstant]) {
		val oldConstant = oldExp.asInstanceOf[ZConstant];
		newExp = oldConstant;
	} else {
		newExp = oldExp;
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
				var sqlConstant = new SQLConstant(replacementTuple2);
				if(oldExp.isInstanceOf[SQLConstant]) {
					val columnType = oldExp.asInstanceOf[SQLConstant].columnType;
					if(columnType != null) {
						sqlConstant.columnType = columnType;
					}			    
				}
				sqlConstant;
			} else {
				oldExp;
			}
		}
		newConstant;
	}

}