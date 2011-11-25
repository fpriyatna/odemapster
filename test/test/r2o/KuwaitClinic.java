package test.r2o;

import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class KuwaitClinic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static void loadODEMapsterResult() throws IOException {
		//Code begins
		Model model = ModelFactory.createOntologyModel();
		String defaultNS = "http://www.q8onto.org/healthcareOntology#";
		String rdfFile = "resources/query01-result.rdf.xml";
		String ontologyFile = "resources/q1-5-13-04-2011.owl";
		String ontologyFile2 = "resources/goodRelations.owl";	

		InputStream fileread = FileManager.get().open(ontologyFile);
		model.read(fileread,defaultNS);
		fileread.close();

		InputStream fileread2 = FileManager.get().open(ontologyFile2);
		model.read(fileread2,defaultNS);
		fileread2.close();

		InputStream modelFile = FileManager.get().open(rdfFile);
		model.read(modelFile, defaultNS);
		modelFile.close();
		//Code ends		
	}
}
