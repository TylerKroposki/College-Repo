import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Queue;

public class Logger {

    private Queue<String> queue;

    public Logger() {
         queue = new LinkedList<String>();
    }

    public void addLog(String s) {
        if(queue.size() < 5) {
            queue.add(s);
        } else {
            pushLogs();
        }
    }

    //Push logs in Queue to file
    private void pushLogs() {
        try {
            PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream("output.txt")));
            for(String s: queue) {
                out.println(s);
                queue.remove(s);
            }
            //Violation 3
            //https://wiki.sei.cmu.edu/confluence/display/java/FIO14-J.+Perform+proper+cleanup+at+program+termination
            Runtime.getRuntime().exit(1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}