package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Condition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.ConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Restriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Selector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.Restriction.RestrictionType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OConceptMapping;

public class R2OModelGenerator {
	private static Logger logger = Logger.getLogger(R2OModelGenerator.class);
	
	public void generateModel(ResultSet rs, Model model, AbstractConceptMapping conceptMapping) throws Exception {
		logger.info("generating model.......");
		long startGeneratingModel = System.currentTimeMillis();
		
		R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping;
		long counter = 0;
		while(rs.next()) {
			String uri = rs.getString("uri");

			
			boolean conditionAppliesIf = this.processConceptMappingAppliesIfElement(r2oConceptMapping, rs);
			//boolean conditionAppliesIf = true;
			
			if(conditionAppliesIf) {
				uri = Utility.encodeURI(uri);

				if(counter % 10000 == 0) {
					logger.info("Current record (" + counter + ") = " + uri);
				}
				counter++;

				Resource subject = model.createResource(uri);
				Resource object = model.getResource(conceptMapping.getName());
				Statement statement = model.createStatement(subject, RDF.type, object);
				model.add(statement);
				
				//logger.info("Processing property mappings.");
				this.processPropertyMappings(r2oConceptMapping, rs, model, subject);
			}
			


		}
		logger.info(counter + " retrieved.");

		
		
		long endGeneratingModel = System.currentTimeMillis();
		long durationGeneratingModel = (endGeneratingModel-startGeneratingModel) / 1000;
		logger.info("Generating model time was "+(durationGeneratingModel)+" s.");
		

	}
	
	private void processPropertyMappings(R2OConceptMapping r2oConceptMapping, ResultSet rs, Model model, Resource subject) throws Exception {
		List<R2OAttributeMapping> attributeMappings = r2oConceptMapping.getAttributeMappings();
		for(R2OAttributeMapping attributeMapping : attributeMappings) {
			//logger.info("Processing attribute mappings : " + attributeMapping.getName());
			this.processAttributeMapping(attributeMapping, rs, model, subject);
		}
			
	}
	
	private void processAttributeMapping(R2OAttributeMapping attributeMapping, ResultSet rs, Model model, Resource subject) throws Exception {
		String propName = attributeMapping.getName();
		
		String dbColUsed = attributeMapping.getUseDBCol();
		if(dbColUsed != null) {
			
		} else {
			Collection<Selector> attributeMappingSelectors = attributeMapping.getSelectors();
			if(attributeMappingSelectors != null) {
				for(Selector attributeMappingSelector : attributeMappingSelectors) {
					ConditionalExpression attributeMappingSelectorAppliesIf = attributeMappingSelector.getAppliesIf();
					boolean attributeMappingSelectorAppliesIfValue = false;
					if(attributeMappingSelectorAppliesIf == null) {
						//if there is no applies-if specified in the selector, then the condition is true
						attributeMappingSelectorAppliesIfValue = true;
					} else {
						//else, evaluate the applies-if condition
						attributeMappingSelectorAppliesIfValue = this.processConditionalExpression(attributeMappingSelectorAppliesIf, rs);
					}
					
					if(attributeMappingSelectorAppliesIfValue) {
						String alias = attributeMapping.getId() + attributeMappingSelector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
						String propValString = rs.getString(alias);
						
						if(propValString!= null && propValString != "" && !propValString.equals("")) {
							if(propName.equalsIgnoreCase(RDFS.label.toString())) { //special case of rdfs:label
								int lastAtIndex = propValString.lastIndexOf("@");
								Literal literal = null;
								if(lastAtIndex > 0) {
									String propertyValue = propValString.substring(0, lastAtIndex);
									String language = propValString.substring(lastAtIndex+1, propValString.length());
									literal = model.createLiteral(propertyValue, language);
								} else {
									literal = model.createLiteral(propValString);
								}
								subject.addProperty(RDFS.label, literal);
							} else {
								subject.addProperty(model.createProperty(propName), model.createLiteral(propValString));
							}							
						}
					}
				}
				
			}
		}
		
	}

	private void processRelationMapping() {
		
	}

	private boolean processConceptMappingAppliesIfElement(R2OConceptMapping r2oConceptMapping, ResultSet rs) throws Exception {
		boolean result = false;
		ConditionalExpression appliesIf = r2oConceptMapping.getAppliesIf();
		ConditionalExpression appliesIfTop = r2oConceptMapping.getAppliesIfTop();
		
		if(appliesIf == null && appliesIfTop == null) {
			result = true;
		} else {
			if(appliesIf != null) {
				return this.processConditionalExpression(appliesIf, rs);
			} else {
				if(appliesIfTop != null) {
					return this.processConditionalExpression(appliesIfTop, rs);
				}
			}			
		}
		
		return result;

	}
	
	/*
	private boolean processOrConditionalExpression(OrConditionalExpression orConditionalExpression, ResultSet rs) throws Exception {
		boolean result = false;
		
		if(orConditionalExpression.isUsingOr()) { //the case of : OR orcond-expr condition
			Condition condition = orConditionalExpression.getCondition();
			boolean result1 = this.processCondition(condition, rs);
			if(result1) {
				return true;
			}
			
			OrConditionalExpression orCondExpression = orConditionalExpression.getOrCondExpr();
			boolean result2 = this.processOrConditionalExpression(orCondExpression, rs);
			if(result2) {
				return true;
			}
			
			//false at this point
			return false;
		} else { //the case of : condition
			Condition condition = orConditionalExpression.getCondition();
			result = this.processCondition(condition, rs);
		}
		
		return result;
	}
	*/
	
