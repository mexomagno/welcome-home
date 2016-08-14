#!/usr/bin/python
from socket import socket, AF_INET, SOCK_DGRAM, SOL_SOCKET, SO_BROADCAST, SOCK_STREAM, gethostbyname, gethostname
from time import sleep
from threading import Thread
import json
import signal
from sys import exit

# General settings
BUFFER_SIZE = 1024

# Service broadcast settings
#servicebroadcast_thread;
BROADCAST_PORT = 50000
BROADCAST_IP = "192.168.0.255"
SECRET = "laminatenicida"
SLEEP_PERIOD = 5

# Server settings
#requestsserver_thread;
SERVER_PORT = 8005

def getOwnIpAddress():
    s = socket(AF_INET, SOCK_DGRAM)
    s.connect(('8.8.8.8', 1))  # connecting to a UDP address doesn't send packets
    return s.getsockname()[0]

MY_IP = getOwnIpAddress() #None # gethostbyname(gethostname())

# Error codes
E_SUCCESS = 0
E_UNKNOWN = 1
E_MALFORMED_DATA = 2
E_INSUFFICIENT_DATA = 3
E_NO_DATA = 4

def serviceInfoBroadcast():
    # Settings
    # Create broadcast socket
    s = socket(AF_INET, SOCK_DGRAM)
    s.bind(("", 0))
    s.setsockopt(SOL_SOCKET, SO_BROADCAST, 1)

    # Broadcast our info, waiting for a client
    while RUN:
        data = "{}:{}".format(SECRET, SERVER_PORT)
        s.sendto(data, ("192.168.0.255", BROADCAST_PORT))
        print "Sent broadcast with our service info"
        sleep(SLEEP_PERIOD)
    s.close()

def requestsServer():
    # Create socket
    print "Creating requests server on port {}".format(SERVER_PORT)
    s = socket(AF_INET, SOCK_STREAM)
    s.bind((MY_IP, SERVER_PORT))
    s.listen(1)
    # wait for requests
    while RUN:
        print "Waiting for TCP connection..."
        conn, addr = s.accept()
        print "Connected with host '{}'".format(addr)
        response = serve(conn)
        conn.send("{}".format(response))
        print "Operation ended with code '{}'".format(response)
        conn.close()
    s.close()

def serve(conn):
    """
    Give service to connected host
    :param conn: connection with host
    :return: operation status code


    Data received requires the following structure
    - Must be a string representing a dictionary (JSON)
    - Dictionary must have the required keys specified in REQUIRED_KEYS
    """
    REQUIRED_KEYS = ["secret", "username", "command", "args"]
    data = conn.recv(BUFFER_SIZE)
    if not data:
        print "Errors reading data"
        return E_NO_DATA
    # Check data
    print "Data received: " + data
    try:
        data_dict = json.loads(data, "utf-8")
    except ValueError:
        return E_MALFORMED_DATA
    # Check fields
    for key in REQUIRED_KEYS:
        if key not in data_dict.keys():
            return E_INSUFFICIENT_DATA
    return E_SUCCESS

def endProgram(signum, frame):
    global servicebroadcast_thread, requestsserver_thread
    print "Closing program. Waiting for threads to end..."
    RUN = False
    servicebroadcast_thread.join()
    requestsserver_thread.join()
    print "Done. Chao conchetumare."
    exit(0)


if __name__ == "__main__":
    global servicebroadcast_thread, requestsserver_thread
    RUN = True
    # Start signal handler
    signal.signal(signal.SIGINT, endProgram)
    # Start broadcasting our service information
    servicebroadcast_thread = Thread(group=None, target=serviceInfoBroadcast)
    servicebroadcast_thread.start()
    # Start requests server
    requestsserver_thread = Thread(group=None, target=requestsServer)
    requestsserver_thread.start()