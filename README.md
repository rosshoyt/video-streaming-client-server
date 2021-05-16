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
