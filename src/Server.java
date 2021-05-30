/**
 * Class which represents a streaming video server
 * usage: java Server [RTSP listening port]
 */
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class Server extends JFrame implements ActionListener, Runnable {

    public void run(){
        //show GUI:
        pack();
        setVisible(true);

        //Initiate TCP connection with the client for the RTSP session
        ServerSocket listenSocket = null;
        try {
            listenSocket = new ServerSocket(RTSP_socket_port);
            RTSPsocket = listenSocket.accept();
            listenSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //Get Client IP address
        ClientIPAddr = RTSPsocket.getInetAddress();

        //Initiate RTSPstate
        Server.state = Server.INIT;

        //Set input and output stream filters:
        try {
            Server.RTSPBufferedReader = new BufferedReader(new InputStreamReader(RTSPsocket.getInputStream()));
            Server.RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(RTSPsocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        //loop to handle RTSP requests
        while (true) {
            // Wait for initial SETUP request from client
            int request_type = parse_RTSP_request(); //blocking

            boolean authenticated = authenticate();

            if (!authenticated) {
                System.out.println("Authentication failed");
                send_401_response();
            } else if (request_type == Server.SETUP) {
                try {
                    // Check the video file exists (can throw a FileNotFoundException)
                    video = new VideoStream(Server.VideoFileName);
                    // Load associated audio stream
                    audio = new AudioStream("AudioStreamTest1_16bit_44100_Mono.wav", 100);
                    // File was found, so we'll change server to the READY state
                    Server.state = Server.READY;
                    System.out.println("New RTSP state: READY");
                    //Send response
                    send_RTSP_response();

                    try{
                        //init RTP video socket
                        RTPsocket = new DatagramSocket();
                        //init RTP audio socket
                        RTPsocketaudio = new DatagramSocket();
                    } catch(SocketException se){
                        se.printStackTrace();
                        System.exit(-1);
                    }

                } catch (FileNotFoundException e) {
                    send_404_response();
                }
            } else if ((request_type == Server.PLAY) && (Server.state == Server.READY)) {
                //send back response
                send_RTSP_response();
                //start timer
                timer.start();
                //update state
                Server.state = Server.PLAYING;
                System.out.println("New RTSP state: PLAYING");
            } else if ((request_type == Server.PAUSE) && (Server.state == Server.PLAYING)) {
                //send back response
                send_RTSP_response();
                //stop timer
                timer.stop();
                //update state
                Server.state = Server.READY;
                System.out.println("New RTSP state: READY");
            } else if (request_type == Server.TEARDOWN) {
                //send back response
                send_RTSP_response();

                exit();

            } else if(request_type == Server.UNIMPLEMENTED_MESSAGE_TYPE){
                System.out.println("Client Response Code " + request_type + " not Implemented  - sending Response Code 501");
                send_501_response();
            }
        }
    }

    //RTP variables:
    //----------------
    DatagramSocket RTPsocket; //socket to be used to send and receive UDP packets
    DatagramPacket senddp; //UDP packet containing the video frames

    //RTP audio variables:
    //----------------
    DatagramSocket RTPsocketaudio;
    DatagramPacket senddpaudio;

    InetAddress ClientIPAddr; //Client IP address
    int RTSP_socket_port = 0; // given by Server Application
    int RTP_dest_port = 0; //destination port for RTP packets  (given by the RTSP Client)
    int RTP_dest_port_audio = 1026; // TODO let RTSP Client set port when setting up the audio stream

    //GUI:
    //----------------
    JLabel label;

    //Video variables:
    //----------------
    int imagenb = 0; //image nb of the image currently transmitted
    VideoStream video; //VideoStream object used to access video frames
    static int MJPEG_TYPE = 26; //RTP payload type for MJPEG video
    static int FRAME_PERIOD = 100; //Frame period of the video to stream, in ms
    static int VIDEO_LENGTH = 500; //length of the video in frames

    Timer timer; //timer used to send the images at the video frame rate
    byte[] buf; //buffer used to store the images to send to the client

    //Audio variables:
    //----------------
    int sanmplenb = 0; //image nb of the image currently transmitted
    AudioStream audio; //VideoStream object used to access video frames
    static int AUDIO_TYPE = 10; //RTP payload type for Linear PCM 16-bit Stereo audio (1411.2 kbit/s, uncompressed)
    //static int AUDIO_CHUNK_PERIOD = 25; //Frame period of the video to stream, in ms
    //static int AUDIO_LENGTH = 500; //length of the video in frames

    //Timer timeraudio; //timer used to send the images at the video frame rate
    byte[] bufaudio; //buffer used to store the images to send to the client

    //RTSP variables
    //----------------
    //rtsp states
    final static int INIT = 0;
    final static int READY = 1;
    final static int PLAYING = 2;
    //rtsp message types
    final static int SETUP = 3;
    final static int PLAY = 4;
    final static int PAUSE = 5;
    final static int TEARDOWN = 6;
    // flag that signifies the message type is not supported by the server
    final static int UNIMPLEMENTED_MESSAGE_TYPE = -1;

    static int state; //RTSP Server state == INIT or READY or PLAY
    Socket RTSPsocket; //socket used to send/receive RTSP messages
    //input and output stream filters
    static BufferedReader RTSPBufferedReader;
    static BufferedWriter RTSPBufferedWriter;
    static String VideoFileName; //video file requested from the client
    static int RTSP_ID = 123456; //ID of the RTSP session
    int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session

    final static String CRLF = "\r\n";

    // Authentication variables
    //----------------
    // Username provided in last RTSP request
    String ClientUsername;
    // Password provided in last RTSP request
    String ClientPassword;

    /**
     * Constructs the video streaming server
     */
    public Server(int rtspSocketPort){
        //init Frame
        super("Server");

        //init Timer
        timer = new Timer(FRAME_PERIOD, this);
        timer.setInitialDelay(0);
        timer.setCoalesce(true);

        //allocate memory for the sending buffer
        buf = new byte[15000];
        bufaudio = new byte[30000]; // TODO make audio buffer smaller by implementing shorter Audio timer

        //Handler to close the main window
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                //stop the timer and exit
                timer.stop();
                System.exit(0);
            }});

        //GUI:
        label = new JLabel("Send frame #        ", JLabel.CENTER);
        getContentPane().add(label, BorderLayout.CENTER);
    }

    //------------------------
    //Handler for timer
    //------------------------
    public void actionPerformed(ActionEvent e) {

        //if the current image nb is less than the length of the video
        if (imagenb < VIDEO_LENGTH)
        {
            //update current imagenb
            imagenb++;

            try {
                //get next frame to send from the video, as well as its size
                int image_length = video.getnextframe(buf);

                //Builds an RTPpacket object containing the frame
                RTPpacket rtp_packet = new RTPpacket(MJPEG_TYPE, imagenb, imagenb*FRAME_PERIOD, buf, image_length);

                //get to total length of the full rtp packet to send
                int packet_length = rtp_packet.getlength();

                //retrieve the packet bitstream and store it in an array of bytes
                byte[] packet_bits = new byte[packet_length];
                rtp_packet.getpacket(packet_bits);

                //send the packet as a DatagramPacket over the UDP socket
                senddp = new DatagramPacket(packet_bits, packet_length, ClientIPAddr, RTP_dest_port);
                RTPsocket.send(senddp);

                System.out.println("Sent video frame #"+imagenb);
                //print the header bitstream
                rtp_packet.printheader();

                // play the audio buffer to test the audio stream
                try {
                    //get next frame to send from the video, as well as its size
                    int audio_length = audio.getnextframe(bufaudio);

                    //Builds an RTPpacket object containing the audio frame
                    RTPpacket rtp_packet_audio = new RTPpacket(AUDIO_TYPE, imagenb, imagenb*FRAME_PERIOD, bufaudio, audio_length);

                    //get total length of the full rtp packet to send
                    int packet_length_audio = rtp_packet_audio.getlength();

                    //retrieve the packet bitstream and store it in an array of bytes
                    byte[] packet_bits_audio = new byte[packet_length_audio];
                    rtp_packet_audio.getpacket(packet_bits_audio);

                    //send the packet as a DatagramPacket over the UDP socket
                    senddp = new DatagramPacket(packet_bits_audio, packet_length_audio, ClientIPAddr, RTP_dest_port_audio);
                    RTPsocket.send(senddp);

                    System.out.println("Sent audio frame #"+imagenb);
                    //print the header bitstream
                    rtp_packet_audio.printheader();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                //update GUI
                label.setText("Send frame #" + imagenb);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                System.exit(0);
            }
        }
        else
        {
            //if we have reached the end of the video file, stop the timer
            timer.stop();
        }
    }
    //------------------------------------
    //Parse RTSP Request
    //exits program if request is null (Client has closed connection)
    //returns -1 if the request type is not implemented
    //------------------------------------
    private int parse_RTSP_request()
    {
        int request_type = UNIMPLEMENTED_MESSAGE_TYPE;
        try{
            //parse request line and extract the request_type:
            String RequestLine = RTSPBufferedReader.readLine();
            System.out.println("RTSP Server - Received from Client:");

            if(RequestLine == null){
                System.out.println("Null Request - The Client has closed the connection");
                exit();
            } else {
                System.out.println(RequestLine);
                StringTokenizer tokens = new StringTokenizer(RequestLine);
                String request_type_string = tokens.nextToken();

                //convert to request_type structure:
                if ((new String(request_type_string)).compareTo("SETUP") == 0)
                    request_type = SETUP;
                else if ((new String(request_type_string)).compareTo("PLAY") == 0)
                    request_type = PLAY;
                else if ((new String(request_type_string)).compareTo("PAUSE") == 0)
                    request_type = PAUSE;
                else if ((new String(request_type_string)).compareTo("TEARDOWN") == 0)
                    request_type = TEARDOWN;

                if (request_type == SETUP) {
                    // parse the RTSP url from RequestLine
                    parse_RTSP_URL(tokens.nextToken());
                }

                //parse the SeqNumLine and extract CSeq field
                String SeqNumLine = RTSPBufferedReader.readLine();
                System.out.println(SeqNumLine);
                tokens = new StringTokenizer(SeqNumLine);
                tokens.nextToken(); // skips "CSeq: " text
                RTSPSeqNb = Integer.parseInt(tokens.nextToken());
                System.out.println("(Debug) RTSPSeqNb = " + RTSPSeqNb);

                //get LastLine
                String LastLine = RTSPBufferedReader.readLine();
                System.out.println(LastLine);

                if (request_type == SETUP) {
                    //extract RTP_dest_port from LastLine
                    tokens = new StringTokenizer(LastLine);
                    for (int i = 0; i < 3; i++)
                        tokens.nextToken();
                    RTP_dest_port = Integer.parseInt(tokens.nextToken());
                    RTP_dest_port_audio = RTP_dest_port + 1;
                }
                //else LastLine will be the SessionId line ... do not check for now.
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            System.exit(0);
        }
        return(request_type);
    }

    //------------------------------------
    //Parse RTSP URl and sets Server data fields
    //TODO handle malformed or differently formatted RTSP urls
    //TODO delete debug msgs
    //------------------------------------
    private void parse_RTSP_URL(String rtspURL){
        // check if URL starts correctly
        String rtspProtocolIDString = "rtsp://";
        if(rtspURL.indexOf(rtspProtocolIDString) == -1){
            throw new IllegalArgumentException("URL must start with " + rtspProtocolIDString);
        }

        // extract the username and password
        // TODO could use regex
        // TODO could add encryption/decryption
        int firstColonIndex = rtspURL.indexOf(":", rtspProtocolIDString.length());
        ClientUsername = rtspURL.substring(rtspProtocolIDString.length(),firstColonIndex);
        int atSignIndex = rtspURL.indexOf("@");
        ClientPassword = rtspURL.substring(firstColonIndex+1,atSignIndex);

        // extract the video file name TODO handle different filename types and directories
        VideoFileName = rtspURL.substring(rtspURL.lastIndexOf("/") + 1);
    }

    //------------------------------------
    //Send RTSP Response
    //------------------------------------
    private void send_RTSP_response()
    {
        try{
            RTSPBufferedWriter.write(RTSPutils.get_RTSP_response(200, RTSPSeqNb, RTSP_ID));
            RTSPBufferedWriter.flush();
            //System.out.println("RTSP Server - Sent response to Client.");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    //------------------------------------
    //Send RTSP Response with 404 code to indicate the video file was not found
    //------------------------------------
    private void send_404_response() {
        try{
            RTSPBufferedWriter.write(RTSPutils.get_RTSP_response(401, RTSPSeqNb, RTSP_ID));
            RTSPBufferedWriter.flush();
            System.out.println("RTSP Server - Sent 404 response to Client (video file not found).");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Sends a 401 not authorized response to client
     */
    private void send_401_response() {
        try{
            RTSPBufferedWriter.write(RTSPutils.get_RTSP_response(401, RTSPSeqNb, RTSP_ID));
            RTSPBufferedWriter.flush();
            System.out.println("Sent 401 Not Authorized Response to client");
        }catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    //------------------------------------
    //Send RTSP Response with 501 code to indicate the server hasn't implemented the request
    //------------------------------------
    private void send_501_response() {
        try{
            RTSPBufferedWriter.write(RTSPutils.get_RTSP_response(501, RTSPSeqNb, RTSP_ID));
            RTSPBufferedWriter.flush();
            System.out.println("Sent 501 Not Implemented Response to client");
        }catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    //------------------------------------
    //Verifies the username and password combo
    //returns true if authentication successful
    //TODO extend to support different username/pw combos defined externally, add encryption/decryption
    //------------------------------------
    private boolean authenticate() {
        return ClientUsername.equals("johndoe") && ClientPassword.equals("asdf1234");
    }

    /**
     * Method which stops server sending video, closes all socket connections, and exits the program
     */
    private void exit(){
        //stop timer
        timer.stop();
        try{
            // close all socket connections
            if(RTSPsocket != null)
                RTSPsocket.close();
            if(RTPsocket != null)
                RTPsocket.close();
            if(RTPsocketaudio != null)
                RTPsocketaudio.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        System.exit(0);
    }
}