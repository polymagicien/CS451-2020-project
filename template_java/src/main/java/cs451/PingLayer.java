package cs451;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PingLayer {
    static private Set<Host> pingReceived = Collections.synchronizedSet(new HashSet<>()); // Updated with ping received in period
    static private HashMap<Host, TimerTask> hostToTask = new HashMap<>(); // Link host to task used to send ping to it
    static private Set<Host> correctProcesses = new HashSet<>(); // Correct Processes 
    static private Timer timer = new Timer();  // Send pings

    public static void start(List<Host> hosts) {
        // Initialize correctProcesses
        correctProcesses.addAll(hosts);

        for (Host host : hosts)
            System.out.println(host.getIp() + ":" + host.getPort());

        // Schedule ping sending
        for (Host host : correctProcesses) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    GroundLayer.send(host.getIp(), host.getPort(), Constants.PING);
                }
            };
            timer.scheduleAtFixedRate(task, 0, Constants.DELAY_PING);
            hostToTask.put(host, task);
        }


        Thread thread = new Thread(() -> {
            checkForCrash();
        });
        thread.start();
    }

    public static void checkForCrash() {
        while (true) {
            try {
                Thread.sleep(Constants.DELAY_FOR_CRASH);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // System.out.println("Rain Check");

            Set<Host> crashedProcesses = new HashSet<>();
            crashedProcesses.addAll(correctProcesses);
            crashedProcesses.removeAll(pingReceived);

            // System.out.println(crashedProcesses.size());
            for (Host host : crashedProcesses) 
                handleCrash(host);

            correctProcesses.removeAll(crashedProcesses);
            pingReceived.clear();
        }
    }


    public static void handlePing(String sourceHostname, int sourcePort){
        Host host = new Host();
        host.populate("-1", sourceHostname, String.valueOf(sourcePort));
        pingReceived.add(host);
        // System.out.println("Ping from " + sourceHostname + ":" + sourcePort);

    }

    public static void handleCrash(Host host){
        System.out.println("Crash report : " + host.getIp() + ":" + host.getPort());
        hostToTask.get(host).cancel();  // Stop sending ping to it
    }

    public static Set<Host> getCorrectProcesses(){
        return correctProcesses;
    }
    
}
