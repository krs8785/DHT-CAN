Source - http://conferences.sigcomm.org/sigcomm/2001/p13-ratnasamy.pdf


If you already know the concept of CAN you can directly run the project and skip the high level description below

1. Run the bootstrap server (/routingServer)
   Keep a note of the IP address of the server.
2. Run few servers and start inserting/searching files (/NodeServer)
   Enter the IP address of the bootstrap to get connected. 

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
  
1. JOIN: A request is sent to the bootstrap from a joining node. The bootstrap randomly chooses a node already in the CAN and routes this request to this node. This node then randomly chooses co-ordinates within the CAN. If these co-ordinates lie within this node itself, the node splits and transmits necessary state information to the joining node. Otherwise, all the neighbours are checked to find the closest one to the destination co-ordinates. The information is relayed to this peer and thus the relaying continues till the appropriate zone is discovered.

2. INSERT: Hash of the key is calculated and co-ordinates are determined. If the co-ordinates lie in the requesting zone itself, the key is inserted in the hashtable. Otherwise, routing to the destination co-ordinates happens the same way as in join.

3. SEARCH: Hash of the key is calculated and co-ordinates are determined. If the co-ordinates lie in the requesting zone itself, the key is looked up in the hashtable. Otherwise, routing to the destination co-ordinates happens the same way as in join. If the key is found the route to the destination node from the source node is displayed.

4. VIEW: If a peer node id is provided as a parameter the ip address of the node is obtained from the bootstrap. If not provided, the ip addresses of all the active nodes are obtained from the bootstrap. Each of the peers is then requested for its data by the requesting peer and the data is accordingly painted on the standard output.

5. LEAVE: Implemented but some issues still persist. Will fix these later. The expected behaviour is that the requesting node should exit the CAN and the boundaries of the neighbours are appropriately adjusted and the keys of this leaving node get assigned to the smaller neighbouring zone to keep the distribution of the keys fairly even.


