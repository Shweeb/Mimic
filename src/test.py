import configparser, os

# # list files in the current directory
# print("Files in current directory:")
# for filename in os.listdir('.'):
#     print(filename)

# # list files in the parent directory
# print("Files in parent directory:")
# for filename in os.listdir('..'):
#     print(filename)

# config_file_path = 'config.ini'
# if os.path.exists(config_file_path):
#     print(f"The file '{config_file_path}' exists!")
# else:
#     print(f"The file '{config_file_path}' does not exist.")

config = configparser.ConfigParser()
output = config.read('src/config.ini')

print(output, "\n")

server_host = config['Server']['HOST']
server_port = config['Server']['PORT']
server_buffer_size = config['Server']['BUFFER_SIZE']

client_host = config['Client']['HOST']
client_port = config['Client']['PORT']
client_buffer_size = config['Client']['BUFFER_SIZE']

print(f"Server Host: {server_host}")
print(f"Server Port: {server_port}")
print(f"Server Buffer Size: {server_buffer_size}")
print(f"Client Host: {client_host}")
print(f"Client Port: {client_port}")
print(f"Client Buffer Size: {client_buffer_size}")
