package es.upm.fi.oeg.obdi.core.utility

import Zql.ZExpression
import Zql.ZConstant
import scala.collection.JavaConversions._
import java.util.Collection
import Zql.ZExp

class SQLUtility2 {

	def replaceExp(oldExp : ZExp, mapReplacement : java.util.Map[ZConstant, ZConstant])  : ZExp= {
			val newExpression = {
					if(mapReplacement.size == 0) { 
						oldExp;;
					} else {
						val newExpressionAux = SQLUtility2.this.replaceExp(oldExp, mapReplacement.head);
						SQLUtility2.this.replaceExp(newExpressionAux, mapReplacement.tail);
					}
			}

			newExpression
	}

	private def replaceExp(oldExp : ZExp, mapReplacement : (ZConstant, ZConstant)) : ZExp = {
		val newExp : ZExp = {
		if(oldExp.isInstanceOf[ZExpression]) {
			val oldExpression = oldExp.asInstanceOf[ZExpression];
			val operator = oldExpression.getOperator();
			val oldOperands = oldExpression.getOperands();
			var newOperands = List[ZExp]();

			for(oldOperand <- oldOperands) yield {

				val newOperand = {
						if(oldOperand.isInstanceOf[ZConstant]) {
							SQLUtility2.this.replaceConstant(oldOperand.asInstanceOf[ZConstant], mapReplacement);

						} else if(oldOperand.isInstanceOf[ZExpression]) {
							SQLUtility2.this.replaceExp(oldOperand.asInstanceOf[ZExpression], mapReplacement);;
						} else {

							null;
						}
				}
				newOperands = newOperand :: newOperands; 
			}

			val newExpression = new ZExpression(operator);
			for(newOperand <- newOperands reverse) yield {
				newExpression.addOperand(newOperand);
			}
			newExpression;
		} else if(oldExp.isInstanceOf[ZConstant]) {
			val oldConstant = oldExp.asInstanceOf[ZConstant];
			oldConstant;
		} else {
			oldExp;
		}
	}

	newExp;

	}

	private def replaceConstant(oldExp : ZConstant, mapReplacement : (ZConstant, ZConstant) ) : ZConstant = {
		val newExpression : ZConstant =  
				if(oldExp.getValue().equals(mapReplacement._1.getValue())) {
					mapReplacement._2;
				} else {
					oldExp
				}


	newExpression;
	}

}