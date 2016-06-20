Source - http://conferences.sigcomm.org/sigcomm/2001/p13-ratnasamy.pdf

Step 1

Run the routingServer ie BootStrapServer.java on your server. It will print the IP address of that machine.
It is important to set up bootstrap server which will connect all the new servers together. A new server typically has an associated DNS domain name, and that this resolves to the IP address of one or more server bootstrap nodes. 
I have made small tweaking here. Instead of servers maintaining the IP address we are required to provide the bootstrapServer IP to it.


