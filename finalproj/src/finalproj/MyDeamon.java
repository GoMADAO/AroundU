package finalproj;

import java.io.IOException;




import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;





import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import finalproj.appThread.Workerpool;
import finalproj.appThread.reader;

public class MyDeamon extends Thread{
	
	
	private static AmazonSQS sqs;
	private static Connection conn = null;
	
	
	
	public static class reader implements Runnable{

		public synchronized void run(){
			
			int Interval = 1000;
			try{	
				while(true){
					finalproj.Scavenger.go();
					Thread.sleep(Interval);
				}
													
			} catch(Exception ex){
				
			}
		}
	}
	
	
	
	public static class Workerpool implements Runnable{
		public synchronized void run(){
			
			//reader push message every Interval ms
			int Interval = 1000;
			try {
				while(true){
				//	finalproj.textcluster.go();
					Thread.sleep(Interval);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
 
	
	private static void init() {
		
		
		System.out.println("--------------Deamon initialization----------------");
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
		
		
		//Create connections
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
	    
	    System.out.println("----------------------Done-------------------------");
	    
	}
	
	public static void main(String[] args) {
		
		//Initialization
		
		init();
		
		//Run cluster and scavenger
		try{
			
			Thread t1 = new Thread(new reader());
            t1.start();
		
            Thread t3 = new Thread(new Workerpool());
            t3.start();
          
            
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		
		//Run worker 
		while(true){
			ExecutorService e = Executors.newFixedThreadPool(15);
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(Global.queueURL);
			//Message m = sqsClient.receiveMessage(receiveMessageRequest);
			
			List<Message> messages  = sqs.receiveMessage(receiveMessageRequest
					.withAttributeNames("ApproximateReceiveCount")
					.withMessageAttributeNames("number"))
						.getMessages();
			if(messages.isEmpty()){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}else{
				Future<?> task = e.submit(new Worker(messages,  Global.queueURL,  sqs, conn));
				e.execute(new Helper(task, Global.queueURL,  sqs, messages));				
			}
			
			
		}
	}

	
}
