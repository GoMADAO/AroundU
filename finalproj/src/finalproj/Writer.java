package finalproj;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class Writer
 */
public class Writer extends HttpServlet {
	
	
	static Connection conn = null;
	static Statement setupStatement = null;
	// Statement setupStatement = null;
	 static Statement readStatement = null;
	 static PreparedStatement sql;
	 
	 static ResultSet res = null;
	 static String results = "";
	 int numresults = 0;
	 //static Connection conn = null;

	
	private static final long serialVersionUID = 1L;
       
    /**
     * @throws JSONException 
     * @see HttpServlet#HttpServlet()
     */
    public Writer() throws JSONException {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject possibleRequest = new JSONObject();
     	JSONObject inner = new JSONObject();
     	String op=null;
     	JSONObject jsonObj=null;
     	try{
	     	inner.put("type","emergency");
	     	inner.put("userid","13");
	     	inner.put("lat",28);
	     	inner.put("lng",45);
	     	inner.put("id",3);
	     	inner.put("text","aaaaaahassasjhkj");
	     	possibleRequest.put("MSG",inner);
	     	possibleRequest.put("OP","insert");
	      	System.out.println(possibleRequest);
	      	if(request.getParameter("data")!=null){
	      		jsonObj = new JSONObject(request.getParameter("data").toString());//{...}
		      	op = jsonObj.getString("OP");
	      	}
	      		
     	} catch(Exception ex){
     		ex.printStackTrace();
     	}
      	//----end----
      	
    
    	
    	// Load the JDBC Driver
	    try {
	    	System.out.println("Loading driver...");
	    	Class.forName("com.mysql.jdbc.Driver");
	    	System.out.println("Driver loaded!");
	    } catch (ClassNotFoundException e) {
	    	throw new RuntimeException(
	    			"Cannot find the driver in the classpath!", e);
	    }
	
	    try {
	    	// Create connection to RDS instance
	    	if (conn == null)
	    		conn = DriverManager.getConnection(Global.jdbcUrl);
	
	    } catch (SQLException ex) {
	    	// handle any errors
	    	System.out.println("SQLException: " + ex.getMessage());
	    	System.out.println("SQLState: " + ex.getSQLState());
	    	System.out.println("VendorError: " + ex.getErrorCode());
	    }
	
	    if(op==null){
	    	OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());		  
			  writer.write("");
			  
			  writer.flush();
			  return;
	    }
	    try {
	    	Statement setupStatement = conn.createStatement();
	    	
		    if(op.equals("insert")){
		    	
		    	JSONObject msg = jsonObj.getJSONObject("MSG");
		    	String type = msg.getString("type");
		    	if(type.equals("normal")){//////if it is normal
		    		
		    		//double a=0;
		    		String add = "INSERT INTO Normal (text,longtitude,latitude,userid) VALUES('"+msg.getString("text")+"', '"+msg.getString("lng")+"' ,'"+msg.getString("lat")+"','"+msg.getString("userid")+"');";
		    		setupStatement.execute(add);
		    	}
		    		
		    	else if(type.equals("emergency")){///if it is emergency 
		    		String add = "INSERT INTO Emergency (text,longtitude,latitude,userid) VALUES('"+msg.getString("text")+"', '"+msg.getDouble("lng")+"' ,'"+msg.getDouble("lat")+"','"+msg.getString("userid")+"');";
		    		setupStatement.execute(add);
		    			
		    	}
		    		
		    	else if(type.equals("importance")) {///////if is and importance
		    		String add = "INSERT INTO Importance (text,longtitude,latitude,userid) VALUES('"+msg.getString("text")+"', '"+msg.getDouble("lng")+"' ,'"+msg.getDouble("lat")+"','"+msg.getString("userid")+"');";
		    		setupStatement.execute(add);
		    	}
		    	
		    }
		    
		    if(op.equals("like")){
		    	JSONObject msg = jsonObj.getJSONObject("MSG");
		    	String type = msg.getString("type");
		    	if(type.equals("normal")){//////if it is normal
		    		
		    		Statement writeStatement = null;
		    		writeStatement = conn.createStatement();
		    		//System.out.println(msg.getDouble("id"));
		    		//System.out.println("UPDATE Normal SET likes = likes+1 where id = "+ msg.getInt("id")+";");
		    		writeStatement.execute("UPDATE Normal SET likes = likes+1 where id = "+ msg.getInt("id")+";");
		    	
		    	}
		    }
		
		    if(op.equals("dislike")){
		    	JSONObject msg = jsonObj.getJSONObject("MSG");
		    	String type = msg.getString("type");
		    	if(type.equals("normal")) {//////if it is normal
				    Statement writeStatement = conn.createStatement();
				    //System.out.println(msg.getDouble("id"));
				    //System.out.println("UPDATE Normal SET likes = likes+1 where id = "+ msg.getInt("id")+";");
				    writeStatement.execute("UPDATE Normal SET dislikes = dislikes+1 where id = "+ msg.getInt("id")+";");
		    	
		    	}
		    }
		
		    if(op.equals("report")){
		    	JSONObject msg = jsonObj.getJSONObject("MSG");
		    	String type = msg.getString("type");
		    	if(type.equals("emergency")) {//////if it is normal
		    		
		    		Statement writeStatement = null;
		    		writeStatement = conn.createStatement();
		    		//System.out.println(msg.getDouble("id"));
		    		//System.out.println("UPDATE Normal SET likes = likes+1 where id = "+ msg.getInt("id")+";");
		    		writeStatement.execute("UPDATE Emergency SET reporttimes = reporttimes+1 where id = "+ msg.getInt("id")+";");
		    	
		    	}
		    }
	
	    } catch (SQLException | JSONException ex) {
	    	// handle any errors
	    	System.out.println("SQLException: " + ex.getMessage());
	    	System.out.println("SQLState: " + ((SQLException) ex).getSQLState());
	    	System.out.println("VendorError: " + ((SQLException) ex).getErrorCode());
	    }
	    OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());		  
		  writer.write("");
		  
		  writer.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
