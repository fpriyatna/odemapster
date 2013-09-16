package es.upm.fi.dia.oeg.obdi.core.utility
import scala.collection.JavaConversions._
import java.util.Collection

class CollectionUtility {
	def mkString(theCollection : Collection[Any], sep:String, start:String, end:String) : String = {
	  theCollection.mkString(start, sep, end);
	}

	def getElementsStartWith(theCollection : Collection[String], prefix:String) : Collection[String] = {
	  var result : List[String] = Nil;
	  for(collectionElement <- theCollection) {
	    if(collectionElement.startsWith(prefix)) {
	      result = result ::: List(collectionElement);
	    }
	  }
	  
	  result;
	} 

}