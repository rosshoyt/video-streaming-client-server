### Video Streaming Client Server in Java
### CPSC 5510 – Computer Networks - SP2021
### Group: Ross Hoyt and Hiba Salama
#
### Project 2 - Part A Submission (May 1st, 2021)
#### RTP Packet Loss Rate: 0.0%
#### Video Data Streaming Rate:  77 Kilobytes/second
#
### Directions:
#### 1.	Start server -
* #### Compile with:  javac Server.java
* #### Run with: java Server *server-port-number*
* #### (Example)  java Server 1025
#### 2.	Start client -
* #### Compile: javac Client.java
* #### Run: java Client *server-host-address server-port-number video-file username password*
* #### (Example) java Client 127.0.0.1 1025 movie.Mjpeg johndoe asdf1234
* ##### *Note: Username and password are required to be "johndoe" and "asdf1234" for authorization*
#### 3.	Press ‘Setup’ button in Client
#### 4.	Press ‘Play’ button in Client
