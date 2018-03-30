import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class OS extends Thread {

	private final int QUANTUM = 5;

	private CPU cpu;
	private Memory memory;
	private volatile List<ProcessImage> readyQueue;
	private volatile List<ProcessImage> blockedQueue;
	private Semaphore mutex;
	private InputThread inputThread;
	private volatile List<ProcessImage> fileInputQueue;
	private Semaphore fullFileInput;
	private Semaphore emptyFileInput;
	private ProducerFileInput producerFileInput;
	private ConsumerFileInput consumerFileInput;
	private volatile List<Integer> consoleInputQueue;
	private Semaphore fullConsoleInput;
	private Semaphore emptyConsoleInput;
	private ConsumerConsoleInput consumerConsoleInput;

	public OS(int size) {
		this.memory = new Memory(size);
		this.cpu = new CPU(memory);
		this.mutex=new Semaphore(1);
		this.readyQueue = new ArrayList<ProcessImage>();
		this.blockedQueue = new ArrayList<ProcessImage>();
		this.fileInputQueue = new ArrayList<ProcessImage>(5);
		this.fullFileInput = new Semaphore(0);
		this.emptyFileInput = new Semaphore(5);
		this.consoleInputQueue = new ArrayList<Integer>(5);
		this.fullConsoleInput = new Semaphore(0);
		this.emptyConsoleInput = new Semaphore(5);

		
		this.producerFileInput = new ProducerFileInput(mutex, fullFileInput, emptyFileInput, fileInputQueue);
		producerFileInput.start();
		this.consumerFileInput = new ConsumerFileInput(mutex, fullFileInput, emptyFileInput, fileInputQueue, memory, readyQueue);
		consumerFileInput.start();
		this.inputThread = new InputThread(mutex, blockedQueue, readyQueue, consoleInputQueue, fullConsoleInput, emptyConsoleInput);
		inputThread.start();
		this.consumerConsoleInput = new ConsumerConsoleInput(mutex, blockedQueue, readyQueue, consoleInputQueue, fullConsoleInput, emptyConsoleInput);
		consumerConsoleInput.start();
	}

	@Override
	public void run() {
		try {
			while (true) {

				mutex.acquire();
				boolean isBlockedQueueEmpty = blockedQueue.isEmpty();
				boolean isReadyQueueEmpty = readyQueue.isEmpty();
				mutex.release();


				if (!isReadyQueueEmpty) {
					System.out.println("Executing " + (readyQueue.get(0)).processName);
					cpu.transferFromImage(readyQueue.get(0));
					for (int i = 0; i < QUANTUM; i++) {
						if (cpu.getPC() < cpu.getLR()) {
							cpu.fetch(); 
							int returnCode = cpu.decodeExecute();

							if (returnCode == 0)  {
								System.out.println("Process " + readyQueue.get(0).processName + " made a system call for ");
								if (cpu.getV() == 0) {
									System.out.println( "Input, transfering to blocked queue and waiting for input...");
									ProcessImage p=new ProcessImage();
									this.cpu.transferToImage(p);
									
									mutex.acquire();
									readyQueue.remove(0);
									blockedQueue.add(p);
									mutex.release();
								} 
								else { //syscall for output
									System.out.print("Output Value: ");
									ProcessImage p=new ProcessImage();
									cpu.transferToImage(p);

									mutex.acquire();
									readyQueue.remove(0);
									System.out.println( p.V +"\n");
									readyQueue.add(p);
									mutex.release();
								}
								//Process blocked, need to end quantum prematurely
								break;
							}
						}
						else {
							System.out.println("Process " + readyQueue.get(0).processName +" has been finished! Removing from the queue..." );
							ProcessImage p = new ProcessImage();
							cpu.transferToImage(p);
							p.writeToDumpFile();

							mutex.acquire();
							System.out.println( "Deallocating memory used by process "+p.processName+"...\n");
							memory.deallocate(p.BR, p.LR);
							readyQueue.remove(0);
							mutex.release();
							break;
						}

						if (i == QUANTUM - 1) {
							//quantum finished put the process at the end of readyQ
							System.out.println ("Context Switch! Allocated quantum have been reached, switching to next process...\n");
							ProcessImage p = new ProcessImage();
							cpu.transferToImage(p);  

							mutex.acquire();
							readyQueue.remove(0);
							readyQueue.add(p);
							mutex.release();
						}
					}
				}
				else {
					System.out.println( "OS Thread is sleeping 2 seconds...");
					Thread.sleep(2000);
				}
			}
			/*producerFileInput.stopThread();
			consumerFileInput.stopThread();
			inputThread.stopThread();
			consumerConsoleInput.stopThread();
			System.out.println("Execution of all processes has finished!");*/
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
