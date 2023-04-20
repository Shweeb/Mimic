# import socket

# # Define constants
# HOST = 'localhost'  # Server hostname or IP address
# PORT = 8000  # Port to connect to
# BUFFER_SIZE = 10240  # Buffer size for receiving data

# def start_client():
#     # Create a socket object
#     with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
#         # Connect to the server
#         client_socket.connect((HOST, PORT))

#         while True:
#             # Send a message to the server
#             message = input('Enter a message to send to the server: ')
#             client_socket.sendall(message.encode())

#             # Check if the input is quit and then close if it is
#             if message == 'quit':
#                 break

#             # Receive a response from the server
#             data = client_socket.recv(BUFFER_SIZE)

#             # Print the received response
#             print(f'Received response from server: {data.decode("latin-1")}')


# if __name__ == '__main__':
#     start_client()

# -------------------------------------------------------

import socket, configparser
import tkinter as tk

class ClientGUI:
    def __init__(self, client):
        self.client = client
        self.client.title("Client")

        # Create HOST input text box
        self.host_label = tk.Label(client, text="Host:")
        self.host_label.grid(row=0, column=0)
        self.host_input = tk.Entry(client)
        self.host_input.grid(row=0, column=1)

        # Create CONNECT button
        self.connect_button = tk.Button(client, text="Connect", command=self.connect, bg='green')
        self.connect_button.grid(row=0, column=2)

        # Create PING button
        self.ping_button = tk.Button(client, text="Ping", command=self.check_ping)
        self.ping_button.grid(row=0, column=3)

        # Create DISCONNECT button
        self.disconnect_button = tk.Button(client,
                                           text="Disconnect",
                                           command=self.disconnect,
                                           bg='red',
                                           state="disabled")
        self.disconnect_button.grid(row=0, column=4)

        # Create MESSAGE input text box
        self.message_label = tk.Label(client, text="Message:")
        self.message_label.grid(row=1, column=0)
        self.message_input = tk.Entry(client)
        self.message_input.grid(row=1, column=1)

        # Create SEND MESSAGE button
        self.send_message_button = tk.Button(client, text="Send Message", command=self.send_message)
        self.send_message_button.grid(row=1, column=2)

        # Create CONSOLE text box
        self.console = tk.Text(self.client, state='disabled')
        self.console_scrollbar = tk.Scrollbar(self.client, command=self.console.yview)
        self.console.config(yscrollcommand=self.console_scrollbar.set)

        # Pack console text box and scrollbar
        self.console.grid(row=2, column=0, columnspan=5, sticky="nsew")
        self.console_scrollbar.grid(row=2, column=5, sticky="nsew")

        self.client_socket = None

    def writeToConsole(self, str):
        # Insert text into the console
        self.console.config(state='normal')
        self.console.insert(tk.END, str)
        self.console.config(state='disabled')

    def connect(self):
        # Create a socket object
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        # Connect to the server
        host = self.host_input.get()

        if not host:
            print("ERROR: Blank Host Input\n")
            self.writeToConsole("ERROR: Blank Host Input\n")
            return

        try:
            self.client_socket.connect((host, PORT))
        except ConnectionRefusedError:
            self.writeToConsole("ERROR: Connection refused\n")
            return

        self.connect_button.config(state="disabled")
        self.disconnect_button.config(state="normal")
        self.writeToConsole(f"Connected to {host}\n")

    def disconnect(self):
        if self.client_socket:
            self.client_socket.close()
            self.client_socket = None

        self.connect_button.config(state="normal")
        self.disconnect_button.config(state="disabled")
        self.writeToConsole("Disconnected\n")

    def send_message(self):
        # Get the message from the message input box
        message = self.message_input.get()

        # Blank message handler
        if not message:
            self.writeToConsole("ERROR: Blank Message Input\n")
            return

        if self.client_socket is not None:
            # Send the message to the server
            self.client_socket.sendall(message.encode())

            # Check if the input is quit and then close if it is
            if message == 'quit':
                self.disconnect_from_server()
                return

            # Receive a response from the server
            data = self.client_socket.recv(BUFFER_SIZE)

            # Print the received response
            response = data.decode("latin-1")
            self.writeToConsole(f'Received response from server: {response}\n')
        else:
            self.writeToConsole("ERROR: No Active Connection\n")
            return

        # Clear the message input box
        self.message_input.delete(0, tk.END)

    def check_ping(self):
        host = self.host_input.get()

        if not host:
            print("ERROR: Blank Host Input\n")
            self.writeToConsole("ERROR: Blank Host Input\n")
            return

        try:
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.settimeout(1)
            self.client_socket.connect((host, PORT))
            self.client_socket.close()
            self.writeToConsole(f"{host} is online\n")
        except (ConnectionRefusedError, socket.timeout):
            self.writeToConsole(f"{host} is offline\n")

    def run(self):
        self.client.mainloop()

# Get config file
config = configparser.ConfigParser()
config.read(['config.ini', 'src/config.ini'])

# Call relevant info
HOST = config['Client']['HOST']  # Default server hostname or IP address
PORT = int(config['Client']['PORT'])  # Port to connect to
BUFFER_SIZE = int(config['Client']['BUFFER_SIZE'])  # Buffer size for receiving data

if __name__ == '__main__':
    client = tk.Tk()
    client = ClientGUI(client)
    client.run()

