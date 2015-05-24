package finalproj;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;


public class Originaltextcluster {
	
	
	 @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public static void main(String[] argc){
		 
		 	//clustering analysis setup
			JSONObject checkingText = new JSONObject();		
			checkingText.put("type", "pre-sentenced");				
			JSONArray jarray = new JSONArray();
			
			//sentiment analysis setup
			HttpClient senticlient = HttpClients.createDefault();
			HttpPost post = new HttpPost("http://access.alchemyapi.com/calls/text/TextGetTextSentiment");
			org.apache.http.HttpResponse sentiresponse;
			HttpEntity entity;
			List<NameValuePair> params = new ArrayList<NameValuePair>(2);
			params.add(new BasicNameValuePair("apikey", Global.Alchemykey));
			params.add(new BasicNameValuePair("outputMode","json"));
			String scoretype = null;
			int i=0;
			
			try {	
					Connection conn = null;
					
					try {
			
						Class.forName("com.mysql.jdbc.Driver");					
					} catch (ClassNotFoundException e) {
						throw new RuntimeException("Cannot find the driver in the classpath!", e);
					}

				   Statement readStatement = null;
				   Statement writeStatement = null;
				   ResultSet resultSet = null;	 
				
		  
				   try {
					   if(conn==null)
						   conn = DriverManager.getConnection(Global.jdbcUrl1);
				    
					    readStatement = conn.createStatement();
					    writeStatement = conn.createStatement();
				        resultSet = readStatement.executeQuery("SELECT * FROM Normal;");
				        int count = 0;
				        
					    while (resultSet.next()) {//reading database row by row and analysis sentiment
					    		
					    		String dbtext = resultSet.getString("text");
					    		int id = resultSet.getShort("id");
					    		if(count == 0) i = id;
					    		params.add(new BasicNameValuePair("text", dbtext));
					    		
					    		try {
			                		post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			                		sentiresponse = senticlient.execute(post);
			                		entity = sentiresponse.getEntity();
			                		BufferedReader rd = new BufferedReader(new InputStreamReader(sentiresponse.getEntity().getContent()));
			                		JSONParser jsonParser = new JSONParser();
			                		JSONObject jsonObject = (JSONObject) jsonParser.parse(rd);

			                		if(jsonObject.get("status").toString().equals("OK")){
			             
				                		JSONObject temp = (JSONObject) jsonObject.get("docSentiment");
				                		scoretype= temp.get("type").toString();
				                				                				               		
			                		//insert back to the database
			                	     writeStatement.execute("UPDATE Normal SET sentiment = '" + scoretype + "' where id = "+id);
			                		} 
			                		
			                	} catch (UnsupportedEncodingException e) {
			                		// TODO Auto-generated catch block
			                		e.printStackTrace();
			                	}
				    		
					    		count++;
					    		//adding to clustering Json
					    		JSONObject text = new JSONObject();	
					    		text.put("sentence",dbtext);
					    		jarray.add(text);
				    }
					
					checkingText.put("text",jarray); 
					
				    
 				    } catch (SQLException ex) {
				      // handle any errors
				      System.out.println("SQLException: " + ex.getMessage());
				      System.out.println("SQLState: " + ex.getSQLState());
				      System.out.println("VendorError: " + ex.getErrorCode());
				    }
		

					
					System.out.println(checkingText.toString());
					
					//sending clustering evaluation request to the cluster API
					HttpResponse<JsonNode> response = Unirest.post("https://rxnlp-core.p.mashape.com/generateClusters")
					.header("X-Mashape-Key", Global.Mashapekey)
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")					 
					.body(checkingText.toString())
					.asJson();

					System.out.println(response.getBody());
					JSONParser jsonParser = new JSONParser();
					JSONObject clusterResult = (JSONObject) jsonParser.parse(response.getBody().toString());
					JSONObject temp1 = (JSONObject) clusterResult.get("results");
					JSONArray temp2 = (JSONArray) temp1.get("clusters");
					
					Statement writeStatement2 = conn.createStatement();
					
					//receiving results and store back to database;
					Iterator i1 = temp2.iterator();
					while(i1.hasNext()){
						JSONObject temp3 = (JSONObject) i1.next();
						JSONArray temp4 = (JSONArray) temp3.get("clusteredSentences");
						Iterator i2 = temp4.iterator();
						while(i2.hasNext()){
							String sentenceidTemp = i2.next().toString();
							int id = Integer.parseInt(sentenceidTemp.substring(0,4));
							//map clustering id to database id
							id = id + i;		
							if(!temp3.get("clusterTopics").equals("[sentences_with_no_cluster_membership]"))								
								writeStatement2.execute("UPDATE Normal SET topic = '"+ temp3.get("clusterTopics").toString().split(":")[0].replace("[", "").replace("]", "") +"' where id =" + id);													
							else
								writeStatement2.execute("UPDATE Normal SET topic = 'No Topic found' where id =" + id);													

						}
					}
					conn.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
	}

}
