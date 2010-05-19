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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.wrapper.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OCondition;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction.RestrictionType;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2ORelationMapping;

public class R2OPostProcessor {
	private static Logger logger = Logger.getLogger(R2OPostProcessor.class);
	private static final String ATTRIBUTE_MAPPING_SELECTOR = "attributemappingselector";
	private static final String CONCEPT_MAPPING_APPLIES_IF = "conceptmappingappliesif";

	public void generateModel(ResultSet rs, Model model, AbstractConceptMapping conceptMapping) throws Exception {
		String conceptName = conceptMapping.getConceptName();
		logger.info("generating model  for " + conceptName);
		long startGeneratingModel = System.currentTimeMillis();

		
		R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping;
		Resource object = model.getResource(conceptName);
		
		long counter = 0;
		while(rs.next()) {
			boolean conditionAppliesIf = 
				this.processConceptMappingAppliesIfElement(r2oConceptMapping, rs);

			if(conditionAppliesIf) {
				String uri = rs.getString(r2oConceptMapping.getId() + "_uri");
				uri = Utility.encodeURI(uri);

				if(counter % 10000 == 1000) {
					logger.info("Processing record no " + counter + " : " + uri);
				}
				counter++;

				Resource subject = model.createResource(uri);
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
		List<R2OPropertyMapping> propertyMappings = r2oConceptMapping.getPropertyMappings();
		if(propertyMappings != null) {
			for(R2OPropertyMapping propertyMapping : propertyMappings) {
				if(propertyMapping instanceof R2OAttributeMapping) {
					try {
						this.processAttributeMapping((R2OAttributeMapping) propertyMapping, rs, model, subject);
					} catch(Exception e) {
						String newErrorMessage = e.getMessage() + " while processing attribute mapping " + propertyMapping.getId();
						logger.error(newErrorMessage);
						throw e;
					}

				} else if(propertyMapping instanceof R2ORelationMapping) {
					try {
						this.processRelationMapping((R2ORelationMapping) propertyMapping, rs, model, subject);
					} catch(Exception e) {
						String newErrorMessage = e.getMessage() + " while processing relation mapping " + propertyMapping.getId();
						logger.error(newErrorMessage);
						throw e;
					}

				}
			}			
		}
		




	}

	private void addDataTypeProperty(String propName, String propValString, Model model, Resource subject) {
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

	private void processRelationMapping(R2ORelationMapping r2oRelationMapping, ResultSet rs, Model model, Resource subject) throws Exception {
		String relationName = r2oRelationMapping.getRelationName();
		Property property = model.createProperty(relationName);
		String rangeURI = rs.getString(r2oRelationMapping.getId());
		if(rangeURI != null) {
			Resource object = model.createResource(rangeURI);
			Statement rdfstmtInstance = model.createStatement(subject,property, object);
			model.add(rdfstmtInstance);
		}

	}


	
	private void processAttributeMapping(R2OAttributeMapping attributeMapping, ResultSet rs, Model model, Resource subject) throws Exception {
		String propName = attributeMapping.getName();

		String dbColUsed = attributeMapping.getUseDBCol();
		if(dbColUsed != null) {
			String propValString = (String) this.processDBColumn(attributeMapping.getUseDBCol(), attributeMapping.getUseDBColDatatype(), rs);
			this.addDataTypeProperty(propName, propValString, model, subject);
		} else {
			Collection<R2OSelector> attributeMappingSelectors = attributeMapping.getSelectors();
			if(attributeMappingSelectors != null) {
				for(R2OSelector attributeMappingSelector : attributeMappingSelectors) {
					R2OConditionalExpression attributeMappingSelectorAppliesIf = attributeMappingSelector.getAppliesIf();
					boolean attributeMappingSelectorAppliesIfValue = false;
					if(attributeMappingSelectorAppliesIf == null) {
						//if there is no applies-if specified in the selector, then the condition is true
						attributeMappingSelectorAppliesIfValue = true;
					} else {
						//else, evaluate the applies-if condition
						attributeMappingSelectorAppliesIfValue = 
							this.processConditionalExpression(attributeMappingSelectorAppliesIf, rs, ATTRIBUTE_MAPPING_SELECTOR);
					}

					if(attributeMappingSelectorAppliesIfValue) {
						//String alias = attributeMapping.getId() + attributeMappingSelector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
						String alias = attributeMappingSelector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
						String propValString = rs.getString(alias);

						if(propValString!= null && propValString != "" && !propValString.equals("")) {
							this.addDataTypeProperty(propName, propValString, model, subject);
						}
					}
				}

			}
		}

	}



	private boolean processConceptMappingAppliesIfElement(R2OConceptMapping r2oConceptMapping, ResultSet rs) throws Exception {
		boolean result = false;
		R2OConditionalExpression appliesIf = r2oConceptMapping.getAppliesIf();
		R2OConditionalExpression appliesIfTop = r2oConceptMapping.getAppliesIfTop();

		if(appliesIf == null && appliesIfTop == null) {
			result = true;
		} else {
			if(appliesIf != null) {
				return this.processConditionalExpression(appliesIf, rs, CONCEPT_MAPPING_APPLIES_IF);
			} else {
				if(appliesIfTop != null) {
					return this.processConditionalExpression(appliesIfTop, rs, CONCEPT_MAPPING_APPLIES_IF);
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

	private boolean processConditionalExpression(R2OConditionalExpression conditionalExpression, ResultSet rs, String source) throws Exception {
		boolean result = false;
		String conditionalExpressionOperator = conditionalExpression.getOperator();

		if(source.equalsIgnoreCase(CONCEPT_MAPPING_APPLIES_IF)) {
			return true;
		}
		
		if(conditionalExpressionOperator == null) {
			result = this.processCondition(conditionalExpression.getCondition(), rs);
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.AND_TAG)) {
			for(R2OConditionalExpression condExpr : conditionalExpression.getCondExprs()) {
				boolean condition = this.processConditionalExpression(condExpr, rs, source);
				if(condition == false) {
					return false;
				}
			}

			return true;
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.OR_TAG)) {
			for(R2OConditionalExpression condExpr : conditionalExpression.getCondExprs()) {
				boolean condition = this.processConditionalExpression(condExpr, rs, source);
				if(condition == true) {
					return true;
				}
			}

			return false;
		}

		return result;
	}

	private boolean processCondition(R2OCondition condition, ResultSet rs) throws Exception {
		boolean result = false;

		String operationId = null;
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(condition.getPrimitiveCondition())) {
			operationId = condition.getOperId();
		} else {
			operationId = condition.getPrimitiveCondition();
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_EQUALS_NAME.equalsIgnoreCase(operationId)) {
			result = this.processEqualsConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_LO_THAN_NAME.equalsIgnoreCase(operationId)) {
			result = this.processLoThanConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_LOEQ_THAN_NAME.equalsIgnoreCase(operationId)) {
			result = this.processLoEqThanConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_HI_THAN_NAME.equalsIgnoreCase(operationId)) {
			result = this.processHiThanConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_HIEQ_THAN_NAME.equalsIgnoreCase(operationId)) {
			result = this.processHiEqThanConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_NOT_EQUALS_NAME.equalsIgnoreCase(operationId)) {
			boolean conditionEquals = this.processEqualsConditional(condition, rs);
			result = !conditionEquals;
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_MATCH_REGEXP_NAME.equalsIgnoreCase(operationId)) {
			result = this.processMatchRegExpConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_IN_KEYWORD_NAME.equalsIgnoreCase(operationId)) {
			result = this.processInKeywordConditional(condition, rs);
		}

		if(R2OConstants.CONDITIONAL_OPERATOR_BETWEEN_NAME.equalsIgnoreCase(operationId)) {
			result = this.processBetweenConditional(condition, rs);
		}

		return result;
	}


	private boolean processMatchRegExpConditional(R2OCondition condition, ResultSet rs) throws Exception {
		R2ORestriction inputStringRestriction = condition.getArgRestricts(R2OConstants.ONPARAM_STRING);
		String inputString = (String) this.processRestriction(inputStringRestriction, rs);
		R2ORestriction regexRestriction = condition.getArgRestricts(R2OConstants.ONPARAM_REGEXP);
		String regex = (String) this.processRestriction(regexRestriction, rs);

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

	private boolean processEqualsConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		String operand0 = (String) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		String operand1 = (String) this.processRestriction(restriction1, rs);

		if(operand0 != null && operand1 != null) {
			return operand0.equals(operand1);
		} else {
			return false;
		}
	}

	private boolean processBetweenConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		Double operand0 = (Double) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		Double operand1 = Double.parseDouble(this.processRestriction(restriction1, rs).toString());
		R2ORestriction restriction2 = argumentRestrictions.get(2).getRestriction();
		Double operand2 = Double.parseDouble(this.processRestriction(restriction2, rs).toString());

		if(operand0 != null && operand1 != null && operand2 != null) {
			return operand0.doubleValue() >= operand1.doubleValue() && operand0.doubleValue() <= operand2.doubleValue();
		} else {
			return false;
		}
	}
	
	private boolean processInKeywordConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		String operand0 = (String) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		String operand1 = (String) this.processRestriction(restriction1, rs);

		if(operand0 != null && operand1 != null) {
			return operand0.indexOf(operand1) != -1;
		} else {
			return false;
		}
	}
	
	private boolean processLoThanConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		Double operand0 = (Double) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		Double operand1 = Double.parseDouble(this.processRestriction(restriction1, rs).toString());

		if(operand0 != null && operand1 != null) {
			return(operand0.doubleValue() < operand1.doubleValue());
		} else {
			return false;
		}
	}

	private boolean processLoEqThanConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		Double operand0 = (Double) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		Double operand1 = Double.parseDouble(this.processRestriction(restriction1, rs).toString());

		if(operand0 != null && operand1 != null) {
			return(operand0.doubleValue() <= operand1.doubleValue());
		} else {
			return false;
		}
	}

