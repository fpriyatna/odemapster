package es.upm.fi.oeg.obdi.core.utility
import scala.collection.JavaConversions._
import java.util.Collection

class CollectionUtility {
	def mkString(theCollection : Collection[Any], sep:String, start:String, end:String) : String = {
	  theCollection.mkString(start, sep, end);
	}

	

}