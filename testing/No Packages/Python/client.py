import socket

# Define constants
HOST = 'localhost'  # Server hostname or IP address
PORT = 8000  # Port to connect to
BUFFER_SIZE = 1024  # Buffer size for receiving data

def start_client():
    # Create a socket object
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
        # Connect to the server
        client_socket.connect((HOST, PORT))

        # Send a message to the server
        message = input('Enter a message to send to the server: ')
        client_socket.sendall(message.encode())

        # Receive a response from the server
        data = client_socket.recv(BUFFER_SIZE)

        print(data)
        print(type(data))

        # Print the received response
        print(f'Received response from server: {data.decode()}')

if __name__ == '__main__':
    start_client()
