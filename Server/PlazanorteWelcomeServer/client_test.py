import json
from socket import socket, AF_INET, SOCK_DGRAM, SOCK_STREAM
from server import BROADCAST_PORT

SERVICE_SECRET = "laminatenicida"

def getServerIp():
    s = socket(AF_INET, SOCK_DGRAM)
    s.bind(("", BROADCAST_PORT))
    while 1:
        # Wait for data packet
        data, addr = s.recvfrom(1024)
        # Check if it's what we want
        ip = ""
        if data.startswith(SERVICE_SECRET):
            SERVICE_PORT = int(data[(len(SERVICE_SECRET)+1) :])
            addr = (addr[0], SERVICE_PORT)
            return addr
        else:
            return None


def sendRequest(addr, request):
    s2 = socket(AF_INET, SOCK_STREAM)
    s2.connect(addr)
    s2.send(request)
    response = s2.recv(1024)
    print response
    s2.close()

def generateCorrectRequest():
    req = {"username": "mexomagno",
           "secret" : SERVICE_SECRET,
           "command": None,
           "args": None}
    return json.dumps(req)

if __name__ == "__main__":
    print "Searching service..."
    SERVER_ADDR = getServerIp()
    if SERVER_ADDR == None:
        print "not our service!"
        exit()
    print "Found at {}:{}".format(SERVER_ADDR[0], SERVER_ADDR[1])
    print "Sending a little message..."
    sendRequest(SERVER_ADDR, generateCorrectRequest())
    print "Done."