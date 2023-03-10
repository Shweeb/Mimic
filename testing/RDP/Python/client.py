import socket
from rdpy.protocol.rdp import rdp

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(('localhost', 3389))

rdp(s).exchange()

s.close()
