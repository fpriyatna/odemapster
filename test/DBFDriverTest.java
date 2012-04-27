import java.sql.*;

public class DBFDriverTest {

  public static void main(String[] args) {
    try {
      // load the driver into memory
      //Class.forName("jstels.jdbc.dbf.DBFDriver");
    	Class.forName("com.hxtt.sql.dbf.DBFDriver");

      // create a connection. The first command line parameter is assumed to
      // be the directory in which the .dbf files are held
      //Connection conn = DriverManager.getConnection("jdbc:jstels:dbf:" + args[0]);
    	Connection conn = DriverManager.getConnection("jdbc:DBF:////home/fpriyatna/Dropbox/oeg/odemapster2/mappings/r2rml-mappings/r2rml-atlashp/");
//    	Connection conn = DriverManager.getConnection("jdbc:DBF:////home/fpriyatna/Dropbox/oeg/odemapster2/mappings/r2rml-mappings/r2rml-atlashp/", "", "");

      // create a Statement object to execute the query with
      Statement stmt = conn.createStatement();

      // execute a query
      ResultSet rs = stmt.executeQuery("SELECT * FROM \"atlashp.dbf\"");

      // read the data and put it to the console
      for (int j = 1; j <= rs.getMetaData().getColumnCount(); j++) {
        System.out.print(rs.getMetaData().getColumnName(j) + "\t");
      }
      System.out.println("----------------------------------");

      while (rs.next()) {
        for (int j = 1; j <= rs.getMetaData().getColumnCount(); j++) {
          System.out.print(rs.getObject(j) + "\t");
        }
        System.out.println();
      }

      // close the objects
      rs.close();
      stmt.close();
      conn.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}