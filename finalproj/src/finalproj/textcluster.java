package finalproj;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;


public class textcluster {
	
	
	 @SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public static void go(){
		 
		 	//clustering analysis setup
			JSONObject checkingText = new JSONObject();		
			checkingText.put("type", "pre-sentenced");				
			JSONArray jarray = new JSONArray();
			
			
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
						   conn = DriverManager.getConnection(Global.jdbcUrl);
				    
					    readStatement = conn.createStatement();
					    writeStatement = conn.createStatement();
				        resultSet = readStatement.executeQuery("SELECT * FROM Normal;");
				        int count = 0;
				        
					    while (resultSet.next()) {//reading database row by row and analysis sentiment
					    		
					    		String dbtext = resultSet.getString("text");
					    		int id = resultSet.getShort("id");
					    		if(count == 0) i = id;
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
		

					
					System.out.println("Initializing clustering");
					
					//sending clustering evaluation request to the cluster API
					HttpResponse<JsonNode> response = Unirest.post("https://rxnlp-core.p.mashape.com/generateClusters")
					.header("X-Mashape-Key", Global.Mashapekey)
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")					 
					.body(checkingText.toString())
					.asJson();

					System.out.println("Clustering Done");
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
