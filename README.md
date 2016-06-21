Source - http://conferences.sigcomm.org/sigcomm/2001/p13-ratnasamy.pdf

Step 1

Run the routingServer ie BootStrapServer.java on your server. It will print the IP address of that machine.
It is important to set up bootstrap server which will connect all the new servers together. A new server typically has an associated DNS domain name, and that this resolves to the IP address of one or more server bootstrap nodes. 
I have made small tweaking here. Instead of servers maintaining the IP address we are required to provide the bootstrapServer IP to it.

Step 2
You can start adding new server by running Peer.java on different machines.

Run a few of servers and perform the different distributed functionality. I have a few text files that you can upload by giving the correct path. 
Inserting the file will show the route (along with the final destination) where the file is going to be stored.

