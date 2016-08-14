Rough protocol specification for server-client comunication

"service" refers to the core program, which serves the app client.


[Protocol communication scheme]

SERVER                     |                        CLIENT
---------------------------|------------------------------
Broadcasts port number     | Constantly waits for udp
where service resides,     | packet with said port. Only
and a SECRET word to show  | accept the one with
that it's our service      | correct SECRET
---------------------------|------------------------------
Waits for TCP connection   | Tries to connect to the
on service port            | broadcasted port, only if
                           | we're close enough to the
                           | wifi hotspot (to make sure we
                           | are at a certain distance of
                           | it)
---------------------------|------------------------------
                  TCP connection achieved
                           * Send required fields to
                           * service
                          ...
* Validates required fields
* Reads the data
* Execute commands if 
specified
* Sends response
                          ...
                           * Receive response
                           * Do stuff with it
                          