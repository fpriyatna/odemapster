package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLConstants;

public class R2RMLSubjectMap extends R2RMLTermMap {
	private static Logger logger = Logger.getLogger(R2RMLSubjectMap.class);
	
	private Collection<String> classURIs;
	private R2RMLGraphMap graphMap;

	public R2RMLSubjectMap(String constantValue) {
		super(TermMapPosition.SUBJECT, constantValue);
		super.setTermType(R2RMLConstants.R2RML_LITERAL_URI);
	}
	
	public R2RMLSubjectMap(Resource resource) throws R2RMLInvalidTermMapException {
		super(resource, TermMapPosition.SUBJECT);

		
		StmtIterator classStatements = resource.listProperties(R2RMLConstants.R2RML_CLASS_PROPERTY);
		if(classStatements != null) {
			this.classURIs = new HashSet<String>();
			
			while(classStatements.hasNext()) {
				Statement classStatement = classStatements.nextStatement();
				this.classURIs.add(classStatement.getObject().toString());
			}
		}
		
		Statement graphMapStatement = resource.getProperty(R2RMLConstants.R2RML_GRAPHMAP_PROPERTY);
		if(graphMapStatement != null) {
			this.graphMap = new R2RMLGraphMap((Resource) graphMapStatement.getObject());
		}

		Statement graphStatement = resource.getProperty(R2RMLConstants.R2RML_GRAPH_PROPERTY);
		if(graphStatement != null) {
			String constantValueObject = graphStatement.getObject().toString();
			this.graphMap = new R2RMLGraphMap(constantValueObject);
			logger.debug("this.graphMap = " + this.graphMap);
		}

	}

	public Collection<String> getClassURIs() {
		return classURIs;
	}

	public R2RMLGraphMap getGraphMap() {
		return graphMap;
	}
	
}
