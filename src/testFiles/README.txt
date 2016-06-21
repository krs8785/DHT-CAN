--Karan Shah

Example

1. Complile the program
javac *.java

2. Run the BootStrapServer on glados.cs.rit.edu
java BootStrapServer
(It will print the server IP)
The Server IP is:glados/129.21.30.38

3. Run 1st Peer on queeg.cs.rit.edu
java Peer
Enter the BootStrap Id
129.21.30.38

4. Insert 
(Make sure txt file in same folder or give path also)
I have provided a few txt files
test.txt
karan.txt
movie.txt
(Insert Them) 
You will see the route information too.

5. Join 2nd peer on yes.cs.rit.edu
If you view the files (press 6) on this peer you will see
that the files have been split accordingly as well as the zones.
hurray!
Professor had asked to implement leave on 2/3 nodes
so now press 3 to leave. Again back to 1st peer(Queeg) press 6 to view files. You can see that new files have been added and zone information is also changed
Leave works! (for 2-3 nodes. Will fail more than 4 as there were many corner cases I havent handled)

Add 2nd peer back by running. you will again see the same changes

6. Join Peer 3 on joplin.cs.rit.edu
You will see that files have moved again and so have the zone information changed also neigbhor information.

7. Join peer 4 buddy.cs.rit.edu
8. Join Peer 5 nial.cs.rit.edu
9. join peer 6 gorgon.cs.rit.edu
10. join peer 7 doors.cs.rit.edu
search for test.txt file
find through routing to peer with IP-31

Everything works :)