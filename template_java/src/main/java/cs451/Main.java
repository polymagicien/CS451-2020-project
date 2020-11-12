package cs451;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static Layer applicationLayer;
    private static String outputFile = "default.txt";
    private static int numBroadcasts = 1000;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        //write/flush output file if necessary
        System.out.println("Writing output.");
        try {
            FileWriter myWriter = new FileWriter(outputFile, false);
            myWriter.write(applicationLayer.waitFinishBroadcasting());
            myWriter.close();

            System.out.println("Successfully wrote to the file.");
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
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

            try {
                File myObj = new File(parser.config());
                Scanner myReader = new Scanner(myObj);
                numBroadcasts = Integer.valueOf(myReader.nextLine());
                myReader.close();
              } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }

        outputFile = parser.output();
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
        applicationLayer = new ApplicationLayer(parser.hosts(), me);

        for (int i = 1; i <= numBroadcasts; i++) {
            applicationLayer.send(null, ""+i);
        }
        String log = applicationLayer.waitFinishBroadcasting();

        System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
