import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
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

    //AudioFileFormat audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.WAVE,);
    /**
     * Creates an audio stream of a specified folder
     * @param filename
     * @param timerRateMS
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    public AudioStream(String filename, double timerRateMS) throws IOException, UnsupportedAudioFileException {
        this.timerRateMS = timerRateMS;
        file = new File(filename);
        try{
            ais = AudioSystem.getAudioInputStream(file);
            isValid = true;
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    /**
     * Reads the next frame of audio data from the audio stream into a byte array
     * @param frame the byte array to fill with audio data
     * @return
     * @throws IOException
     */
    public int getnextframe(byte[] frame) throws IOException {

        int length = 0;
        // TODO set length
        //ais.read(chunk_length, 0, 5);

        return (ais.read(frame,0, length));
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

}
