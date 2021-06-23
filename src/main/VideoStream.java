package main;

import java.io.*;

public class VideoStream {

    FileInputStream fis; //video file
    int frame_nb; //current frame nb

    //-----------------------------------
    //constructor
    //-----------------------------------
    public VideoStream(String filename) throws FileNotFoundException{

        //init variables
        fis = new FileInputStream(filename);
        frame_nb = 0;
    }

    //-----------------------------------
    // getnextframe
    //returns the next frame as an array of byte and the size of the frame
    //-----------------------------------
    public int getnextframe(byte[] frame) throws Exception
    {
        int length = 0;
        String length_string;
        byte[] frame_length = new byte[5];

        //read current frame length
        fis.read(frame_length,0,5);

        //transform frame_length to integer
        length_string = new String(frame_length);
        length = Integer.parseInt(length_string);

        return(fis.read(frame,0,length));
    }

    /**
     * Gets the intended fps rate of the video file or source material
     * @return double video frames per second rate
     */
    public double getsourcevideofps(){
        return  this.frames_per_second;
    }
    private double frames_per_second = 24; // the FPS of the source video file (default = 24)

}