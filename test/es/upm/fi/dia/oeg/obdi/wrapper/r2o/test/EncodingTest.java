package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;



import es.upm.fi.dia.oeg.obdi.Utility;

public class EncodingTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String originalString = "http://www.google.com/Hello World# abc";
		String encodedString = URLEncoder.encode(originalString, "UTF-8");
		encodedString = encodedString.replaceAll("%3A", ":");
		encodedString = encodedString.replaceAll("%2F", "/");
		encodedString = encodedString.replaceAll("%23", "#");
		
		System.out.println("encodedString = " + encodedString);
		

	}

}
