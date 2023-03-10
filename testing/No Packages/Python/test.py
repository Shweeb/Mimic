import socket
import subprocess # Import the subprocess module to run terminal commands

HOST = '127.0.0.1'  # Standard loopback interface address (localhost)
PORT = 65432        # Port to listen on (non-privileged ports are > 1023)
BUFFER_SIZE = 1024  # Buffer size for receiving data

def start_server():
    """Start the server and listen for incoming connections."""
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.bind((HOST, PORT))  # Bind the socket to a specific address and port
        server_socket.listen()  # Listen for incoming connections
        print(f'Server listening on {HOST}:{PORT}')

        while True:
            client_socket, client_address = server_socket.accept()  # Accept a new connection
            print(f'Client connected from {client_address}')

            with client_socket:
                while True:
                    data = client_socket.recv(BUFFER_SIZE)  # Receive data from the client

                    if not data:  # If no more data is received, break out of the loop
                        break

                    message = data.decode().strip()  # Decode the received data and remove leading/trailing whitespace
                    print(f'Received message from client: {message}')

                    if message.startswith('/'):  # If the message starts with '/', run it as a terminal command
                        command = message[1:]  # Get the command without the leading '/'
                        try:
                            result = subprocess.check_output(command, shell=True)  # Run the command using subprocess
                            client_socket.sendall(result)  # Send the output of the command back to the client
                        except subprocess.CalledProcessError as e:
                            error_message = f'Command "{command}" failed with error:\n{e.output.decode()}'
                            client_socket.sendall(error_message.encode())  # Send the error message back to the client
                    else:
                        response = f'Echo: {message}'  # Echo the message back to the client
                        client_socket.sendall(response.encode())  # Send the response back to the client

            print(f'Client disconnected from {client_address}')

if __name__ == '__main__':
    start_server()
