import socket
import subprocess

# Define constants
HOST = 'localhost'  # Server hostname or IP address
PORT = 8000  # Port to listen on
BUFFER_SIZE = 1024  # Buffer size for receiving data

def start_server():
    # Create a socket object
    # Using 'with' makes sure that the server auto closes upon lost connection
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        # Bind the socket to a public host and port
        server_socket.bind((HOST, PORT))

        # Listen for incoming connections
        server_socket.listen()

        # Print a message indicating the server is listening
        print(f'Server is listening on {HOST}:{PORT}')

        while True:
            # Accept a new client connection
            client_socket, client_address = server_socket.accept()

            # Print a message indicating the client has connected
            print(f'New client connected from {client_address[0]}:{client_address[1]}')

            # Handle client requests
            handle_client(client_socket, client_address)

def handle_client(client_socket, client_address):
    # Makes sure the client socket closes upon code completion
    with client_socket:
        command = None  # Initialize command variable to None

        while True:
            # Receive data from the client
            data = client_socket.recv(BUFFER_SIZE)

            # If no more data, break the loop
            if not data:
                break

            # Decode the received data and remove leading/trailing whitespace
            message = data.decode().strip()

            # Print the received data
            print(f'Received data from {client_address[0]}:{client_address[1]}: {message}')

            # If the message starts with '/', run it as a terminal command
            if message.startswith('/'):
                # Get the command without the leading '/'
                command = message[1:]

                try:
                    result = subprocess.check_output(command, shell=True)  # Run the command using subprocess
                    client_socket.sendall(result)  # Send the output of the command back to the client
                    command = None  # Reset command variable to None after running help command
                except subprocess.CalledProcessError as e:
                    error_message = f'Command "{command}" failed with error:\n{e.output.decode()}'
                    print(type(error_message))

                    client_socket.sendall(error_message.encode())  # Send the error message back to the client
            else:
                response = f'Echo: {message}'  # Echo the message back to the client
                client_socket.sendall(response.encode())  # Send the response back to the client

    print(f'Client {client_address[0]}:{client_address[1]} disconnected\n')

if __name__ == '__main__':
    start_server()
