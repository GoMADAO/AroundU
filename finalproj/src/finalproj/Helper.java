package finalproj;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;

public class Helper implements Runnable{
	private Future<?> task;
	private AmazonSQS sqs;
	private String queueURL;
	private List<Message> messages;
	
	public Helper(Future<?> task, String queueURL, AmazonSQS sqs, List<Message> messages){
		this.task = task;
		this.queueURL= queueURL;
		this.sqs =sqs;			
		this.messages = messages;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		boolean done = false;
		//System.out.println("Helper is working on!");
		
		int id=0;
		List<ChangeMessageVisibilityBatchRequestEntry> list = 
				new LinkedList<ChangeMessageVisibilityBatchRequestEntry>();
		for(Message message : messages){
			id++;
			ChangeMessageVisibilityBatchRequestEntry entry
			= new ChangeMessageVisibilityBatchRequestEntry 
			(id+"", message.getReceiptHandle());
			entry.setVisibilityTimeout(40);
			list.add(entry);
			
		}						
		while(!done){							
			try {
				task.get(20, TimeUnit.SECONDS);
				done =true;
			} catch (InterruptedException ie){
				Thread.currentThread().interrupt();
				done = true;
			}catch(ExecutionException ee){
				done =true;
			}catch(java.util.concurrent.TimeoutException e) {
				sqs.changeMessageVisibilityBatch(queueURL, list);
				
			}catch(java.util.concurrent.CancellationException ce){
				System.out.println("wrong");
			}
				
			
		}
		
	}

}
