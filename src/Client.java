/**
 * Class which represents a streaming video client
 * usage: java Client [Server hostname] [Server RTSP listening port] [Video file requested]
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class Client{

    //GUI
    //----
    JFrame f = new JFrame("Client");
    JButton setupButton = new JButton("Setup");
    JButton playButton = new JButton("Play");
    JButton pauseButton = new JButton("Pause");
    JButton tearButton = new JButton("Teardown");
    JPanel mainPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JLabel iconLabel = new JLabel();
    ImageIcon icon;

    //RTP variables:
    //----------------
    DatagramPacket rcvdp_video; //UDP packet received from the server
    DatagramPacket rcvdp_audio; //UDP packet received from the server
    DatagramSocket RTPsocket_video; //socket to be used to send and receive UDP packets
    DatagramSocket RTPsocket_audio; //socket to be used to send and receive UDP audio packets
    static int RTP_RCV_PORT_VIDEO = 25000; //port where the client will receive the RTP video packets
    static int RTP_RCV_PORT_AUDIO = 25001; //port where the client will receive the RTP audio packets


    Timer timer; //timer used to receive data from the UDP socket
    byte[] buf_video; //buffer used to store video data received from the server
    byte[] buf_audio; //buffer used to store audio data received from the server

    //RTSP variables
    //----------------
    //rtsp states
    final static int INIT = 0;
    final static int READY = 1;
    final static int PLAYING = 2;
    static int state; //RTSP state == INIT or READY or PLAYING
    Socket RTSPsocket; //socket used to send/receive RTSP messages
    //input and output stream filters
    static BufferedReader RTSPBufferedReader;
    static BufferedWriter RTSPBufferedWriter;
    static String VideoFileName; //video file to request to the server
    int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session
    int RTSPid = 0; //ID of the RTSP session (given by the RTSP Server)

    final static String CRLF = "\r\n";

    //Authentication variables
    String Username, Password;

    //Video constants:
    //------------------
    static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video
    static int PAYLOAD_TYPE = 11; //RTP payload type for 16 bit 44.khz mono WAV file

    // Socket info
    //----
    InetAddress ServerIPAddr;
    String ServerHost;

    /**
     * Constructs the video streaming client
     */
    public Client() {
        //build GUI
        //Frame
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        //Buttons
        buttonPanel.setLayout(new GridLayout(1,0));
        buttonPanel.add(setupButton);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(tearButton);
        setupButton.addActionListener(new setupButtonListener());
        playButton.addActionListener(new playButtonListener());
        pauseButton.addActionListener(new pauseButtonListener());
        tearButton.addActionListener(new tearButtonListener());

        //Image display label
        iconLabel.setIcon(null);

        //frame layout
        mainPanel.setLayout(null);
        mainPanel.add(iconLabel);
        mainPanel.add(buttonPanel);
        iconLabel.setBounds(0,0,380,280);
        buttonPanel.setBounds(0,280,380,50);

        f.getContentPane().add(mainPanel, BorderLayout.CENTER);
        f.setSize(new Dimension(390,370));
        f.setVisible(true);

        //init timer
        //--------------------------
        timer = new Timer(20, new timerListener());
        timer.setInitialDelay(0);
        timer.setCoalesce(true);

        //allocate enough memory for the buffer used to receive data from the server
        buf_video = new byte[15000];
        buf_audio = new byte[15000];
    }

    /**
     * Method which runs the Client program
     * @param argv Host IP, Host Port, Requested video file.
     * Example: 127.0.0.1 1025 movie.Mjpeg
     * @throws Exception
     */
    public static void main(String argv[]) throws Exception
    {
        //Create a Client object
        Client theClient = new Client();

        //get server RTSP port and IP address from the command line
        //------------------
        int RTSP_server_port = Integer.parseInt(argv[1]);
        theClient.ServerHost = argv[0];
        theClient.ServerIPAddr = InetAddress.getByName(theClient.ServerHost);

        //get video filename to request:
        VideoFileName = argv[2];

        //get username and password to setup the stream:
        theClient.Username = argv[3];
        theClient.Password = argv[4];

        //Establish a TCP connection with the server to exchange RTSP messages
        //------------------
        theClient.RTSPsocket = new Socket(theClient.ServerIPAddr, RTSP_server_port);

        //Set input and output stream filters:
        RTSPBufferedReader = new BufferedReader(new InputStreamReader(theClient.RTSPsocket.getInputStream()) );
        RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theClient.RTSPsocket.getOutputStream()) );

        //init RTSP state:
        state = INIT;
    }


    //------------------------------------
    //Handler for buttons
    //------------------------------------

    //------------------------------------
    //Handler for Setup button
    //-----------------------
    class setupButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            System.out.println("Setup Button pressed !");
            if (state == INIT)
            {
                //Init non-blocking RTPsocket that will be used to receive data
                try{
                    //construct a new DatagramSocket to receive RTP packets from the server, on port RTP_RCV_PORT
                    RTPsocket_video = new DatagramSocket(RTP_RCV_PORT_VIDEO);
                    //construct a new DatagramSocket to receive RTP packets from the server, on port RTP_RCV_PORT
                    RTPsocket_audio = new DatagramSocket(RTP_RCV_PORT_AUDIO);
                    //set TimeOut value of the socket to 5msec.
                    RTPsocket_video.setSoTimeout(5);
                }
                catch (SocketException se)
                {
                    System.out.println("Socket exception: "+se);
                    System.exit(0);
                }

                //init RTSP sequence number
                RTSPSeqNb = 1;

                //Send SETUP message to the server
                send_RTSP_request("SETUP");

                //Wait for the response
                if (parse_server_response() != 200)
                    System.out.println("Invalid Server Response");
                else
                {
                    //change RTSP state and print new state
                    state = READY;
                    System.out.println("New RTSP state: READY");
                }
            }
            //else if state != INIT then do nothing
        }
    }

    //-----------------------
    //Handler for Play button
    //-----------------------
    class playButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            System.out.println("Play Button pressed!");
            if (state == READY)
            {
                //increase RTSP sequence number
                ++RTSPSeqNb;
                //Send PLAY message to the server
                send_RTSP_request("PLAY");
                //Wait for the response
                if (parse_server_response() != 200)
                    System.out.println("Invalid Server Response");
                else
                {
                    //change RTSP state and print out new state
                    state = PLAYING;
                    System.out.println("New RTSP state: PLAYING");
                    //start the timer
                    timer.start();
                }
            }//else if state != READY then do nothing
        }
    }


    //Handler for Pause button
    //-----------------------
    class pauseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){

            System.out.println("Pause Button pressed !");

            if (state == PLAYING)
            {
                //increase RTSP sequence number
                ++RTSPSeqNb;

                //Send PAUSE message to the server
                send_RTSP_request("PAUSE");

                //Wait for the response
                if (parse_server_response() != 200)
                    System.out.println("Invalid Server Response");
                else
                {
                    //change RTSP state and print out new state
                    state = READY;
                    System.out.println("New RTSP state: PAUSE");

                    //stop the timer
                    timer.stop();
                }
            }
            //else if state != PLAYING then do nothing
        }
    }

    //Handler for Teardown button
    //-----------------------
    class tearButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){

            System.out.println("Teardown Button pressed !");
            teardown();

        }
    }

    private void teardown(){
        System.out.println("Starting teardown and exiting the client program...");
        //increase RTSP sequence number
        ++RTSPSeqNb;

        //Send TEARDOWN message to the server
        send_RTSP_request("TEARDOWN");

        //Wait for the response
        if (parse_server_response() != 200)
            System.out.println("Invalid Server Response");
        else
        {
            //change RTSP state and print out new state
            state = INIT;
            System.out.println("New RTSP state: INIT");

            //stop the timer
            timer.stop();

            //exit
            System.exit(0);
        }
    }

    //------------------------------------
    //Handler for timer
    //------------------------------------

    class timerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //Construct a DatagramPacket to receive data from the UDP socket
            rcvdp_video = new DatagramPacket(buf_video, buf_video.length);
            rcvdp_audio = new DatagramPacket(buf_audio, buf_audio.length);
            try{
                //receive the DP from the socket:
                RTPsocket_video.receive(rcvdp_video);
                //create an RTPpacket object from the DP
                RTPpacket rtp_packet = new RTPpacket(rcvdp_video.getData(), rcvdp_video.getLength());
                //print important header fields of the RTP packet received:
                System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());

                //print header bitstream:
                rtp_packet.printheader();

                //get the payload bitstream from the RTPpacket object
                int payload_length = rtp_packet.getpayload_length();
                byte [] payload = new byte[payload_length];
                rtp_packet.getpayload(payload);

                //get an Image object from the payload bitstream
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Image image = toolkit.createImage(payload, 0, payload_length);

                //display the image as an ImageIcon object
                icon = new ImageIcon(image);
                iconLabel.setIcon(icon);
            }
            catch (InterruptedIOException iioe){
                //System.out.println("Nothing to read");
            }
            catch (IOException ioe) {
                System.out.println("Exception caught: "+ioe);
            }
        }
    }

    //------------------------------------
    //Parse Server Response
    //------------------------------------
    private int parse_server_response()
    {
        int reply_code = 0;
        try{
            //parse status line and extract the reply_code:
            String StatusLine = RTSPBufferedReader.readLine();
            System.out.println("RTSP Client - Received from Server:");
            System.out.println(StatusLine);

            StringTokenizer tokens = new StringTokenizer(StatusLine);
            tokens.nextToken(); //skip over the RTSP version
            reply_code = Integer.parseInt(tokens.nextToken());

            //if reply code is OK get and print the 2 other lines
            if (reply_code == 200)
            {
                String SeqNumLine = RTSPBufferedReader.readLine();
                System.out.println(SeqNumLine);

                String SessionLine = RTSPBufferedReader.readLine();
                System.out.println(SessionLine);

                //if state == INIT gets the Session Id from the SessionLine
                tokens = new StringTokenizer(SessionLine);
                tokens.nextToken(); //skip over the Session:
                RTSPid = Integer.parseInt(tokens.nextToken());
                System.out.println("(Debug) RTSPid value = " + RTSPid);
            }
            else if(reply_code == 401)
            {
                System.out.println("Server returned error 401 - not authorized");
                exit();
            }
            else if(reply_code == 404)
            {
                System.out.println("Server returned error 404 - file was not found");
                exit();
            }
        }
        catch(Exception ex)
        {
            System.out.println("Exception caught: "+ex);
            System.exit(0);
        }

        return(reply_code);
    }

    //------------------------------------
    //Send RTSP Request
    //------------------------------------
    private void send_RTSP_request(String request_type)
    {
        try{
            //Use the RTSPBufferedWriter to write to the RTSP socket

            //write the request line:
            RTSPBufferedWriter.write(request_type + " " + "rtsp://" +
                    Username + ":" + Password + "@" +  ServerIPAddr + "/" + VideoFileName + " RTSP/1.0" + CRLF);

            //write the CSeq line:
            RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);

            //check if request_type is equal to "SETUP" and in this case write the Transport: line
            // advertising to the server the port used to receive the RTP packets RTP_RCV_PORT
            if(request_type.equals("SETUP")){
                // TODO pass audio port to server? or assume it to be RTP_RCV_PORT_VIDEO + 1
                RTSPBufferedWriter.write("Transport: RTP/UDP; client_port= " + RTP_RCV_PORT_VIDEO + CRLF);
            } else {
                //otherwise, write the Session line from the RTSPid field
                RTSPBufferedWriter.write("Session: " + RTSPid + CRLF);
            }

            RTSPBufferedWriter.flush();
        }
        catch(Exception ex)
        {
            System.out.println("Exception caught: "+ex + "\nExiting");
            System.exit(0);
        }
    }

    /**
     * Method which stops client timer, closes all socket connections, and exits the program
     */
    private void exit(){
        System.out.println("Exiting the client program");
        //stop timer
        timer.stop();
        try{
            // close all socket connections
            if(RTSPsocket != null)
                RTSPsocket.close();
            if(RTPsocket_video != null)
                RTPsocket_video.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        System.exit(0);
    }
}