package finalproj;






public class  appThread{


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
	
	
	
	
	public static void main(String argc[]){
		

		
		try{
			
			Thread t1 = new Thread(new reader());
            t1.start();
		
            Thread t3 = new Thread(new Workerpool());
            t3.start();
          
            
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
}



