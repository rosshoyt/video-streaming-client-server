import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioStream {
    FileInputStream fis; //audio file
    int audio_sample_nb; //current audio sample number

    public AudioStream(String filename) throws FileNotFoundException{
        fis = new FileInputStream(filename);
        audio_sample_nb = 0;
    }

    public int getnextchunk(byte[] chunk) throws IOException {
        int length = 0;
        String length_string;
        byte[] chunk_length = new byte[5];


        fis.read(chunk_length, 0, 5);

        length_string = new String(chunk_length);
        length = Integer.parseInt(length_string);

        return (fis.read(chunk,0, length));
    }

}
