import socket

def start_server():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(('localhost', 9999))
    server_socket.listen(1)
    print("Server is listening on port 9999...")
    while True:
        client_socket, addr = server_socket.accept()
        print(f"Connection from {addr} has been established.")
        while True:
            data = client_socket.recv(1024)
            if not data:
                break
            print(f"Received data: {data.decode('utf-8')}")
        client_socket.close()

try:
    start_server()
except KeyboardInterrupt:
    print("Server is shutting down.")