package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
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
import com.hp.hpl.jena.rdf.model.RDFNode;
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
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2ORestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSelector;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OConstantRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OSQLRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.R2OTransformationRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.element.RestrictionValue;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OAttributeMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2OPropertyMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.mapping.R2ORelationMapping;

public abstract class R2OPostProcessor {
	private static Logger logger = Logger.getLogger(R2OPostProcessor.class);


	public void processConceptMapping(ResultSet rs, Model model, AbstractConceptMapping conceptMapping
			, Writer outputFileWriter) throws Exception {
		String conceptName = conceptMapping.getConceptName();
		logger.info("Post processing for " + conceptName);
		long startGeneratingModel = System.currentTimeMillis();
		Resource object = model.getResource(conceptName);

		R2OConceptMapping r2oConceptMapping = (R2OConceptMapping) conceptMapping;
		R2OConditionalExpression appliesIf = r2oConceptMapping.getAppliesIf();


		boolean processRecord = false;
		boolean appliesIfIsDelegableConditionalExpr;
		boolean appliesIfIsConjuctiveConditionalExpr;
		if(appliesIf == null) { //always process if applies-if is null
			processRecord = true;
			appliesIfIsConjuctiveConditionalExpr = false;			
		} else { 
			appliesIfIsDelegableConditionalExpr = appliesIf.isDelegableConditionalExpression();
			appliesIfIsConjuctiveConditionalExpr = appliesIf.isConjuctiveConditionalExpression();

			if(appliesIfIsDelegableConditionalExpr) { //always process if applies-if is delegable conditional expr
				processRecord = true;
			}
		}

		R2OTransformationExpression conceptMappingURIAsTransformationExpression = 
			r2oConceptMapping.getURIAs();

		boolean uriAsIsDelegableTransformationExpr = 
			isDelegableTransformationExpression(conceptMappingURIAsTransformationExpression);

		boolean isEncodeURI = true;
		String encodeURI = r2oConceptMapping.getEncodeURI();

		if(encodeURI == null) {
			encodeURI = R2OConstants.STRING_TRUE;
		}

		if(encodeURI.equalsIgnoreCase(R2OConstants.STRING_FALSE)) {
			isEncodeURI = false;
		}

		boolean useNTriple = false;
		String rdfLanguage = R2ORunner.configurationProperties.getRdfLanguage();
		if(rdfLanguage == null) {
			rdfLanguage = "RDF/XML";
		}
		if(rdfLanguage.equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_NTRIPLE)) {
			useNTriple = true;
		}

		long counter = 0;
		String previousSubjectURI = null;
		while(rs.next()) {
			try {

				if(!processRecord) {
					if(appliesIfIsConjuctiveConditionalExpr) { //only process the non-delegable ones
						boolean allConditionTrue = true;
						for(R2OCondition condition : appliesIf.flatConjuctiveConditionalExpression()) {
							if(processRecord && !condition.isDelegableCondition()) {
								if(!this.processCondition(condition, rs)) {
									allConditionTrue = false;
								}
							}
						}
						processRecord = allConditionTrue;
					} else {
						//processRecord = this.processConditionalExpression(appliesIf, rs, CONCEPT_MAPPING_APPLIES_IF);
						processRecord = this.processConditionalExpression(appliesIf, rs);							
					}					
				}



				if(processRecord) {
					String subjectURI = null;
					if(uriAsIsDelegableTransformationExpr) {
						String alias = R2OConstants.URI_AS_ALIAS + r2oConceptMapping.getId();
						subjectURI = rs.getString(alias);
						//uri = this.processDelegableTransformationExpression(rs, alias).toString();
					} else {
						subjectURI = (String) this.processNonDelegableTransformationExpression(
								rs, conceptMappingURIAsTransformationExpression);
					}
					if(subjectURI == null) {
						throw new Exception("null uri is not allowed!");
					}



					if(isEncodeURI) {
						subjectURI = Utility.encodeURI(subjectURI);
					}

					if(counter % 50000 == 0) {
						logger.info("Processing record no " + counter + " : " + subjectURI);
					}
					counter++;

					Resource subject = model.createResource(subjectURI);

					//create rdf type triple if the subject is different from the previous one
					if(!subjectURI.equals(previousSubjectURI)) {
						this.createRDFTypeTriple(useNTriple, subjectURI, conceptName
								, outputFileWriter, model, subject, object);

					}

					//logger.info("Processing property mappings.");
					this.processPropertyMappings(r2oConceptMapping, rs, model, subject, outputFileWriter);
					previousSubjectURI = subjectURI;
				}

			} catch(Exception e) {
//				e.printStackTrace();
				logger.error("Error processing record no " + counter + " because " + e.getMessage());

				throw e;
			}




		}
		logger.info(counter + " instances retrieved.");



