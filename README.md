# COMPNETPJ3
# Programming Assignment 3
## UDP File Transfer using Go-Back-N Sliding Window

This project implements a reliable file transfer service on top of UDP using the Go-Back-N protocol with a window size of 4. The program has a client and a server. The client requests a file from the server, and the server sends the file in data packets followed by an EOT packet. The client acknowledges packets and reconstructs the file contents. The project also supports simulated packet loss using the reliability number argument. The assignment specification requires a UDP-based client/server transfer, packet types for REQ, DAT, ACK, ERR, and EOT, plus support for reliability modes 0, 1, and 2. :contentReference[oaicite:0]{index=0}

## Project Structure

- `Client/`  
  Contains the client code that sends the file request, receives packets, sends ACKs, and rebuilds the file.

- `Server/`  
  Contains the server code that listens for requests, sends file data, handles retransmissions, and prints statistics.

- `Common/`  
  Contains shared classes such as packet format, packet types, settings, and logging.

- `README.txt`  
  Sample text file used for transfer testing.

## How to Compile

Run this from the project root:

```bash
javac Common/*.java Client/*.java Server/*.java