	private boolean processConditionalExpression(ConditionalExpression conditionalExpression, ResultSet rs) throws Exception {
		boolean result = false;
		String conditionalExpressionOperator = conditionalExpression.getOperator();
		
		if(conditionalExpressionOperator == null) {
			result = this.processCondition(conditionalExpression.getCondition(), rs);
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.AND_TAG)) {
			for(ConditionalExpression condExpr : conditionalExpression.getCondExprs()) {
				boolean condition = this.processConditionalExpression(condExpr, rs);
				if(condition == false) {
					return false;
				}
			}
			
			return true;
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.OR_TAG)) {
			for(ConditionalExpression condExpr : conditionalExpression.getCondExprs()) {
				boolean condition = this.processConditionalExpression(condExpr, rs);
				if(condition == true) {
					return true;
				}
			}
			
			return false;
		}
		
		return result;
	}
	
	private boolean processCondition(Condition condition, ResultSet rs) throws Exception {
		boolean result = false;
		
		String operationId = null;
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(condition.getPrimitiveCondition())) {
			operationId = condition.getOperId();
		} else {
			operationId = condition.getPrimitiveCondition();
		}
		
		//delegeable : already done in database / query-unfolder level
		if(Utility.inArray(R2OConstants.DELEGABLE_OPERATIONS, operationId)) {
			result = true;
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_MATCH_REGEXP_NAME.equalsIgnoreCase(operationId)) {
			result = this.processMatchRegExpConditionalExpression(condition, rs);
		}
		
		return result;
	}
	

	private boolean processMatchRegExpConditionalExpression(Condition condition, ResultSet rs) throws Exception {
		Restriction inputStringRestriction = condition.getArgRestricts(R2OConstants.ONPARAM_STRING);
		String inputString = this.processRestriction(inputStringRestriction, rs);
		Restriction regexRestriction = condition.getArgRestricts(R2OConstants.ONPARAM_REGEXP);
		String regex = this.processRestriction(regexRestriction, rs);
		
		if(inputString != null && regex != null) {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(inputString);
			if(matcher.find()) {
                return true;
            } else {
            	return false;
            }
		} else {
			return false;
		}
	}
	
	/*
	private boolean processEqualsConditionalExpression(Condition condition, ResultSet rs) throws Exception {
		List<ArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		Restriction restriction0 = argumentRestrictions.get(0).getRestriction();
		String operand0 = this.processRestriction(restriction0, rs);
		Restriction restriction1 = argumentRestrictions.get(1).getRestriction();
		String operand1 = this.processRestriction(restriction1, rs);

		if(operand0 != null && operand1 != null) {
			return operand0.equals(operand1);
		} else {
			return false;
		}
	}
	*/
	
	private String processRestriction(Restriction restriction, ResultSet rs) throws Exception {
		String result = null;
		if(restriction.getRestrictionType() == RestrictionType.HAS_COLUMN) {
			result = rs.getString(restriction.getHasColumn().replaceAll("\\.", "_"));
		} else if(restriction.getRestrictionType() == RestrictionType.HAS_VALUE) {
			result = restriction.getHasValue();
		} else if(restriction.getRestrictionType() == RestrictionType.HAS_TRANSFORMATION) {
			
		}
		
		 
		return result;
	}

	public Model createMemoryModel() {
		return ModelFactory.createDefaultModel();
	}

	public Model createHSQLDBModel() {
		try {
			String className = "org.hsqldb.jdbcDriver";       // path of driver class
			Class.forName(className);                        // Load the Driver
			String DB_URL =    "jdbc:hsqldb:file:testdb4";   // URL of database 
			String DB_USER =   "sa";                          // database user id
			String DB_PASSWD = "";                            // database password
			String DB =        "HSQL";                        // database type

			// Create database connection
			IDBConnection conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
			ModelMaker maker = ModelFactory.createModelRDBMaker(conn) ;

			// create or open the default model
			Model model = maker.createDefaultModel();

			// Close the database connection
			conn.close();

			return model;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}



	public Model createTDBModel(String databaseName) {
		String tdbDatabaseFolder = "tdb-database";
		
		File folder = new File(tdbDatabaseFolder);
		if(!folder.exists()) {
			folder.mkdir();
		}
		
		return TDBFactory.createModel(tdbDatabaseFolder + "/" + databaseName) ;	

	}
	
	public Model createModel(String jenaMode, String jenaDatabaseName) {
		Model model = null;

		if(jenaMode == null) {
			//logger.warn("Unspecified jena mode, memory based will be used!");
			model = this.createMemoryModel();
		} else {
			if(jenaMode.equalsIgnoreCase(R2OConstants.JENA_MODE_TYPE_HSQL)) {
				//logger.debug("jena mode = idb hsqldb");
				model = this.createHSQLDBModel();
			} else if(jenaMode.equalsIgnoreCase(R2OConstants.JENA_MODE_TYPE_TDB)) {
				//logger.debug("jena mode = tdb");
				this.createTDBModel(jenaDatabaseName);
			} else if (jenaMode.equalsIgnoreCase(R2OConstants.JENA_MODE_TYPE_MEMORY)){
				//logger.debug("jena mode = memory");
				model = this.createMemoryModel();
			} else {
				//logger.warn("invalid mode of jena type, memory mode will be used.");
				model = this.createMemoryModel();
			}				
		}		
		
		return model;
	}
}
