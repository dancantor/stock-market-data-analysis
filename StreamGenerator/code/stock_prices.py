from consts import stocks
from random import choice, seed, uniform
from time import sleep
import socket


def generate_random_stock_price(previous_stock_values: dict[str, float]) -> tuple[str, float]:
    stock_name = choice(stocks)
    previous_stock_value = previous_stock_values.get(stock_name, None)
    if previous_stock_value is None:
        current_stock_value = uniform(0, 100)
        return stock_name, current_stock_value
    current_stock_value = previous_stock_value + uniform(-1, 1)
    return stock_name, current_stock_value


def stock_price_generator() -> None:
    seed()
    previous_stock_values = {}

    while True:
        stock_name, stock_price = generate_random_stock_price(previous_stock_values)
        previous_stock_values[stock_name] = stock_price
        data = f"{stock_name},{stock_price:.2f}\n"
        print(data)
        client_socket.sendall(data.encode('utf-8'))
        sleep(2)


server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(('0.0.0.0', 9998))
server_socket.listen(1)
print("Server is listening on port 9998...")
client_socket, addr = server_socket.accept()
print(f"Connection from {addr} has been established.")

try:
    stock_price_generator()
except KeyboardInterrupt:
    print("Shutting down the client.")
finally:
    client_socket.close()
