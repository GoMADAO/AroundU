package finalproj;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class read extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public read() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		System.out.println(request.getParameter("data"));
		
		Connection conn = null;
		JSONArray normalText = new JSONArray();
		JSONArray emergencyText = new JSONArray();
		JSONArray importanceText = new JSONArray();
		JSONObject sendingText = new JSONObject();
		JSONArray topiclist = new JSONArray();

		 // System.out.println(request.getParameter("range"));
		  try {
			  Class.forName("com.mysql.jdbc.Driver");
		  } catch (ClassNotFoundException e) {
			  throw new RuntimeException("Cannot find the driver in the classpath!", e);
		  }
		  
		  Statement readStatement = null;
		  ResultSet resultSet1 = null;
		  ResultSet resultSet2 = null;
		  ResultSet resultSet3 = null;
		  ResultSet resultSet4 = null;
		  
		  
		  try {
				if(conn==null)
					conn = DriverManager.getConnection(Global.jdbcUrl);
			    
			    readStatement = conn.createStatement();
			    
			   
			    resultSet1 = readStatement.executeQuery("SELECT * FROM Normal WHERE (longtitude between -73.96651 AND -73.90651) AND "
			    		+ "(latitude between 40.7038597 AND 40.8038597);");		
			    while (resultSet1.next()) {
			    	JSONObject text = new JSONObject();	
				    text.put("text",resultSet1.getString("text"));
				    text.put("topic",resultSet1.getString("topic"));
				    text.put("likes",resultSet1.getString("likes"));
				    text.put("lon",resultSet1.getString("longtitude"));
				    text.put("lat",resultSet1.getString("latitude"));
				    text.put("time",resultSet1.getString("time"));
				    text.put("sentiment",resultSet1.getString("sentiment"));
				    text.put("dislikes",resultSet1.getString("dislikes"));
				    text.put("userid",resultSet1.getString("userid"));
				    text.put("time",resultSet1.getString("time"));
				    text.put("id",resultSet1.getString("id"));
				    normalText.add(text);			    	
			    }
			   
			    
			
			    resultSet2 = readStatement.executeQuery("SELECT * FROM Emergency;");		
			    while (resultSet2.next()) {
			    	JSONObject text = new JSONObject();			
			        text.put("text",resultSet2.getString("text"));
			        text.put("abstract",resultSet2.getString("abstract"));
			        text.put("userid",resultSet2.getString("userid"));
			        text.put("report",resultSet2.getString("reporttimes"));
			        text.put("map",resultSet2.getString("mapOn"));
			        text.put("time",resultSet2.getString("time"));
			        text.put("id",resultSet2.getString("id"));
			        emergencyText.add(text);
			    }
			    
		
			    resultSet3 = readStatement.executeQuery("SELECT * FROM Importance;");		
			    while (resultSet3.next()) {
			    	JSONObject text = new JSONObject();				    
			    	text.put("text",resultSet3.getString("text"));
			    	 text.put("abstract",resultSet3.getString("abstract"));
			    	text.put("topic",resultSet3.getString("topic"));			    		
			    	text.put("sentiment",resultSet3.getString("sentiment"));			    		
			    	text.put("userid",resultSet3.getString("userid"));
			    	text.put("report",resultSet3.getString("reporttimes"));
			    	text.put("time",resultSet3.getString("time"));
			    	text.put("id",resultSet3.getString("id"));
			    	importanceText.add(text);
			    }
			    
			    resultSet4 = readStatement.executeQuery("select distinct topic from Normal where topic != 'No Topic found'");
			    while(resultSet4.next()){
			    	topiclist.add(resultSet4.getString("topic"));
			    }
			    
			    
			  } catch (SQLException ex) {
			    // handle any errors
			    System.out.println("SQLException: " + ex.getMessage());
			    System.out.println("SQLState: " + ex.getSQLState());
			    System.out.println("VendorError: " + ex.getErrorCode());
			  }
		  
//		  System.out.println(normalText);
//		  System.out.println(emergencyText);
//		  System.out.println(importanceText);
		  
		  sendingText.put("normal", normalText);
		  sendingText.put("emergency", emergencyText);
		  sendingText.put("importance",importanceText);
		  sendingText.put("topiclist",topiclist);
		  
		  System.out.println(sendingText);
//		  PrintWriter out = response.getWriter();		  
//		  out.println(sendingText);
//		  out.flush();
		  
		  OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());		  
		  writer.write(sendingText.toString());
		  
		  writer.flush();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
