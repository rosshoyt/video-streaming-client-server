package main;


public class ClientApplication {
   /**
    * Method which runs the Client program
    * @param argv Host IP, Host Port, Requested video file.
    * Example: 127.0.0.1 1025 movie.Mjpeg
    * @throws Exception
    */
   public static void main(String argv[])
   {
      //get server host and RTSP port from the command line
      //------------------
      String ServerHost = argv[0];
      int RTSP_server_port = Integer.parseInt(argv[1]);
      //get video filename to request
      String VideoFileName = argv[2];
      //get username and password
      String Username = argv[3];
      String Password = argv[4];

      //Create the Client
      Client theClient = new Client(ServerHost, RTSP_server_port, VideoFileName, Username, Password);

   }
}

