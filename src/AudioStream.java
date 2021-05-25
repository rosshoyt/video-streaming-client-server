import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * Class which allows an audio file to be packetized for audio streaming
 */
public class AudioStream {
    AudioInputStream ais; // input stream
    File file; // the underlying audio file

    int audio_sample_nb = 0; //current audio sample number

    private boolean isValid = false; // true only if no problems occured setting up the audio input stream

    double timerRateMS = -1; // time length (MS) that the user will be requesting audio frames at

    /**
     * The byte length of a frame of audio. Used to fill the buffer from the audio stream
     */
    int frameByteLength = -1;

    /**
     * Creates an audio stream of a specified folder
     * @param filename
     * @param timerRateMS
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public AudioStream(String filename, double timerRateMS) {
        this.timerRateMS = timerRateMS;
        file = new File(filename);
        try{
            ais = AudioSystem.getAudioInputStream(file);
            isValid = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        calculatePacketByteLength();
    }

    /**
     * TODO use values in audiostream.audioformat instead of hardcoded values
     */
    void calculatePacketByteLength(){
        // byteLength = ...
        int frameLengthMS = 100;
        int sampleRate = 44100;
        int bitDepthRate = 16;
        //ais.getFormat().
        int sampleRateMS = sampleRate / 1000;
        // calculate the number of samples per millisecond
        int frameSampleLengthMS = sampleRateMS * frameLengthMS;
        this.frameByteLength = frameSampleLengthMS * bitDepthRate / 4;
        System.out.println("Audio Stream frameByteLength = " + frameByteLength);
        System.out.println(ais.getFormat()  );
    }

    /**
     * Reads the next frame of audio data from the audio stream into a byte array
     * @param frame the byte array to fill with audio data
     * @return size of data in frame
     * @throws IOException
     */
    public int getnextframe(byte[] frame) throws IOException {
        return (ais.read(frame,0, frameByteLength));
    }


    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

}
