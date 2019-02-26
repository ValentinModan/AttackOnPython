
from bluetooth import *
import sys
import json
import time

if sys.version < '3':
    input = raw_input

addr = "A4:50:46:1F:BE:90"

if len(sys.argv) < 2:
    print("no device specified.  Searching all nearby bluetooth devices for")
    print("the SampleServer service")
else:
    addr = sys.argv[1]
    print("Searching for SampleServer on %s" % addr)

# search for the SampleServer service
uuid = "f0937a74-f0e3-11e8-8eb2-f2801f1b9fd1" 
service_matches = find_service( uuid = uuid, address = addr )

if len(service_matches) == 0:
    print("couldn't find the SampleServer service =(")
    sys.exit(0)

first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print("connecting to \"%s\" on %s" % (name, host))

# Create the client socket
sock=BluetoothSocket( RFCOMM )
sock.connect((host, port))


print("connected.  type stuff")

a = "aaa"
b = "bbb"
myDictionary = {
    "a":1,
    "b":2
}

while True:
    #data = input()
    time.sleep(1)
    data_string = json.dumps(myDictionary)
    #if len(data) == 0: break
    sock.send(data_string)
    myDictionary["a"] = myDictionary["a"] + 1

sock.close()
