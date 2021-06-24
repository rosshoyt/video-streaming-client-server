package main;

/**
 * Class containing data constants used by the Server and Client in the video and audio streams
 */
public class StreamConstants {

    /**
     * Length of time (milliseconds) the server waits before sending next video packet.
     */
    final static int TRANS_SPEED_MS_VIDEO = 75;

    /**
     * Length of time (milliseconds) the server waits before sending next audio packet.
     */
    final static int TRANS_SPEED_MS_AUDIO = 25;
}
