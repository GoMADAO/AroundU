package finalproj;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * Servlet implementation class write
 */
public class write extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     * 
     */
	
	
	private static AmazonSQS sqs;
	
    public write() {
        super();
        // TODO Auto-generated constructor stub
        AWSCredentials credentials = null;
		//credentials = new ProfileCredentialsProvider("default").getCredentials();
        try {
            credentials = new PropertiesCredentials(
              MyDeamon.class.getResourceAsStream("AwsCredentials.properties"));
        } catch (IOException e2) {
        // TODO Auto-generated catch block
            e2.printStackTrace();
        }
		sqs = new AmazonSQSClient(credentials);
		
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		
		if(request.getParameter("data")==null){
      		System.out.println("No data");
	      	return;
      	}
		JSONObject jsonObj=null;
		String op=null;
		try {
			jsonObj = new JSONObject(request.getParameter("data").toString());
			op = jsonObj.getString("OP");
			
			Statement stmt = null;
			if(op.equals("insert")){
				SendMessageRequest smr = new SendMessageRequest();
				smr.setQueueUrl(Global.queueURL);
				//TODO body is the json
				smr.setMessageBody(jsonObj.toString());
				Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
				messageAttributes.put("number", 
						new MessageAttributeValue()
							.withDataType("String")
							.withStringValue("0"));
				smr.withMessageAttributes(messageAttributes);
				sqs.sendMessage(smr);
				
			}else{
				Connection conn ;
				conn = DriverManager.getConnection(Global.jdbcUrl);
				stmt = conn.createStatement();
				
				if(op.equals("like")){
			    	JSONObject msg = jsonObj.getJSONObject("MSG");
			    	String type = msg.getString("type");
			    	if(type.equals("normal")){//////if it is normal
			    					    
			    		//System.out.println(msg.getDouble("id"));
			    		//System.out.println("UPDATE Normal SET likes = likes+1 where id = "+ msg.getInt("id")+";");
			    		stmt.execute("UPDATE Normal SET likes = likes+1 where id = "+ msg.getInt("id")+";");
			    	
			    	}
			    }
			
			    if(op.equals("dislike")){
			    	JSONObject msg = jsonObj.getJSONObject("MSG");
			    	String type = msg.getString("type");
			    	if(type.equals("normal")) {//////if it is normal
					    //System.out.println(msg.getDouble("id"));
					    //System.out.println("UPDATE Normal SET likes = likes+1 where id = "+ msg.getInt("id")+";");
					    stmt.execute("UPDATE Normal SET dislikes = dislikes+1 where id = "+ msg.getInt("id")+";");
			    	
			    	}
			    }
			
			    if(op.equals("report")){
			    	JSONObject msg = jsonObj.getJSONObject("MSG");
			    	String type = msg.getString("type");
			    	if(type.equals("emergency")) {//////if it is normal
			    		
			    		//System.out.println(msg.getDouble("id"));
			    		//System.out.println("UPDATE Normal SET likes = likes+1 where id = "+ msg.getInt("id")+";");
			    		stmt.execute("UPDATE Emergency SET reporttimes = reporttimes+1 where id = "+ msg.getInt("id")+";");
			    	
			    	}else if(type.equals("importance")){
			    		stmt.execute("UPDATE Importance SET reporttimes = reporttimes+1 where id = "+ msg.getInt("id")+";");
			    	}
			    	
			    }
			    
			    stmt.close();
			    conn.close();
			}
			
		} catch (JSONException | SQLException e) {
			e.printStackTrace();
		}
		
		
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
