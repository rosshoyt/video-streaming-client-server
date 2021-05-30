import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class ServerApplication {

    /**
     * Main method which runs the server program
     * @param argv RTSP listening port
     * example: 1025
     * @throws Exception
     */
    public static void main(String argv[]) {
        //get RTSP socket port from the command line
        int RTSPport = Integer.parseInt(argv[0]);

        //create a Server object
        Server theServer = new Server(RTSPport);

        theServer.run();

    }
}
