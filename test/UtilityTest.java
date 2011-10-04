

import static org.junit.Assert.*;

import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;

import es.upm.fi.dia.oeg.obdi.Utility;

public class UtilityTest {


	@Test
	public void testGetCountryByURI() {
		String uri2 = "http://xmlns.com/foaf/0.1/surname";
		String country2 = Utility.getCountryByURI(uri2);
		System.out.println("country 2 = " + country2);

		String uri3 = "http://xmlns.co.uk/foaf/0.1/surname";
		String country3 = Utility.getCountryByURI(uri3);
		System.out.println("country 3 = " + country3);

		String uri4 = "http://xmlns.co.es/foaf/0.1/surname";
		String country4 = Utility.getCountryByURI(uri4);
		System.out.println("country 4 = " + country4);

	}
	
}
