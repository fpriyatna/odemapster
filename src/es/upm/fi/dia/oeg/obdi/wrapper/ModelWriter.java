package es.upm.fi.dia.oeg.obdi.wrapper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

public class ModelWriter {
	private static Logger logger = Logger.getLogger(ModelWriter.class);
	
	public static void writeModel(Model model, String outputFilename, String rdfLanguage) throws FileNotFoundException {
		try {
			logger.debug("Size of model = " + model.size());
			logger.info("Writing model to " + outputFilename + " ......");
			long startWritingModel = System.currentTimeMillis();
			FileOutputStream resultOutputStream = new FileOutputStream(outputFilename);
			model.write(resultOutputStream, rdfLanguage);
			long endWritingModel = System.currentTimeMillis();
			long durationWritingModel = (endWritingModel-startWritingModel) / 1000;
			logger.info("Writing model time was "+(durationWritingModel)+" s.");
			
		} catch(FileNotFoundException fnfe) {
			logger.error("File " + outputFilename + " can not be found!");
			throw fnfe;			
		}

		
	}
}
