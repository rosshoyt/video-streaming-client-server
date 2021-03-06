package main;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * Test class that shows how to play audio using the SourceDataLine java class
 */
public class AudioTests {
    private int bytes;          // total number of bytes


    public static final  AudioFormat DEF_AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);

    public static void playBuffer(byte[] buf){
        System.out.println("Playing Audio buffer " + buf.length);
        SourceDataLine line = null;

        DataLine.Info info  = new DataLine.Info(SourceDataLine.class, DEF_AUDIO_FORMAT);

        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(DEF_AUDIO_FORMAT);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        line.start();
        line.write(buf, 0, buf.length);
        line.drain();
        line.close();
    }

    public static void play2() {
        int bytes, cursor, unsigned;
        try {
            FileInputStream s = new FileInputStream("AudioStreamTest1_16bit_44100_Mono.wav");
            BufferedInputStream b = new BufferedInputStream(s);
            byte[] data = new byte[128];
            b.skip(44);
            cursor = 0;
            while ((bytes = b.read(data)) > 0) {
                // do something
                for(int i=0; i<bytes; i++) {
                    unsigned = data[i] & 0xFF; // Java..
                    System.out.println(cursor + " " + unsigned);
                    cursor++;
                }
            }
            b.read(data);
            b.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
