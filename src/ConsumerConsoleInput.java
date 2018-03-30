import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class ConsumerConsoleInput extends Thread {
	
	private volatile List<ProcessImage> blockedQueue;
	private volatile List<ProcessImage> readyQueue;
	
	private volatile List<Integer> consoleInputQueue;
	
	private Semaphore mutex;
	private Semaphore full;
	private Semaphore empty;
	
	private volatile boolean isRunning;
	
	public ConsumerConsoleInput(Semaphore mtx, List<ProcessImage> blockedQ, List<ProcessImage> readyQ, List<Integer> consoleIQ, Semaphore fll, Semaphore mpty) {
		this.mutex = mtx;
		this.blockedQueue = blockedQ;
		this.readyQueue = readyQ;
		this.consoleInputQueue = consoleIQ;
		this.full = fll;
		this.empty = mpty;
	}
	
	public int consume() {
		int input=0;
		try {
			full.acquire();
			mutex.acquire();
			
			input = consoleInputQueue.remove(0);
			System.out.println("Removing input(" + input + ") from Console Input Queue...");
			
			mutex.release();
			empty.release();
		} catch (InterruptedException e) {
			mutex.release();
			full.release();
		}
		return input;
	}
	
	@Override
	public void run(){
		isRunning = true;
		try {
			while (isRunning) {
				int input = this.consume();
				
				boolean isBlockedQueueEmpty;
				while(true) { //Looking for an empty space
					mutex.acquire();
					isBlockedQueueEmpty = blockedQueue.isEmpty();
					mutex.release();
					if(!isBlockedQueueEmpty)
						break;
					System.out.println( "Consumer Thread for Console Input Queue is sleeping 2 seconds...");
					Thread.sleep(2000);
				}
				
				mutex.acquire();
				ProcessImage p = blockedQueue.remove(0);
				System.out.println("Removing process " + p.processName + " from BlockedQueue...");
				p.V = input;
				System.out.println("Adding process " + p.processName + " to Ready Queue...");
				readyQueue.add(p);
				mutex.release();
			}
		} catch (InterruptedException e7) {
			e7.printStackTrace();
		}
	}

	public void stopThread() {
		isRunning = false;
	}
	
}
