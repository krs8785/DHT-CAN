Source - http://conferences.sigcomm.org/sigcomm/2001/p13-ratnasamy.pdf

<b> Distributed Hash Table </b>

A DHT is a distrbuted system that enables efficient lookup for a file very much similar to a hashtable (key,value) in a peer to peer network. The server within the system can retrieve value associated with the key. Content Addressable Network is peer to peer file sharing and content distributed system. The CAN is scalable, fault-tolerant and completely self-organizing,
and int this project we will develop a system to demonstrate its scalability, robustness and low-latency properties through simulation as given in the above mentioned paper.

<b> Technology </b>

Java 

<b> Architecture </b>
<p align="center">
  <img src="https://github.com/krs8785/DistributedHashTable_ContentAddressableNetwork/upload/master/src/com/CAN/testFiles/abc.png" width="350"/>
</p>

As you can see in the above image the CAN is a N dimensional space which is shared by the different servers. In this project we cnsider a 2 dimensional space with a dimension of 10x10. The new joining server first contacts the bootstrap server which is responsible to route the server to its appropriate position. To allow the CAN to grow incrementally, a new server that joins the system must be allocated its own portion of the coordinate space. This is done by an existing server splitting its allocated zone in half, retaining half and handing the other half to the new server. Once the server is part of the network it can insert or lookup for files.

The project structure consists of a bootstrap server which is the entry point for all the server. CAN has an associated DNS domain
name, and that this resolves to the IP address of one or more CAN bootstrap server. To join a CAN, a new server looks up the CAN domain name in DNS to retrieve a bootstrap node’s IP address. The bootstrap server then supplies the IP addresses of several randomly chosen server currently in the system. The server can then perform lookup and other functions

<b> Functionality </b>
 
  

Step 1

Run the routingServer ie BootStrapServer.java on your server. It will print the IP address of that machine.
It is important to set up bootstrap server which will connect all the new servers together. A new server typically has an associated DNS domain name, and that this resolves to the IP address of one or more server bootstrap nodes. 
I have made small tweaking here. Instead of servers maintaining the IP address we are required to provide the bootstrapServer IP to it.

Step 2

You can start adding new server by running Peer.java on different machines.
Enter the IP address of the routingServer

Run a few of servers and perform the different distributed functionality. I have a few text files that you can upload by giving the correct path. 
Inserting the file will show the route (along with the final destination) where the file is going to be stored.

