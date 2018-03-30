import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class InputThread extends Thread {

//	private volatile List<ProcessImage> blockedQueue;
//	private volatile List<ProcessImage> readyQueue;
	
	private volatile List<Integer> consoleInputQueue;

	private Semaphore mutex;
	private Semaphore full;
	private Semaphore empty;

	private volatile boolean isRunning;

	public InputThread(Semaphore mtx, List<ProcessImage> blockedQ, List<ProcessImage> readyQ, List<Integer> consoleIQ, Semaphore fll, Semaphore mpty) {
		this.mutex = mtx;
//		this.blockedQueue = blockedQ;
//		this.readyQueue = readyQ;
		this.consoleInputQueue = consoleIQ;
		this.full = fll;
		this.empty = mpty;
		
	}
	
	public void produce(int input) {
		try {
			empty.acquire();
			mutex.acquire();

			System.out.println("Storing input(" + input + ") in Console Input Queue...");
			this.consoleInputQueue.add(input);

			mutex.release();
			full.release();
		} catch (InterruptedException e) {
			mutex.release();
			empty.release();
		}
	}

	@Override
	public void run(){
		isRunning = true;
		Scanner in = new Scanner(System.in); 
		while (isRunning) {
			int i = -1;
			if(in.hasNextInt())
				i = in.nextInt();
			in.close();

			this.produce(i);
		}
	}

	public void stopThread() {
		isRunning = false;
	}
}
