public class RTSPutils {
    // Line delimiter used in RTSP messages TODO make public
    private final static String CRLF = "\r\n";

    /**
     * Method that creates a custom RTSP Response from a server to client
     * TODO support additional RTSP status codes
     * @param statusCode int RTSP status code (404 = file not found, 401 = unauthorized, etc)
     * @param rtspSeqNum int sequence number
     * @param rtspID int id
     * @return String RTSP response with the requested params
     */
    public static String get_RTSP_response(int statusCode, int rtspSeqNum, int rtspID){
        StringBuffer sb = new StringBuffer();
        // Write status line
        sb.append("RTSP/1.0 ");
        switch (statusCode){
            case 401:
                sb.append(statusCode + " ERR" + CRLF);
                break;
        }
        // set sequence number and session id lines
        sb.append("CSeq: "+rtspSeqNum+CRLF);
        sb.append("Session: "+rtspID+CRLF);
        return sb.toString();
    }

}
