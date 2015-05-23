package finalproj;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
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
	
	private String parse(String content){
		String sent = null;
		try {
			JSONObject pkg = new JSONObject(content);
			String status = pkg.getString("status");
			
			System.out.println("this is the status"+status);
			if(!status.equals("OK"))
				return null;
						
			JSONObject docSen =pkg.getJSONObject("docSentiment"); 
			
			System.out.println();
			if (docSen.has("score"))
				sent = docSen.getString("score");
			else
				sent = "0";
		} catch (JSONException e) {

			e.printStackTrace();
		}
		return sent;
		
	}
	
	
	private String callAlchemy(String data){
		
		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost("http://access.alchemyapi.com/calls/text/TextGetTextSentiment");
		HttpResponse response = null;
		HttpEntity entity = null;
		
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("apikey", Global.Alchemykey));
		params.add(new BasicNameValuePair("text", data));
		params.add(new BasicNameValuePair("outputMode","json"));
		
		try {
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
		
		// Execute and get the response.
		try {
			response = client.execute(post);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		entity = response.getEntity();
		
		String rt = null;
		
		if (entity != null) {
		    BufferedReader br=null;
		    String line;
			try {
				br = new BufferedReader(new InputStreamReader(entity.getContent()));
				StringBuffer sb = new StringBuffer();
				while((line=br.readLine())!=null){
					sb.append(line);
				}
				br.close();
				
				rt = parse(sb.toString());
				
				
			}catch (IllegalStateException | IOException e1) {
				
				e1.printStackTrace();
			}
		}
		return rt;
		
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
            
            System.out.println("Processing message:"+message.getMessageId());
            
            String op=null;
            JSONObject jsonObj = null;
            try {
            	jsonObj= new JSONObject(message.getBody());
            	op = jsonObj.getString("OP");
			} catch (JSONException e1) {
				
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
			    		
			    		
			    		String senti = callAlchemy(msg.getString("text"));
			    		if(senti==null){
			    			
			    			senti = "neutral";
			    		}
			    		
			    		//double a=0;
			    		String add = "INSERT INTO Normal (text,longtitude,latitude,userid,sentiment,time)"
			    				+ " VALUES('"+msg.getString("text")+"', '"+msg.getString("lng")+"' ,'"
			    				+ msg.getString("lat")+"','"+msg.getString("userid")+"','"
			    						+ senti+"', Now()-Interval 4 HOUR);";
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
			    
			    
				
				
				
			} catch (SQLException | JSONException e) {
				
				e.printStackTrace();
			}finally{
				try {
					stmt.close();
				} catch (SQLException e) {
					
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
