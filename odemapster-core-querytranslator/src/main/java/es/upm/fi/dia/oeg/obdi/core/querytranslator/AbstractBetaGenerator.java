package es.upm.fi.dia.oeg.obdi.core.querytranslator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import Zql.ZConstant;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator.POS;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;


public abstract class AbstractBetaGenerator {
	private static Logger logger = Logger.getLogger(AbstractBetaGenerator.class);
	protected Map<Node, Set<AbstractConceptMapping>> mapNodeConceptMapping;
	protected AbstractMappingDocument mappingDocument;
	protected AbstractQueryTranslator owner;

	public AbstractBetaGenerator(AbstractQueryTranslator owner,
			Map<Node, Set<AbstractConceptMapping>> mapNodeConceptMapping,
			AbstractMappingDocument mappingDocument) {
		super();
		this.owner = owner;
		this.mapNodeConceptMapping = mapNodeConceptMapping;
		this.mappingDocument = mappingDocument;
	}

	public ZSelectItem calculateBeta(Triple tp, POS pos) throws Exception {
		Node tpSubject = tp.getSubject();
		Collection<AbstractConceptMapping> cms = 
				this.mapNodeConceptMapping.get(tpSubject);
		AbstractConceptMapping cm = cms.iterator().next();
		ZSelectItem beta = this.calculateBeta(tp, pos, cm);
		return beta;
	}
	
	private ZSelectItem calculateBeta(Triple tp, POS pos
			, AbstractConceptMapping cm)
	throws Exception {
		String predicateURI = tp.getPredicate().getURI();;
		ZSelectItem selectItem = null;

		if(pos == POS.sub) {
			selectItem = this.calculateBetaSubject(cm);
		} else if(pos == POS.pre) {
			selectItem = this.calculateBetaPredicate(predicateURI, cm);
		} else if(pos == POS.obj) {
			selectItem = this.calculateBetaObject(cm, tp);
		}
		
		logger.debug("beta " + pos + " = " + selectItem);
		return selectItem;
	}

	public abstract ZSelectItem calculateBetaObject(
			AbstractConceptMapping cm, Triple triple)
	throws QueryTranslationException;

	public ZSelectItem calculateBetaPredicate(String predicateURI, AbstractConceptMapping cm) {
		ZConstant predicateURIConstant = new ZConstant(predicateURI, ZConstant.STRING);
		SQLSelectItem selectItem = new SQLSelectItem();
		selectItem.setExpression(predicateURIConstant);
		logger.debug("calculateBetaCMPredicate = " + selectItem);
		return selectItem;
	}
	
	public abstract ZSelectItem calculateBetaSubject(AbstractConceptMapping cm);

	public AbstractQueryTranslator getOwner() {
		return owner;
	}

	
}
