### Video Streaming Client Server in Java
### CPSC 5510 – Computer Networks - SP2021
### Group: Ross Hoyt and Hiba Salama
#
### Project 2 - Part B Submission (May 15th, 2021)
#
### Directions:
#### 1.	Start server -
* #### Compile with:  javac src/Server.java
* #### Run with: java Server *server-port-number*
* #### *Example:*  java Server 1025
#### 2.	Start client with username and password-
* #### Compile: javac src/Client.java
* #### Run: java Client *server-host-address server-port-number video-file username password*
* #### *Example:* java Client 127.0.0.1 1025 movie.Mjpeg johndoe asdf1234
* ##### *Note: Username and password are required to be "johndoe" and "asdf1234" for successfully authenticating and streaming video from Server*
#### 3.	Press ‘Setup’ button in Client
#### 4.	Press ‘Play’ button in Client
#### 5. (Optional) Press 'Pause' button in Client
#### 6. Press 'Teardown' button in Client
#
### Extra Credit
#### 1. Readability:
* #### Simplified Server.main() method control flow
* #### To reduce code duplication, added static method to generate Server RTSP responses in RTSPutils.java 
#### 2. Error checking:
* #### Server responds with 404 File Not Found Error Code when requested media file is not found on server
* #### Server responds with 401 Not Authorized Error Code when provided username or password is incorrect
* #### Server responds with 501 Not Implemented Error Code when Client sends a RTSP request that isn't supported ("OPTIONS", "DESCRIBE", "RECORD", etc)
#### 3. Resource management:
* #### When a Client closes after error occurs on SETUP (404, 401), the Server closes Sockets and exits
#### 4. Comments: 
* #### Added Javadoc comments for all added classes, fields, and methods
* #### Added in-line comments to clarify intended purpose of individual lines of code