	private boolean processHiThanConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		Double operand0 = (Double) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		Double operand1 = Double.parseDouble(this.processRestriction(restriction1, rs).toString());

		if(operand0 != null && operand1 != null) {
			return(operand0.doubleValue() > operand1.doubleValue());
		} else {
			return false;
		}
	}

	private boolean processHiEqThanConditional(R2OCondition condition, ResultSet rs) throws Exception {
		List<R2OArgumentRestriction> argumentRestrictions = condition.getArgRestricts();

		R2ORestriction restriction0 = argumentRestrictions.get(0).getRestriction();
		Double operand0 = (Double) this.processRestriction(restriction0, rs);
		R2ORestriction restriction1 = argumentRestrictions.get(1).getRestriction();
		Double operand1 = Double.parseDouble(this.processRestriction(restriction1, rs).toString());

		if(operand0 != null && operand1 != null) {
			return(operand0.doubleValue() >= operand1.doubleValue());
		} else {
			return false;
		}
	}
	private Object processDBColumn(String columnName, String dataType, ResultSet rs) throws Exception {
		try {
			Object result = null;
			if(dataType == null) {
				dataType = R2OConstants.DATATYPE_STRING;
				
			} 
			
			String alias = columnName.replaceAll("\\.", "_");
			if(dataType.equalsIgnoreCase(R2OConstants.DATATYPE_STRING)) {
				result = rs.getString(alias);
			} else if (dataType.equalsIgnoreCase(R2OConstants.DATATYPE_DOUBLE)) {
				result = rs.getDouble(alias);
			} else if (dataType.equalsIgnoreCase(R2OConstants.DATATYPE_DATE)) {
				result = rs.getDate(alias);
			} else {
				result = rs.getString(alias);
			}
			
			//rs.getDouble(columnLabel)
			return result;			
		} catch(SQLException e) {
			throw e;
		}

	}

	private Object processRestriction(R2ORestriction restriction, ResultSet rs) throws Exception {
		try {
			Object result = null;
			if(restriction.getRestrictionType() == RestrictionType.HAS_COLUMN) {
				//result = rs.getString(restriction.getHasColumn().replaceAll("\\.", "_"));
				result = this.processDBColumn(restriction.getHasColumn(), restriction.getRestrictionDataType(), rs);
			} else if(restriction.getRestrictionType() == RestrictionType.HAS_VALUE) {
				result = restriction.getHasValue();
			} else if(restriction.getRestrictionType() == RestrictionType.HAS_TRANSFORMATION) {

			}

			return result;			
		} catch(SQLException e) {
			//e.printStackTrace();
			throw e;
		}

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
		String tdbFileBase = tdbDatabaseFolder + "/" + databaseName;
		logger.info("TDB filebase = " + tdbFileBase);
		return TDBFactory.createModel(tdbFileBase) ;

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
				model = this.createTDBModel(jenaDatabaseName);
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
