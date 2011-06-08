package es.upm.fi.dia.oeg.obdi.wrapper;



public abstract class AbstractRDB2RDFMapping {
	protected String name;
	protected String documentation;
	protected String id;
	
	public String getName() {
		return name;
	}

	public String getId() {
		if(this.id != null && !this.id.equals("")) {
			return this.id;
		} else {
			return this.hashCode() + "";
		}
	}
	
	
}

