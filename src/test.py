import configparser

# -------------------------------------ConfigParser-------------------------------------
#region
# config = configparser.ConfigParser()
# output = config.read(['src/config.ini', 'config.ini'])

# print(output, "\n")

# server_host = config['Server']['HOST']
# server_port = config['Server']['PORT']
# server_buffer_size = config['Server']['BUFFER_SIZE']

# client_host = config['Client']['HOST']
# client_port = config['Client']['PORT']
# client_buffer_size = config['Client']['BUFFER_SIZE']

# print(f"Server Host: {server_host}")
# print(f"Server Port: {server_port}")
# print(f"Server Buffer Size: {server_buffer_size}")
# print(f"Client Host: {client_host}")
# print(f"Client Port: {client_port}")
# print(f"Client Buffer Size: {client_buffer_size}")

# n = input()
#endregion

# -------------------------------------PyRDP-------------------------------------
#region
import freerdp
import time

def on_rail(session, width, height, bpp):
    print("RDP session is on the rail")

def on_resize(session, width, height, bpp):
    print("RDP session has been resized to {}x{}x{} bits per pixel".format(width, height, bpp))

def on_disconnect(session):
    print("RDP session has been disconnected")

def on_error(session, code):
    print("RDP session error: {}".format(freerdp.get_error_message(code)))

def on_login_complete(session):
    print("RDP session login complete")

def on_graphics_update(session, x, y, width, height, bpp, data, length):
    print("RDP session graphics update at ({}, {}), size={}x{}, bpp={}, data length={}".format(x, y, width, height, bpp, length))

def on_channel_data(session, channel_id, data, length):
    print("RDP session received data on channel {}, data length={}".format(channel_id, length))

def run_rdp_server():
    # Define RDP settings
    settings = freerdp.Settings()
    settings.server_hostname = "localhost"
    settings.server_port = 3389
    settings.username = "user"
    settings.password = "password"
    settings.desktop_width = 1024
    settings.desktop_height = 768
    settings.color_depth = 32

    # Create RDP instance
    rdp = freerdp.RDP()
    rdp.on_rail = on_rail
    rdp.on_resize = on_resize
    rdp.on_disconnect = on_disconnect
    rdp.on_error = on_error
    rdp.on_login_complete = on_login_complete
    rdp.on_graphics_update = on_graphics_update
    rdp.on_channel_data = on_channel_data

    # Connect to RDP server
    rdp.connect(settings)

    # Wait for RDP session to start
    while not rdp.is_connected():
        time.sleep(1)

    # Run RDP session loop
    while rdp.is_connected():
        try:
            rdp.check_activity()
        except KeyboardInterrupt:
            rdp.disconnect()
            break

if __name__ == "__main__":
    run_rdp_server()


#endregion
