package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class R2RMLConstants {
	public static String R2RML_NS = "http://www.w3.org/ns/r2rml#";

	//MappingDocument
	private static String R2RML_TRIPLESMAP_URI = R2RML_NS + "TriplesMap";
	public static Resource R2RML_TRIPLESMAP_CLASS = ResourceFactory.createResource(R2RML_TRIPLESMAP_URI);
	private static String R2RML_OBJECTMAPCLASS_URI = R2RML_NS + "ObjectMap";
	public static Resource R2RML_OBJECTSMAP_CLASS = ResourceFactory.createResource(R2RML_OBJECTMAPCLASS_URI);

	


	//logical table
	private static String R2RML_TABLENAME_URI = R2RML_NS + "tableName";
	public static Property R2RML_TABLENAME_PROPERTY = ResourceFactory.createProperty(R2RML_TABLENAME_URI);
	private static String R2RML_SQLQUERY_URI = R2RML_NS + "sqlQuery";
	public static Property R2RML_SQLQUERY_PROPERTY = ResourceFactory.createProperty(R2RML_SQLQUERY_URI);
	
	//TriplesMap
	private static String R2RML_LOGICALTABLE_URI = R2RML_NS + "logicalTable";
	public static Property R2RML_LOGICALTABLE_PROPERTY = ResourceFactory.createProperty(R2RML_LOGICALTABLE_URI);
	private static String R2RML_SUBJECTMAP_URI = R2RML_NS + "subjectMap";
	public static Property R2RML_SUBJECTMAP_PROPERTY = ResourceFactory.createProperty(R2RML_SUBJECTMAP_URI);
	private static String R2RML_PREDICATEOBJECTMAP_URI = R2RML_NS + "predicateObjectMap";
	public static Property R2RML_PREDICATEOBJECTMAP_PROPERTY = ResourceFactory.createProperty(R2RML_PREDICATEOBJECTMAP_URI);
	private static String R2RML_SUBJECT_URI = R2RML_NS + "subject";
	public static Property R2RML_SUBJECT_PROPERTY = ResourceFactory.createProperty(R2RML_SUBJECT_URI);
	private static String R2RML_PREDICATE_URI = R2RML_NS + "predicate";
	public static Property R2RML_PREDICATE_PROPERTY = ResourceFactory.createProperty(R2RML_PREDICATE_URI);
	private static String R2RML_OBJECT_URI = R2RML_NS + "object";
	public static Property R2RML_OBJECT_PROPERTY = ResourceFactory.createProperty(R2RML_OBJECT_URI);
	private static String R2RML_GRAPH_URI = R2RML_NS + "graph";
	public static Property R2RML_GRAPH_PROPERTY = ResourceFactory.createProperty(R2RML_GRAPH_URI);
		
	//PredicateObjectMap
	private static String R2RML_PREDICATEMAP_URI = R2RML_NS + "predicateMap";
	public static Property R2RML_PREDICATEMAP_PROPERTY = ResourceFactory.createProperty(R2RML_PREDICATEMAP_URI);
	private static String R2RML_OBJECTMAP_URI = R2RML_NS + "objectMap";
	public static Property R2RML_OBJECTMAP_PROPERTY = ResourceFactory.createProperty(R2RML_OBJECTMAP_URI);
	private static String R2RML_REFOBJECTMAP_URI = R2RML_NS + "refObjectMap";
	public static Property R2RML_REFOBJECTMAP_PROPERTY = ResourceFactory.createProperty(R2RML_REFOBJECTMAP_URI);
	private static String R2RML_JOINCONDITION_URI = R2RML_NS + "joinCondition";
	public static Property R2RML_JOINCONDITION_PROPERTY = ResourceFactory.createProperty(R2RML_JOINCONDITION_URI);
	private static String R2RML_CHILD_URI = R2RML_NS + "child";
	public static Property R2RML_CHILD_PROPERTY = ResourceFactory.createProperty(R2RML_CHILD_URI);
	private static String R2RML_PARENT_URI = R2RML_NS + "parent";
	public static Property R2RML_PARENT_PROPERTY = ResourceFactory.createProperty(R2RML_PARENT_URI);
	
	//TermMap
	private static String R2RML_TERMTYPE_URI = R2RML_NS + "termType";
	public static Property R2RML_TERMTYPE_PROPERTY = ResourceFactory.createProperty(R2RML_TERMTYPE_URI);
	private static String R2RML_DATATYPE_URI = R2RML_NS + "datatype";
	public static Property R2RML_DATATYPE_PROPERTY = ResourceFactory.createProperty(R2RML_DATATYPE_URI);
	private static String R2RML_GRAPHMAP_URI = R2RML_NS + "graphMap";
	public static Property R2RML_GRAPHMAP_PROPERTY = ResourceFactory.createProperty(R2RML_GRAPHMAP_URI);
	private static String R2RML_TEMPLATE_URI = R2RML_NS + "template";
	public static Property R2RML_TEMPLATE_PROPERTY = ResourceFactory.createProperty(R2RML_TEMPLATE_URI);
	private static String R2RML_CONSTANT_URI = R2RML_NS + "constant";
	public static Property R2RML_CONSTANT_PROPERTY = ResourceFactory.createProperty(R2RML_CONSTANT_URI);
	private static String R2RML_COLUMN_URI = R2RML_NS + "column";
	public static Property R2RML_COLUMN_PROPERTY = ResourceFactory.createProperty(R2RML_COLUMN_URI);
	private static String R2RML_LANGUAGE_URI = R2RML_NS + "language";
	public static Property R2RML_LANGUAGE_PROPERTY = ResourceFactory.createProperty(R2RML_LANGUAGE_URI);
	
	//SubjectMap
	private static String R2RML_CLASS_URI = R2RML_NS + "class";
	public static Property R2RML_CLASS_PROPERTY = ResourceFactory.createProperty(R2RML_CLASS_URI);
	
	//ObjectMap
	private static String R2RML_PARENTTRIPLESMAP_URI = R2RML_NS + "parentTriplesMap";
	public static Property R2RML_PARENTTRIPLESMAP_PROPERTY = ResourceFactory.createProperty(R2RML_PARENTTRIPLESMAP_URI);

	
	//datatype
	public static String R2RML_LITERAL_URI = R2RML_NS + "Literal";
	public static Resource R2RML_LITERAL_CLASS = ResourceFactory.createResource(R2RML_LITERAL_URI);
	public static String R2RML_IRI_URI = R2RML_NS + "IRI";
	public static Resource R2RML_IRI_CLASS = ResourceFactory.createResource(R2RML_IRI_URI);
	public static String R2RML_BLANKNODE_URI = R2RML_NS + "BlankNode";
	public static Resource R2RML_BLANKNODE_CLASS = ResourceFactory.createResource(R2RML_BLANKNODE_URI);
	
	
}
