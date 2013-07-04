package es.upm.fi.dia.oeg.obdi.odemapster.r2rml.rdb.querytranslator.test;

import java.util.HashSet;
import java.util.Set;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Set<String> theSet1 = new HashSet<String>();
		String str1 = "abc";theSet1.add(str1);
		String str2 = "def";theSet1.add(str2);
		String str5 = "xyz";theSet1.add(str5);
		
		Set<String> theSet2 = new HashSet<String>();
		String str3 = "def";theSet2.add(str3);
		String str4 = "abc";theSet2.add(str4);
		boolean condition = theSet1.containsAll(theSet2);
		System.out.println(condition);
		

	}

}