		long endGeneratingModel = System.currentTimeMillis();
		long durationGeneratingModel = (endGeneratingModel-startGeneratingModel) / 1000;
		logger.info("Post Processing time was "+(durationGeneratingModel)+" s.");


	}

	private void createRDFTypeTriple(boolean useNTriple, String subjectURI, String conceptName
			, Writer outputFileWriter, Model model, Resource subject, RDFNode object) 
	throws IOException {
		if(useNTriple) {
			String triple = 
				this.createTriple(
						this.createURIref(subjectURI)
						, this.createURIref(RDF.type.toString())
						, this.createURIref(conceptName)); 
			outputFileWriter.append(triple);							

		} else {
			subject = model.createResource(subjectURI);
			Statement statement = model.createStatement(subject, RDF.type, object);
			model.add(statement);						
		}
	}

	/*
	private Object processDelegableTransformationExpression(ResultSet rs, String alias) throws SQLException {
		Object result = rs.getObject(alias);;
		return result;
	}
	 */

	private Object processNonDelegableTransformationExpression(
			ResultSet rs, R2OTransformationExpression transformationExpression) throws PostProcessorException, InvalidRestrictionType, SQLException {
		if(transformationExpression.getOperId().equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_SUBSTRING)) {
			return this.processSubstringTransformationExpression(rs, transformationExpression);
		} else if(transformationExpression.getOperId().equalsIgnoreCase(R2OConstants.TRANSFORMATION_OPERATOR_CUSTOM_TRANSFORMATION)) {
			List<Object> arguments = new ArrayList<Object>();
			List<R2OArgumentRestriction> argumentRestrictions = transformationExpression.getArgRestrictions();
			for(R2OArgumentRestriction argument : argumentRestrictions) {
				Object restrictionValue = this.processRestriction(argument.getRestriction(), rs);
				arguments.add(restrictionValue);
			}
			return this.processCustomFunctionTransformationExpression(arguments);
		} else {
			String errorMessage = "Not supported transformation operation : " + transformationExpression.getOperId();
			logger.error(errorMessage);
			throw new PostProcessorException(errorMessage);
		}
	}

	protected abstract Object processCustomFunctionTransformationExpression(List<Object> arguments) 
	throws PostProcessorException;

	private String processSubstringTransformationExpression(
			ResultSet rs, R2OTransformationExpression transformationExpression) throws PostProcessorException, InvalidRestrictionType, SQLException {
		List<R2OArgumentRestriction> argumentRestrictions = transformationExpression.getArgRestrictions();
		String argument0 = (String) this.processRestriction(argumentRestrictions.get(0).getRestriction(), rs); 
		Integer beginIndex = Integer.parseInt(this.processRestriction(argumentRestrictions.get(1).getRestriction(), rs).toString());
		Integer endIndex = Integer.parseInt(this.processRestriction(argumentRestrictions.get(2).getRestriction(), rs).toString());

		return argument0.substring(beginIndex, endIndex);
	}

	private void processPropertyMappings(R2OConceptMapping r2oConceptMapping, ResultSet rs, Model model, Resource subject
			, Writer outputFileWriter) throws Exception {
		List<R2OPropertyMapping> propertyMappings = r2oConceptMapping.getPropertyMappings();
		if(propertyMappings != null) {
			for(R2OPropertyMapping propertyMapping : propertyMappings) {
				if(propertyMapping instanceof R2OAttributeMapping) {
					try {
						this.processAttributeMapping(
								(R2OAttributeMapping) propertyMapping, rs, model, subject, outputFileWriter);
					} catch(Exception e) {
						String newErrorMessage = e.getMessage() + " while processing attribute mapping " + propertyMapping.getName();
						logger.error(newErrorMessage);
						throw e;
					}

				} else if(propertyMapping instanceof R2ORelationMapping) {
					try {
						this.processRelationMapping(
								(R2ORelationMapping) propertyMapping, rs, model, subject, outputFileWriter);
					} catch(Exception e) {
						String newErrorMessage = e.getMessage() + " while processing relation mapping " + propertyMapping.getName();
						logger.error(newErrorMessage);
						throw e;
					}

				}
			}			
		}





	}

	/*
	private void addDataTypeProperty(String propName, Object propVal, String datatype, Model model, Resource subject) {
		if(propName.equalsIgnoreCase(RDFS.label.toString())) { //special case of rdfs:label
			String propValString = propVal.toString();
			int lastAtIndex = propValString.lastIndexOf("@");
			Literal literal = null;
			if(lastAtIndex > 0) {
				String propertyValue = propValString.substring(0, lastAtIndex);
				String language = propValString.substring(lastAtIndex+1, propValString.length());
				literal = model.createLiteral(propertyValue, language);
			} else {
				literal = model.createTypedLiteral(propValString);
			}
			subject.addProperty(RDFS.label, literal);
		} else {
			Literal literal;
			if(datatype == null) {
				literal =  model.createTypedLiteral(propVal);
			} else {
				literal =  model.createTypedLiteral(propVal, datatype);
			}

			subject.addProperty(model.createProperty(propName), literal);
		}
	}
	 */


	//XML Schema states that xml:Lang is not meaningful on xsd datatypes.
	//Thus for almost all typed literals there is no xml:Lang tag.
	private void addDataTypeProperty(
			String propName, Object propVal, String datatype, String lang
			, Model model, Resource subject, Writer outputFileWriter) 
	throws Exception {
		Literal literal;

		if(propName.equalsIgnoreCase(RDFS.label.toString())) { //special case of rdfs:label
			if(lang == null) {
				datatype = R2OConstants.XSD_STRING;
			}
		}

		String literalString = null;
		if(datatype == null) {
			if(lang == null) {
				literal =  model.createTypedLiteral(propVal);
				literalString = this.createLiteral(propVal.toString());
			} else {
				literal =  model.createLiteral(propVal.toString(), lang);
				literalString = this.createLanguageLiteral(propVal.toString(), lang);
			}
		} else {
			literal =  model.createTypedLiteral(propVal, datatype);
			literalString = this.createDataTypeLiteral(propVal.toString(), datatype);
		}

		String rdfLanguage = R2ORunner.configurationProperties.getRdfLanguage();
		if(rdfLanguage == null) {
			rdfLanguage = R2OConstants.OUTPUT_FORMAT_NTRIPLE;
		}

		if(rdfLanguage.equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_NTRIPLE)) {
			String tripleSubject = this.createURIref(subject.getURI());
			String triplePredicate = this.createURIref(propName);
			//String tripleObject = this.createURIref(literalString);
			String tripleObject = literalString;
			String tripleString = this.createTriple(tripleSubject, triplePredicate, tripleObject); 
			outputFileWriter.append(tripleString);			
		} else {
			subject.addProperty(model.createProperty(propName), literal);
		}





	}

	//Creates a triple
	private String createTriple(String subject, String predicate, String object)
	{
		StringBuffer result = new StringBuffer();
		result.append(subject);
		result.append(" ");
		result.append(predicate);
		result.append(" ");
		result.append(object);
		result.append(" .\n");


		return result.toString();
	}

	//Create Literal
	private String createLiteral(String value)
	{
		value = Utility.encodeLiteral(value);
		StringBuffer result = new StringBuffer();
		result.append("\"");
		result.append(value);
		result.append("\"");
		return result.toString();
	}

	//Create typed literal
	private String createDataTypeLiteral(String value, String datatypeURI)
	{
		value = Utility.encodeLiteral(value);
		StringBuffer result = new StringBuffer();
		result.append("\"");
		result.append(value);
		result.append("\"^^");
		result.append(datatypeURI);
		return result.toString();
	}

	//Create language tagged literal
	private String createLanguageLiteral(String text, String languageCode)
	{
		text = Utility.encodeLiteral(text);
		StringBuffer result = new StringBuffer();
		result.append("\"");
		result.append(text);
		result.append("\"@");
		result.append(languageCode);
		return result.toString();
	}

	//Create URIREF from namespace and element
	private String createURIref(String namespace, String element)
	{
		StringBuffer result = new StringBuffer();
		result.append("<");
		result.append(namespace);
		result.append(element);
		result.append(">");
		return result.toString();
	}

	//Create URIREF from URI
	private String createURIref(String uri)
	{
		StringBuffer result = new StringBuffer();
		result.append("<");
		result.append(uri);
		result.append(">");
		return result.toString();
	}

	private void processRelationMapping(R2ORelationMapping r2oRelationMapping, ResultSet rs, Model model, Resource subject
			, Writer outputFileWriter) throws Exception {
		String relationName = r2oRelationMapping.getRelationName();
		Property property = model.createProperty(relationName);

		if(r2oRelationMapping.getToConcept() != null) {
			String rangeURI = rs.getString(R2OConstants.RELATIONMAPPING_ALIAS + r2oRelationMapping.hashCode());
			if(rangeURI != null) {
				rangeURI = Utility.encodeURI(rangeURI);

				String rdfLanguage = R2ORunner.configurationProperties.getRdfLanguage();
				if(rdfLanguage.equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_NTRIPLE)) {
					String triple = 
						this.createTriple(
								this.createURIref(subject.getURI())
								, this.createURIref(relationName)
								, this.createURIref(rangeURI)); 
					outputFileWriter.append(triple);

				} else {
					Resource object = model.createResource(rangeURI);
					Statement rdfstmtInstance = model.createStatement(subject,property, object);
					model.add(rdfstmtInstance);

				}


			}			
		} 

		if(r2oRelationMapping.getRmSelectors() != null) {
			Collection<R2OSelector> selectors = r2oRelationMapping.getRmSelectors();
			if(selectors != null) {
				for(R2OSelector selector : selectors) {
					R2OConditionalExpression selectorAppliesIf = selector.getAppliesIf();
					boolean selectorAppliesIfValue = false;
					if(selectorAppliesIf == null) {
						//if there is no applies-if specified in the selector, then the condition is true
						selectorAppliesIfValue = true;
					} else {
						//else, evaluate the applies-if condition

						if(this.isDelegableConditionalExpression(selectorAppliesIf)) {
							String selectorAppliesIfAlias = selector.generateAppliesIfAlias(); 
							int appliesIfValue = rs.getInt(selectorAppliesIfAlias);
							if(appliesIfValue == 0) {
								selectorAppliesIfValue = false;
							} else if(appliesIfValue == 1) {
								selectorAppliesIfValue = true;
							}
						} else {
							selectorAppliesIfValue = this.processConditionalExpression(selectorAppliesIf, rs); 
							//this.processConditionalExpression(attributeMappingSelectorAppliesIf, rs, ATTRIBUTE_MAPPING_SELECTOR);							

						}

					}

					if(selectorAppliesIfValue) {
						//String alias = attributeMapping.getId() + attributeMappingSelector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
						R2OTransformationExpression selectorAT = selector.getAfterTransform();


						Object propVal = null;
						if(isDelegableTransformationExpression(selectorAT)) {
							String returnType = selectorAT.getDatatype();

							String alias = R2OConstants.AFTERTRANSFORM_ALIAS + selector.hashCode();

							propVal = this.processSQLExpression(alias, alias, returnType, rs);
							//propVal = this.processDelegableTransformationExpression(rs, alias);							
						} else {
							propVal = this.processNonDelegableTransformationExpression(rs, selectorAT);
						}


						if(propVal != null) {
							String propValString = propVal.toString();

							if(R2ORunner.configurationProperties.getRdfLanguage().equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_NTRIPLE)) {
								String triple = 
									this.createTriple(
											this.createURIref(subject.getURI())
											, this.createURIref(relationName)
											, this.createURIref(propValString)); 
								outputFileWriter.append(triple);								
							} else {

								propValString = Utility.encodeURI(propValString);
								Resource object = model.createResource(propValString);
								Statement rdfstmtInstance = model.createStatement(subject,property, object);
								model.add(rdfstmtInstance);								
							}

						}		
					}
				}

			}			
		}

	}



	private void processAttributeMapping(
			R2OAttributeMapping attributeMapping, ResultSet rs, Model model, Resource subject, Writer outputFileWriter) throws Exception {
		String propName = attributeMapping.getName();
		String amDataType = attributeMapping.getDatatype();
		String lang = null;
		if(attributeMapping.getLangHasValue() != null) {
			lang = attributeMapping.getLangHasValue();
		} else if(attributeMapping.getLangDBCol() != null) {
			String columnName = attributeMapping.getLangDBCol();
			String alias = columnName.replaceAll("\\.", "_");
			lang = (String) this.processSQLExpression(columnName, alias, attributeMapping.getLangDBColDataType(), rs);			
		}

		String dbColUsed = attributeMapping.getUseDBCol();
		if(dbColUsed != null) {
			String columnName = attributeMapping.getUseDBCol();
			String alias = columnName.replaceAll("\\.", "_");
			Object dbColValue = this.processSQLExpression(columnName, alias, attributeMapping.getUseDBColDatatype(), rs);
			this.addDataTypeProperty(propName, dbColValue, amDataType, lang, model, subject, outputFileWriter);
		} else if(attributeMapping.getUseSQL() != null) {
			String expression = attributeMapping.getUseSQL();
			String alias = attributeMapping.getUseSQLAlias();
			Object dbColValue = this.processSQLExpression(expression, alias, attributeMapping.getUseSQLDataType(), rs);
			this.addDataTypeProperty(propName, dbColValue, amDataType, lang, model, subject, outputFileWriter);			
		} else if(attributeMapping.getSelectors() != null){
			Collection<R2OSelector> attributeMappingSelectors = attributeMapping.getSelectors();

			for(R2OSelector attributeMappingSelector : attributeMappingSelectors) {
				R2OConditionalExpression attributeMappingSelectorAppliesIf = attributeMappingSelector.getAppliesIf();
				boolean attributeMappingSelectorAppliesIfValue = false;
				if(attributeMappingSelectorAppliesIf == null) {
					//if there is no applies-if specified in the selector, then the condition is true
					attributeMappingSelectorAppliesIfValue = true;
				} else {
					//else, evaluate the applies-if condition

					if(this.isDelegableConditionalExpression(attributeMappingSelectorAppliesIf)) {
						String selectorAppliesIfAlias = attributeMappingSelector.generateAppliesIfAlias();
						int appliesIfValue = rs.getInt(selectorAppliesIfAlias);
						if(appliesIfValue == 0) {
							attributeMappingSelectorAppliesIfValue = false;
						} else if(appliesIfValue == 1) {
							attributeMappingSelectorAppliesIfValue = true;
						}
					} else {
						attributeMappingSelectorAppliesIfValue = this.processConditionalExpression(attributeMappingSelectorAppliesIf, rs); 
						//this.processConditionalExpression(attributeMappingSelectorAppliesIf, rs, ATTRIBUTE_MAPPING_SELECTOR);							

					}

				}

				if(attributeMappingSelectorAppliesIfValue) {
					//String alias = attributeMapping.getId() + attributeMappingSelector.hashCode() + R2OConstants.AFTERTRANSFORM_TAG;
					R2OTransformationExpression attMapSelAT = attributeMappingSelector.getAfterTransform();
					String selectorDataType = attMapSelAT.getDatatype();


					Object propVal = null;
					if(isDelegableTransformationExpression(attMapSelAT)) {
						String alias = R2OConstants.AFTERTRANSFORM_ALIAS + attributeMappingSelector.hashCode();

						propVal = this.processSQLExpression(alias, alias, selectorDataType, rs);
						//propVal = this.processDelegableTransformationExpression(rs, alias);							
					} else {
						propVal = this.processNonDelegableTransformationExpression(rs, attMapSelAT);
					}


					String isCollection = attMapSelAT.getIsCollection();
					if(isCollection != null && isCollection != "") {
						if(isCollection.equalsIgnoreCase(R2OConstants.DATATYPE_COLLECTION)) {
							Collection<Object> propCol = (Collection<Object>) propVal;
							for(Object propColItem : propCol) {
								this.addDataTypeProperty(
										propName, propColItem, amDataType, lang, model, subject, outputFileWriter);
							}
						} else {
							throw new Exception("Unsupported return type : " + selectorDataType);
						}
					} else {
						if(propVal!= null && propVal != "" && !propVal.equals("")) {
							this.addDataTypeProperty(
									propName, propVal, amDataType, lang, model, subject, outputFileWriter);							
						}
					}
				}
			}

		} else {
			throw new Exception("Unsupported attribue mapping.");
		}

	}


	private boolean isDelegableCondition(R2OCondition condition) {
		String operationId = null;
		if(R2OConstants.CONDITION_TAG.equalsIgnoreCase(condition.getPrimitiveCondition())) {
			operationId = condition.getOperId();
		} else {
			operationId = condition.getPrimitiveCondition();
		}

		if(Utility.inArray(R2ORunner.primitiveOperationsProperties.getDelegableConditionalOperations(), operationId)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isDelegableConditionalExpression(R2OConditionalExpression conditionalExpression) {
		if(conditionalExpression.getOperator() == null) {
			return this.isDelegableCondition(conditionalExpression.getCondition());
		} else {
			for(R2OConditionalExpression condExpr : conditionalExpression.getCondExprs()) {
				if(this.isDelegableConditionalExpression(condExpr) == false) {
					return false;
				}
			}
			return true;

		}


	}


	//	private boolean processConceptMappingAppliesIfElement(R2OConceptMapping r2oConceptMapping, ResultSet rs) throws Exception {
	//		boolean result = false;
	//		R2OConditionalExpression appliesIf = r2oConceptMapping.getAppliesIf();
	//
	//		if(appliesIf == null) {
	//			result = true;
	//		} else {
	//			return this.processConditionalExpression(appliesIf, rs, CONCEPT_MAPPING_APPLIES_IF);
	//		}
	//
	//		return result;
	//	}



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

	private boolean processConditionalExpression(R2OConditionalExpression conditionalExpression, ResultSet rs) 
	throws Exception {
		boolean result = false;
		String conditionalExpressionOperator = conditionalExpression.getOperator();


		if(conditionalExpressionOperator == null) {
			result = this.processCondition(conditionalExpression.getCondition(), rs);
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.AND_TAG)) {
			for(R2OConditionalExpression condExpr : conditionalExpression.getCondExprs()) {
				boolean condition = this.processConditionalExpression(condExpr, rs);
				if(condition == false) {
					return false;
				}
			}

			return true;
		} else if(conditionalExpressionOperator.equalsIgnoreCase(R2OConstants.OR_TAG)) {
			for(R2OConditionalExpression condExpr : conditionalExpression.getCondExprs()) {
				boolean condition = this.processConditionalExpression(condExpr, rs);
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

		//		//only skip records while processing concept mapping
		//		if(source.equalsIgnoreCase(CONCEPT_MAPPING_APPLIES_IF) 
		//				&&  Utility.inArray(this.properties.getDelegableConditionalOperations(), operationId)) {
		//			return true;
		//		}

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

		if(R2OConstants.CONDITIONAL_OPERATOR_NOT_MATCH_REGEXP_NAME.equalsIgnoreCase(operationId)) {
			result = this.processMatchRegExpConditional(condition, rs);
			return !result;
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
		/*
		if(inputString.contains("Cruz de")) {
			boolean test = true;
		}
		 */

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

	private Object processSQLExpression(String expression, String alias, String dataType, ResultSet rs) throws SQLException {
		try {
			Object result = null;

			if(dataType == null || dataType == "") {
				result = rs.getObject(alias);
			} else {
				if(dataType.equalsIgnoreCase(R2OConstants.DATATYPE_STRING)) {
					result = rs.getString(alias);
				} else if (dataType.equalsIgnoreCase(R2OConstants.DATATYPE_DOUBLE)) {
					result = rs.getDouble(alias);
				} else if (dataType.equalsIgnoreCase(R2OConstants.DATATYPE_DATE)) {
					result = rs.getDate(alias);
				} else if (dataType.equalsIgnoreCase(R2OConstants.DATATYPE_INTEGER)) {
					result = rs.getInt(alias);
				} else if (dataType.equalsIgnoreCase(R2OConstants.DATATYPE_BLOB)) {
					result = rs.getBlob(alias);
				} else {
					result = rs.getObject(alias);
				}
			}

			return result;			
		} catch(SQLException e) {
			//e.printStackTrace();
			logger.error("Error while retrieving SQL records!");
			throw e;
		}

	}

	private Object processRestriction(R2ORestriction restriction, ResultSet rs) throws PostProcessorException, InvalidRestrictionType, SQLException {
		Object result = null;

		if(restriction instanceof R2OColumnRestriction) {
			R2OColumnRestriction restrictionColumn = (R2OColumnRestriction) restriction;
			R2ODatabaseColumn dbColumn = restrictionColumn.getDatabaseColumn();
			String fullColumnName = dbColumn.getFullColumnName();
			String columnNameOnly = dbColumn.getColumnNameOnly();
			String dataType = dbColumn.getDataType();
			
			String alias = columnNameOnly;
//			if(restrictionColumn.getDatabaseColumn().getAlias() != null) {
//				alias = restrictionColumn.getDatabaseColumn().getAlias();
//			}
			//alias can be used only if the container is delegable 
			//String alias = columnName.replaceAll("\\.", "_");
			
			result = this.processSQLExpression(fullColumnName, alias, dataType, rs);
		} else if(restriction instanceof R2OConstantRestriction) {
			R2OConstantRestriction restrictionConstant = (R2OConstantRestriction) restriction;
			result = restrictionConstant.getConstantValue();
		} else if(restriction instanceof R2OTransformationRestriction) {
			R2OTransformationRestriction restrictionTransformation = 
				(R2OTransformationRestriction) restriction;
			R2OTransformationExpression childTransformationExpression = 
				restrictionTransformation.getTransformationExpression();
			Object subresult = this.processNonDelegableTransformationExpression(rs, childTransformationExpression);
			return subresult;
		} else if(restriction instanceof R2OSQLRestriction) {
			R2OSQLRestriction restrictionSQL = (R2OSQLRestriction) restriction;
			String expression = restrictionSQL.getHasSQL();
			String alias = restrictionSQL.getAlias();
			if(alias == null || alias == "") {
				alias = R2OConstants.RESTRICTION_ALIAS + restriction.hashCode();
			}

			result = this.processSQLExpression(expression, alias, null, rs);
		} else {
			String errorMessage = "Unsupported restriction type!";
			logger.error(errorMessage);
			throw new InvalidRestrictionType(errorMessage);
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




	private boolean isDelegableTransformationExpression(R2OTransformationExpression transformationExpression) {
		String operator = transformationExpression.getOperId();

		//if the root operator is not delegable, then return false 
		if(!Utility.inArray(R2ORunner.primitiveOperationsProperties.getDelegableTransformationOperations(), operator)) {
			return false;
		}

		//if one of the arguments has non delegable transformation, then return false
		for(R2OArgumentRestriction argRestriction : transformationExpression.getArgRestrictions()) {
			R2ORestriction restriction = argRestriction.getRestriction();
			if(restriction instanceof R2OTransformationRestriction) {
				R2OTransformationRestriction restrictionTransformation = 
					(R2OTransformationRestriction) restriction;
				if(!isDelegableTransformationExpression(
						restrictionTransformation.getTransformationExpression())) {
					return false;
				}
			}
		}

		return true;
	}


}
