using System;
using System.Net;
using System.Net.Sockets;

class RdpClient {
    static void Main() {
        try {
            // Connect to the server on the RDP port (3389)
            IPAddress ipAddress = IPAddress.Parse("127.0.0.1");
            int port = 3389;

            TcpClient tcpClient = new TcpClient(ipAddress.ToString(), port);

            // Open the RDP connection
            NetworkStream stream = tcpClient.GetStream();
            byte[] buffer = new byte[1024];
            stream.Write(buffer, 0, buffer.Length);

            // Receive the RDP connection response
            byte[] responseBuffer = new byte[1024];
            stream.Read(responseBuffer, 0, responseBuffer.Length);

            // Close the connection
            stream.Close();
            tcpClient.Close();
        } catch (Exception e) {
            Console.WriteLine("Exception: {0}", e);
        }
    }
}

// We start by creating a TcpClient object and connecting to the server on the RDP port (3389) with the Connect() method.
// We then call the GetStream() method on the TcpClient object to get a NetworkStream object to read and write data to the server.
// We send a buffer of data to the server using the Write() method on the NetworkStream.
// We receive the response from the server using the Read() method on the NetworkStream.
// Finally, we close the connection with the Close() method on the NetworkStream and TcpClient objects.