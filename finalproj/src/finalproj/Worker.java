package finalproj;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;




public class Worker implements Runnable{
	private List<Message> messages;
	private String queueURL;
	private AmazonSQS sqs;
	private Connection conn;
	
	
	
	public Worker(List<Message> m, String queueURL, AmazonSQS s, Connection conn){
		this.messages =m;
		this.queueURL = queueURL;
		this.sqs = s;
		this.conn = conn;
		
	}
	@Override
	public void run(){
		System.out.println(Thread.currentThread().toString());
		for(Message message : messages){
			System.out.println("  Message");
            System.out.println("    MessageId:     " + message.getMessageId());
            System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
            System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
            System.out.println("    Body:          " + message.getBody());
            
//            for (Entry<String, String> entry : message.getAttributes().entrySet()) {
//                System.out.println("  Attribute");
//                System.out.println("    Name:  " + entry.getKey());
//                System.out.println("    Value: " + entry.getValue());
//            }
            
            
            Map<String, String> entry = message.getAttributes();
            System.out.println("GMF get:"+entry.get("ApproximateReceiveCount"));
            
            int times = Integer.parseInt(entry.get("ApproximateReceiveCount")) ;
            System.out.println("Message:"+message.getMessageId()+" has received "+times+" times");
            if(times>1)
            	continue;
            
            
            String op=null;
            JSONObject jsonObj = null;
            try {
            	jsonObj= new JSONObject(message.getBody());
            	op = jsonObj.getString("OP");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

            
            
            Statement stmt = null;
            try {
				stmt = conn.createStatement();
				//stmt.execute("");
				
				
				
				if(op.equals("insert")){
			    	
			    	JSONObject msg = jsonObj.getJSONObject("MSG");
			    	String type = msg.getString("type");
			    	if(type.equals("normal")){//////if it is normal
			    		
			    		//double a=0;
			    		String add = "INSERT INTO Normal (text,longtitude,latitude,userid)"
			    				+ " VALUES('"+msg.getString("text")+"', '"+msg.getString("lng")+"' ,'"
			    				+ msg.getString("lat")+"','"+msg.getString("userid")+"');";
			    		stmt.execute(add);
			    	}
			    		
			    	else if(type.equals("emergency")){///if it is emergency 
			    		String add = "INSERT INTO Emergency (text,longtitude,latitude,userid,abstract)"
			    				+ " VALUES('"+msg.getString("text")+"', '"+msg.getString("lng")
			    				+"' ,'"+msg.getString("lat")+"','"+msg.getString("userid")
			    				+"','"+msg.getString("abstract")+"');";		    		
			    		stmt.execute(add);
			    			
			    	}
			    		
			    	else if(type.equals("importance")) {///////if is and importance
			    		String add = "INSERT INTO Importance (text,longtitude,latitude,userid,abstract) VALUES('"+msg.getString("text")+"', '"+msg.getString("lng")+"' ,'"+msg.getString("lat")+"','"+msg.getString("userid")+"','"+msg.getString("abstract")+"');";		    		
			    		stmt.execute(add);
			    	}
			    	
			    }
			    
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
			    	
			    	}
			    }
				
				
				
			} catch (SQLException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
            
            
			
			// delete data
			String messageRecieptHandle = message.getReceiptHandle();
			sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(queueURL)
			    .withReceiptHandle(messageRecieptHandle));
			
		}
		
	
	}
}
