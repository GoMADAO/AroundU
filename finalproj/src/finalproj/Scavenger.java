package finalproj;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;






public class Scavenger {
	public static void go()  {
		// TODO Auto-generated method stub
		
		  Connection conn = null;
		  
		  
		  try {
			
			    Class.forName("com.mysql.jdbc.Driver");
	
			  } catch (ClassNotFoundException e) {
			    throw new RuntimeException("Cannot find the driver in the classpath!", e);
			  }
		  	Statement deleteStatement = null;

		  	
		
				try {
					if(conn==null)
						conn = DriverManager.getConnection(Global.jdbcUrl);
				    
				    deleteStatement = conn.createStatement();	
				    deleteStatement.execute("DELETE FROM Normal WHERE time < (NOW() - INTERVAL 1000 MINUTE);");

				  } catch (SQLException ex) {
				    // handle any errors
				    System.out.println("SQLException: " + ex.getMessage());
				    System.out.println("SQLState: " + ex.getSQLState());
				    System.out.println("VendorError: " + ex.getErrorCode());
				  }
				

	}
}