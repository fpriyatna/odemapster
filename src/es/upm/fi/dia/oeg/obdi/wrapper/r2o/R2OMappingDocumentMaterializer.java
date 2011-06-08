package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.QueryEvaluator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2ODataTranslator;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OUnfolder;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.RelationMappingUnfolderException;

public class R2OMappingDocumentMaterializer {
	private static Logger logger = Logger.getLogger(R2OMappingDocumentMaterializer.class);

	private R2ODataTranslator postProcessor; 	
	private Model model;
	private Writer out;
	
	
	public R2OMappingDocumentMaterializer(R2ODataTranslator postProcessor,
			Model model, Writer out) {
		super();
		this.postProcessor = postProcessor;
		this.model = model;
		this.out = out;
	}


	public void materialize(R2OMappingDocument mappingDocument) throws Exception {
		Collection<AbstractConceptMapping> conceptMappings = 
			mappingDocument.getConceptMappings();
		for(AbstractConceptMapping conceptMapping : conceptMappings) {
			try {
				//unfold mapped concepts
				logger.info("Unfolding concept " + conceptMapping.getConceptName());
				R2OUnfolder unfolder = 
					new R2OUnfolder(mappingDocument, R2ORunner.primitiveOperationsProperties, R2ORunner.configurationProperties);
				String sqlQuery = unfolder.unfoldConceptMapping(conceptMapping);
				if(sqlQuery != null) {
					//evaluate query
					ResultSet rs = QueryEvaluator.evaluateQuery(sqlQuery, R2ORunner.configurationProperties.getConn());

//					postProcessor.processConceptMapping(rs, model, conceptMapping, fileWriter);
					postProcessor.processConceptMapping(rs, model, conceptMapping, out);

					//cleaning up
					Utility.closeStatement(rs.getStatement());
					Utility.closeRecordSet(rs);					
				}


			} catch(SQLException e) {
				String errorMessage = "Error processing " + conceptMapping.getName() + " because " + e.getMessage();
				logger.error(errorMessage);				
				throw e;
			} catch(RelationMappingUnfolderException e) {
				String errorMessage = "Error processing " + conceptMapping.getName() + " because " + e.getMessage();
				logger.error(errorMessage);				
				throw e;				
			} catch(Exception e) {
				//e.printStackTrace();
				String errorMessage = "Error processing " + conceptMapping.getName() + " because " + e.getMessage();
				logger.error(errorMessage);
				throw e;
			}

		}
	}
	
	
}
