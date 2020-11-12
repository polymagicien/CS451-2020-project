package cs451;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void talk(Layer layer, Host destHost) {
        Scanner scanner = new Scanner(System.in);
        String data;
        while ( !(data = scanner.nextLine()).equals("") ) {
            layer.send(null, data);
        }
        scanner.close();
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");

        System.out.println("My id is " + parser.myId() + ".");
        System.out.println("List of hosts is:");
        for (Host host: parser.hosts()) {
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
        }

        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Signal: " + parser.signalIp() + ":" + parser.signalPort());
        System.out.println("Output: " + parser.output());
        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
        }


        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

        System.out.println("Waiting for all processes for finish initialization");
            coordinator.waitOnBarrier();

        System.out.println("Broadcasting messages...");
        // Retrieve own port for initialisation
        Host me = null;
        for ( Host host : parser.hosts()) {
            if (host.getId() == parser.myId())
                me = host;
        }
        HostList.populate(parser.hosts());
        GroundLayer.start(me.getPort());
        Layer appli = new ApplicationLayer(parser.hosts(), me);

        // if (parser.myId() == 1){
        //     BufferedReader reader;
        //     try {
        //         reader = new BufferedReader(new FileReader("sent.txt"));
        //         String line = reader.readLine();
        //         while (line != null) {
        //             appli.send(null, line);
        //             line = reader.readLine();
        //         }
        //         reader.close();
        //     } catch (IOException e) {
        //         System.err.println("Unable to open file");
        //     }
        // }
        // if (parser.myId() == 1){
            for (int i = 0; i < 1000; i++) {
                appli.send(null, ""+i);
            }
        // }

        System.out.println("Signaling end of broadcasting messages");
            coordinator.finishedBroadcasting();

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
