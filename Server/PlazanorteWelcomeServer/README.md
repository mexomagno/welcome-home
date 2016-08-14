WelcomeHome Server
==================

This piece of software is in charge of doing all the stuff we want to do
when the person arrives home.
From here we can turn lights on-off, put some music, say some friendly
greeting words and whatnot.

Usage
-----
Simply run the server on a capable machine. This was mostly thought to
be run on a headless 24/7 Raspberry Pi computer.
```
usage: server.py [-h] [-p PORT] [-s SECRET]

WelcomeHome greeting server

optional arguments:
  -h, --help            show this help message and exit
  -p PORT, --port PORT  Specify port where requests will be listened to
  -s SECRET, --secret SECRET
                        Specify secret for service identification

```

Details
-------
The server constantly broadcasts to the network the service's port, and
a secret word to help the client make sure we're the service it wants
to use.
When a client connects to the service, we first validate it's message 
and check that it's using a registered username. 
Only then we proceed to parse the input and do our logic.

Components
----------
The actual code isn't as structured as it's shown here, but the server
is made up from the following parts:

- **UDP Broadcast thread**: Constantly broadcasts to the network the
needed info to use the service
- **TCP Connection Handler**: Listens to any client who wants to say
something to our service. From here we serve requests and send the
corresponding response
- ~~**Greeting manager**~~: _Unimplemented_
Generates friendly, hopefully contextualized greetings and displays 
them or speaks them. 
- ~~**Music Controller**~~: _Unimplemented_
Tiny bridge to mpd server
- ~~**Logging module**~~: _Unimplemented_
For administration and debugging.