from consts import stocks
from random import choice, uniform, seed
from time import sleep
import socket


def generate_random_market_impact() -> tuple[str, float]:
    stock_name = choice(stocks)
    stock_trade_volume = uniform(-5, 5)
    return stock_name, stock_trade_volume


def market_impact_generator():
    seed()

    while True:
        stock_name, market_impact = generate_random_market_impact()
        data = f"{stock_name},{market_impact:.2f}\n"
        print(data)
        client_socket.sendall(data.encode('utf-8'))
        sleep(2)


server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(('0.0.0.0', 9999))
server_socket.listen(1)
print("Server is listening on port 9999...")
client_socket, addr = server_socket.accept()
print(f"Connection from {addr} has been established.")


try:
    market_impact_generator()
except KeyboardInterrupt:
    print("Shutting down the client.")
finally:
    client_socket.close()