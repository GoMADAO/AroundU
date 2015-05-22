package finalproj;

import java.io.IOException;
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
      		System.out.println("Server-Write Initializaion");
	      	return;
      	}
		JSONObject jsonObj=null;
		try {
			jsonObj = new JSONObject(request.getParameter("data").toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
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
			
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
