import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.io.IOException;

public class ProducerFileInput extends Thread {
	
	private Semaphore mutex;
	private Semaphore full;
	private Semaphore empty;
	private volatile List<ProcessImage> fileInputQueue;
	
	private volatile boolean isRunning;
	
	public ProducerFileInput(Semaphore mtx, Semaphore fll, Semaphore mpty, List<ProcessImage> FInputQ) {
		this.mutex = mtx;
		this.full = fll;
		this.empty = mpty;
		this.fileInputQueue = FInputQ;
	}
	
	private void produce(ProcessImage img) {
		try {
			empty.acquire();
			mutex.acquire();

			System.out.println( "Adding process image of "+ img.processName +" to File Input Queue...");
			fileInputQueue.add(img);

			mutex.release();
			full.release();
		} catch (InterruptedException e) {
			mutex.release();
			empty.release();
		}
	}
	
	@Override
	public void run() {
		isRunning = true;
		try {
			Assembler assembler = new Assembler();
			while(isRunning) {
				BufferedReader br=new BufferedReader(new FileReader("inputSequence.txt")); //Reading line by line.
				String line = "";
				while((line = br.readLine()) != null && line.trim().isEmpty()==false) {
					String[]storage=line.split(" "); //Splitting words in each line.
					String processFile = storage[0];
					int timeToWait = Integer.parseInt(storage[1]);
					
					System.out.println( "Creating binary file for "+ processFile+"...");
					int instructionSize = assembler.createBinaryFile(processFile, processFile.substring(0, processFile.length() - 3)+"bin");
					
					this.produce(new ProcessImage(processFile, -1, instructionSize));
					System.out.println( "Producer Thread for File Input Queue is sleeping "+timeToWait+" ms...");
					Thread.sleep(timeToWait);
				}
			}
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		} catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (NumberFormatException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		} catch (IOException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		}
	}
	
	public void stopThread() {
		isRunning = false;
	}
	
}
