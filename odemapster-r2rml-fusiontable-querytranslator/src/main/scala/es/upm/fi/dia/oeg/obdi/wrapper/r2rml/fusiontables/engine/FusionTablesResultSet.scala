package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.fusiontables.engine

import scala.collection.JavaConversions._
import com.google.api.services.fusiontables.model.Sqlresponse
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractResultSet

class FusionTablesResultSet(val rows : List[java.util.List[java.lang.Object]]) 
extends AbstractResultSet {
  val rowsIterator = rows.iterator;
  var activeRow : java.util.List[java.lang.Object] = Nil;
  
  
	def next() : Boolean = {
	  if(this.rowsIterator.hasNext) {
		  this.activeRow = rowsIterator.next();
		  return true;
	  } else {
	    return false;
	  }
	  
	}

	def getString(columnIndex : Int) : String = {
	  return this.activeRow.get(columnIndex).toString();
	}
	
	def getString(columnLabel : String ) : String = {
	  val columnIndex = super.getColumnNames().indexOf(columnLabel);
		return this.getString(columnIndex);
	}
	
}