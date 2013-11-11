# Morph

Morph (formerly called ODEMapster) is an RDB2RDF engine developed by the Ontology Engineering Group, that follows R2RML specifications (http://www.w3.org/TR/r2rml/). Morph supports data upgrade (RDB to RDF instances) and query translation (SPARQL to SQL). Morph employs various techniques in order to generate efficient SQL queries, such as self-join elimination, subquery elimination, and phantom patterns introduction (work in progress). Morph has been tested with real queries from various Spanish/EU projects and has proven work faster than the state-of-the-art tool available.

At the moment, Morph works with MySQL, PostgreSQL, and MonetDB. Morph project page can be found at https://github.com/fpriyatna/odemapster and the instructions of how to use it can be found at https://github.com/fpriyatna/odemapster/wiki.
