import socket
import threading
from rdpy.protocol.rdp import rdp

class RdpServer:
    def __init__(self, host, port):
        self.host = host
        self.port = port

    def start(self):
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.bind((self.host, self.port))
        s.listen(1)

        while True:
            conn, addr = s.accept()
            t = threading.Thread(target=self.handle_client, args=(conn,))
            t.start()

    def handle_client(self, conn):
        rdp(conn).exchange()
        conn.close()

if __name__ == '__main__':
    server = RdpServer('0.0.0.0', 3389)
    server.start()
