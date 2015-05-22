package finalproj;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;




public class User extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

    public User() {
        super();       
    }

	@SuppressWarnings({ "unchecked", "unused" })
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String s = request.getParameter("data");
		if (s==null || s.equals("")){
			return;
		}
		Connection conn = null;
		
		try {

			Class.forName("com.mysql.jdbc.Driver");	
			if (conn == null)
	    		conn = DriverManager.getConnection(Global.jdbcUrl);
			
		} catch (ClassNotFoundException | SQLException e) {
			throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}
		
		JSONObject json = null;
		String op = null;
		String uid = null;
		try {
			json = new JSONObject(s);
			 op= json.getString("OP");
			 uid = json.getString("userid");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		if(op.equals("insert")){
			Statement writeStatement = null;
	           
	        try {
	            writeStatement = conn.createStatement();
	            writeStatement.execute("INSERT INTO User (userid,isblock,isactive) VALUES('"+uid+"','0','1');");
	            writeStatement.close();
	            
	        } catch (SQLException e) {
	 			e.printStackTrace();
	        }
	   
		}else if(op.equals("select")){
			Statement stmt;
			String sendingText;
			JSONObject rt = new JSONObject();
			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM USER WHERE userid = '"+uid+"';");
				while(rs.next()){
					rt.put("isactive", (Integer.parseInt(rs.getString("isactive"))==1)?true:false );
					rt.put("isblock", (Integer.parseInt(rs.getString("isblock"))==1)?true:false);					
				}
				rs.close();
				stmt.close();
				
			} catch (SQLException | NumberFormatException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());		  
			writer.write(rt.toString());
			writer.flush();
			
			
		}else if(op.equals("block")){
			
			Statement writeStatement = null;
	           
	        try {
	            writeStatement = conn.createStatement();
	            writeStatement.execute("UPDATE USER SET isblock = 1 WHERE userid='"+uid+"';");
	            writeStatement.close();
	            
	        } catch (SQLException e) {
	 			e.printStackTrace();
	        }
			
		}else if(op.equals("unblock")){
			
			Statement writeStatement = null;
	           
	        try {
	            writeStatement = conn.createStatement();
	            writeStatement.execute("UPDATE USER SET isblock = 0 WHERE userid='"+uid+"';");
	            writeStatement.close();
	            
	        } catch (SQLException e) {
	 			e.printStackTrace();
	        }
			
		}else{
			System.err.println("Operation does not match anything!");
		}
		
		
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
		return;
		
		
	
	    
      
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
