package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OArgumentRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ORestriction;

public class R2OUtility {
	public static void renameColumns(R2OArgumentRestriction ar, String oldName, String newName, boolean matches) {

		R2ORestriction restriction = ar.getRestriction();
		if(restriction instanceof R2OColumnRestriction) {
			R2OColumnRestriction columnRestriction = (R2OColumnRestriction) restriction;
			R2ODatabaseColumn dbColumn = columnRestriction.getDatabaseColumn();
			String dbColumnFullName = dbColumn.getFullColumnName();
			if(dbColumnFullName.startsWith(oldName+".") == matches) {
				String newColumnName = dbColumnFullName.replaceFirst(oldName+".", newName+".");
				dbColumn.setFullColumnName(newColumnName);
			} 
		}
	}


}
