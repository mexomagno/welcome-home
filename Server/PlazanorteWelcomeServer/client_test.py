from socket import socket, AF_INET, SOCK_DGRAM, SOCK_STREAM
from server import BROADCAST_PORT, SECRET, SERVER_PORT

def getServerIp():
    s = socket(AF_INET, SOCK_DGRAM)
    s.bind(("", BROADCAST_PORT))
    while 1:
        # Wait for data packet
        data, addr = s.recvfrom(1024)
        # Check if it's what we want
        ip = ""
        if data.startswith(SECRET):
            ip = data[len(SECRET):]
        return ip

def sendRequest(ip, request):
    s2 = socket(AF_INET, SOCK_STREAM)
    s2.connect((ip, SERVER_PORT))
    s2.send(request)
    s2.close()


if __name__ == "__main__":
    print "Searching service..."
    SERVER_IP = getServerIp()
    print "Found at " + SERVER_IP
    print "Sending a little message..."
    sendRequest(SERVER_IP, "Wena servidor conchetumare!")
    print "Done."