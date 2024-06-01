from consts import stocks
from random import choice, randint, seed
from time import sleep
import socket


def generate_random_trade_volume() -> tuple[str, float]:
    stock_name = choice(stocks)
    stock_trade_volume = randint(100, 10000)
    return stock_name, stock_trade_volume


def trade_volumes_generator():
    seed()

    while True:
        stock_name, trade_volume = generate_random_trade_volume()
        data = f"{stock_name},{trade_volume}\n"
        print(data)
        client_socket.sendall(data.encode('utf-8'))
        sleep(2)


server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(('0.0.0.0', 9997))
server_socket.listen(1)
print("Server is listening on port 9997...")
client_socket, addr = server_socket.accept()
print(f"Connection from {addr} has been established.")


try:
    trade_volumes_generator()
except KeyboardInterrupt:
    print("Shutting down the client.")
finally:
    client_socket.close()