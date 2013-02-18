package es.upm.fi.dia.oeg.obdi.core.test;


import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.junit.Test;

import junit.framework.TestCase;



import es.upm.fi.dia.oeg.obdi.core.ODEMapsterUtility;

public class EncodingTest extends TestCase {

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
	
	@Test
	public void testEncode01() throws Exception {
		String str = "[\nab,c";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode02() throws Exception {
		String str = "_";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode03() throws Exception {
		String str = "\\";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode04() throws Exception {
		String str = "	?";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode05() throws Exception {
		String str = ",";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode06() throws Exception {
		String str = "'";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode07() throws Exception {
		String str = "abc def";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode08() throws Exception {
		String str = "\"";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode09() throws Exception {
		String str = "http://edu.linkeddata.es/UPM/resource/Actividad/Manual    de la calidad del Laboratorio de Ensayos Qu�micos Industriales , ((LEQIM)), Rev.10";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode10() throws Exception {
		String str = "http://edu.linkeddata.es/UPM/resource/LineaInvestigaci%C3%B3n/31656_Análisis del Sector de las TIC\\s";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode11() throws Exception {
		String str = "http://edu.linkeddata.es/UPM/resource/OtroParticipante/ Gallardo	_Fernando";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode12() throws Exception {
		String str = "http://www.google.com/españa spain#lang=en,es";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode13() throws Exception {
		String str = "http://geo.linkeddata.es/HospitalesMadrid#Hospitál110051";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode14() throws Exception {
		String str = "http://edu.linkeddata.es/UPM/resource/Fecha/31/12/2004";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}

	@Test
	public void testEncode15() throws Exception {
		String str = "http://edu.linkeddata.es/UPM/resource/OtroParticipante/LABORATORIO \"SALVADOR VELAYOS\"_INSTITUTO DE MAGNETISMO APLICADO";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}	
	
	@Test
	public void testEncode16() throws Exception {
		String str = "Say \\r \"Hello World\"";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}	

	@Test
	public void testEncode17() throws Exception {
		String str = "Say \\n \'Hello World\'";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}	

	@Test
	public void testEncode18() throws Exception {
		String str = "Soledad_____Hurtado";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}	
	
	@Test
	public void testEncode19() throws Exception {
		String str = "A & D ARQUITECTURA Y DECORACI�N 2000";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}	

	@Test
	public void testEncode20() throws Exception {
		String str = " ";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("Espacio en blanco (blank space) = " + strEncoded);
	}	

	@Test
	public void testEncode21() throws Exception {
		String str = "�� ��� ���� ���� ���� ���� Carlos   Mol";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}	

	@Test
	public void testEncode22() throws Exception {
		String str = "\"Hello World\" ";
		String strEncoded = ODEMapsterUtility.encodeURI(str);
		System.out.println("strEncoded = " + strEncoded);
	}	

	@Test
	public void testEncode23() throws Exception {
		String str = "I Workshop of POMSEBES ¿Policy oriented measures in support of the evolving Biosystems Engineering Studies in EU-USA\"";
		String strEncoded = ODEMapsterUtility.encodeLiteral(str);
		System.out.println("strEncoded = " + strEncoded);
	}	
	
	@Test
	public void testEncode24() throws Exception {
		String str = "\\A\nTwo-Dimensional Self-Adaptive hp Finite Element Method for the Analysis of\rOpen Region Problems in Electromagnetics.\"";
		System.out.println("str = " + str);
		String strEncoded = ODEMapsterUtility.encodeLiteral(str);
		System.out.println("strEncoded2 = " + strEncoded);
	}	
}
