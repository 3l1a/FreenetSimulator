FREENET SIMULATOR 
=============

This software is a simulation of the Freenet protocol(v. 0.7)
The project is written in Java ad use the Peersim Simulator ([Official Web](http://peersim.sourceforge.net/)).

## Simulation goal

Freenet protocol v. 0.7 uses a particular type of overlay based in which each peer can be linked only with 'trusted neighbors'
for security and anonimity reasons. 
For improving the routing efficiency without modifying the overlay structure this version of Freenet performs swap operations among nodes. These operations
allow to store the keys in nodes whare it is easy to find them, improving the efficiency of the search.

This software aims to simulate the protocol and analyze the efficiency of searching operations depending by the number of swap executed.

## Usage

```bash
git clone .....
cd FreenetSimulator/
```

change the file ./run/Freenet.cfg. The file itself contains a working model and some instructions about hot to compile it.
Execute with.

```bash
gradle init
gradle simulate
```

