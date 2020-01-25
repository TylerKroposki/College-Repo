import java.math.BigDecimal;

public class Application implements Runnable{

	//Violation #1
	//https://wiki.sei.cmu.edu/confluence/display/java/NUM10-J.+Do+not+construct+BigDecimal+objects+from+floating-point+literals
	public static BigDecimal num = new BigDecimal(1.0);
	public static Logger logger = new Logger();

	public static void main(String[] args) throws InterruptedException {

		//Start new Threads
		for(int i = 0; i < 1000; i++) {
			new Thread(new Application()).start();
		}

		Thread.sleep(2000);

		//Print when done running all threads
		System.out.println("Number Equals: " + num);
	}

	//Each thread adds to the running total, and pushes the result to the log
	@Override
	public void run() {
		//Violation 2
		//https://wiki.sei.cmu.edu/confluence/display/java/NUM07-J.+Do+not+attempt+comparisons+with+NaN
		if(Double.parseDouble(num.toString()) == Double.NaN) {
			logger.addLog("Invalid Number!");
		} else {
			logger.addLog(num.toString());
		}
	}
}