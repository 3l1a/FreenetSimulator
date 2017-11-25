package freenetsimulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
public class FreenetInitializer implements Control {
		int pid;
		String dataset;
		public FreenetInitializer(final String prefix) {
            pid = Configuration.getPid(prefix + "." + "protocol");
            dataset = Configuration.getString(prefix + "." + "dataset");
		}

		@Override
        public boolean execute() {
            HashMap<BigInteger, Node> hashmap = new HashMap<>();
            int index = 0;
            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ",";

            try {
                br = new BufferedReader(new FileReader(dataset));

                while ((line = br.readLine()) != null) {
                // use comma as separator
                    String[] ids = line.split(cvsSplitBy);

                    if (ids.length != 2) continue;

                    Node node1;
                    Node node2;
                    BigInteger id1 = new BigInteger(ids[0],16);
                    BigInteger id2 = new BigInteger(ids[1],16);

                    if ((node1 = hashmap.get(id1)) == null){
                        node1 = Network.get(index++);
                        ((FreenetProtocol) node1.getProtocol(pid)).id = id1;
                        hashmap.put(id1, node1);
                    }

                    if ((node2 = hashmap.get(id2)) == null){
                        node2 = Network.get(index++);
                        ((FreenetProtocol) node2.getProtocol(pid)).id = id2;
                        hashmap.put(id2, node2);
                    }

                    ((FreenetProtocol) node1.getProtocol(pid)).AddNeighbor(node2);
                    ((FreenetProtocol) node2.getProtocol(pid)).AddNeighbor(node1);
                    ((FreenetProtocol) node1.getProtocol(pid)).updateNeighbor(node2, id2);
                    ((FreenetProtocol) node2.getProtocol(pid)).updateNeighbor(node1, id1);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("############## INITIALIZATION DONE ##############");
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("");

            return false;
        }
}