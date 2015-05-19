package finalproj;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class User extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

    public User() {
        super();       
    }

	@SuppressWarnings({ "unchecked", "unused" })
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    JSONObject possibleRequest = new JSONObject();
	    JSONObject userInfo = null;
	    
     	try{
	     	possibleRequest.put("userid","hello world");
	     	possibleRequest.put("isblock","0");
	     	possibleRequest.put("isactive","0");
	      	JSONParser jsonParser = new JSONParser();
    		userInfo = (JSONObject) jsonParser.parse(possibleRequest.toString());	      	
     	}catch(Exception ex){
     		ex.printStackTrace();
     	}
     	
     	
		Connection conn = null;
		
		try {

			Class.forName("com.mysql.jdbc.Driver");					
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}
		
	
	    Statement writeStatement = null;
	    ResultSet resultSet = null;	 
		
           
         try {
           if(conn==null)
        	   conn = DriverManager.getConnection(Global.jdbcUrl); 
            writeStatement = conn.createStatement();
            writeStatement.execute("INSERT INTO User (userid,isblock,isactive) VALUES('"+userInfo.get("userid")+"','"+userInfo.get("isblock")+"','"+userInfo.get("isactive")+"')");
            conn.close();
         } catch (SQLException e) {
 			e.printStackTrace();
 	   	}
      
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
