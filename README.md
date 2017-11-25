FREENET SIMULATOR 
=============

This software is a simulation of the Freenet protocol v. 0.7 ([Freenet Project](https://freenetproject.org/pages/about.html))
The project is written in Java ad use the Peersim Simulator ([Official Web](http://peersim.sourceforge.net/)).

## Simulation

Freenet protocol v. 0.7 uses a particular type of overlay in which each peer can be linked only with 'trusted neighbors'
for security and anonimity reasons. This kind of overlay is named 'Darknet'. 
For improving the routing efficiency without modifying the overlay structure this version of Freenet performs swap operations among nodes. These operations
allow to store the keys in nodes where it is easy to find them, improving the efficiency of the search.

This software aims to simulate the protocol and analyze the efficiency of searching operations depending by the number of swap executed.

## Usage

```bash
git clone i
cd FreenetSimulator/
```

change the file ./run/Freenet.cfg. The file itself contains a working model and some instructions about hot to compile it.
Execute with. Inside the directory ./run/Dataset it is possible to find 3 samples of overlay with different sizes. 
It is possible to choose the overlay by modify the Freenet.cfg being careful to correctly set the size of the overlay.
Then, in order to execute:

```bash
gradle wrapper
./gradlew simulate
```



