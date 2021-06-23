package main;

public class RTSPutils {
    // Line delimiter used in RTSP messages TODO make public
    private final static String CRLF = "\r\n";


    /**
     * Method that creates a custom RTSP Response from a server to client
     * Supported RTSP Status Codes: 200 OK, 404 file not found, 401 not authorized, 501 Not Implemented
     * @param statusCode int RTSP status code
     * @param rtspSeqNum int sequence number
     * @param rtspID int id
     * @return String RTSP response with  the requested params
     */
    public static String get_RTSP_response(int statusCode, int rtspSeqNum, int rtspID){
        StringBuffer sb = new StringBuffer();
        // Write status line
        sb.append("RTSP/1.0 ");

        if(statusCode == 200){
            sb.append(statusCode + " OK" + CRLF);
        }else if(statusCode == 401 || statusCode == 404 || statusCode ==  501){
            sb.append(statusCode + " ERR" + CRLF);
        }

        // set sequence number and session id lines
        sb.append("CSeq: "+rtspSeqNum+CRLF);
        sb.append("Session: "+rtspID+CRLF);
        return sb.toString();
    }

}
