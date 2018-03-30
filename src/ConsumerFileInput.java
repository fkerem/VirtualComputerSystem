import java.util.List;
import java.util.concurrent.Semaphore;

public class ConsumerFileInput extends Thread {
	
	private Semaphore mutex;
	private Semaphore full;
	private Semaphore empty;
	private volatile List<ProcessImage> fileInputQueue;
	private Memory memory;
	private volatile List<ProcessImage> readyQueue;
	
	private volatile boolean isRunning;
	
	public ConsumerFileInput(Semaphore mtx, Semaphore fll, Semaphore mpty, List<ProcessImage> FInputQ, Memory mem, List<ProcessImage> rQ) {
		this.mutex = mtx;
		this.full = fll;
		this.empty = mpty;
		this.fileInputQueue = FInputQ;
		this.memory = mem;
		this.readyQueue = rQ;
	}
	
	private ProcessImage remove_img() {
		ProcessImage img = null;
		try {
			full.acquire();
			mutex.acquire();
			
			img = fileInputQueue.remove(0);
			System.out.println( "Removing process image of "+img.processName+" from File Input Queue...");
			
			mutex.release();
			empty.release();
		} catch (InterruptedException e) {
			mutex.release();
			full.release();
		}
		return img;
	}
	
	@Override
	public void run() {
		isRunning = true;
		try {
			Assembler assembler = new Assembler();
			while(isRunning) {
				ProcessImage img = remove_img();

				int index;
				while(true) { //Looking for an empty space
					index = this.memory.availableIndex(img.LR);
					if(index != -1)
						break;
					System.out.println( "Consumer Thread for File Input Queue is sleeping for 2 seconds...");
					Thread.sleep(2000);
				}
				char[] process = assembler.readBinaryFile(img.LR, img.processName.substring(0, img.processName.length() - 3)+"bin");

				mutex.acquire();
				System.out.println("Loading process " + img.processName + " to memory...");
				this.memory.addInstructions(process, img.LR, index);
				img.BR = index;
				System.out.println("Adding process" + img.processName + " to Ready Queue...");
				readyQueue.add(img);
				mutex.release();
			}
		} catch (InterruptedException e6) {
			e6.printStackTrace();
		}
	}
	
	public void stopThread() {
		isRunning = false;
	}
	
}
