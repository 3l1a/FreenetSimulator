package freenetsimulator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import freenetsimulator.ReplyMessage.ResponseType;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;

public class StatControl implements Control {
	protected static final int END_SIMULATION_TIME = 100000000;
	protected static int TTlExpiredMessageNumber = 0;
	protected static int swapSuccess = 0;
	protected static int numResponsePositive = 0;
	protected static boolean print_begin = true;


	static List<Couple<Integer, Integer>> hopsDoneList = new ArrayList<>();
	static List<BigInteger> storeDistance = new ArrayList<>();
	static List<Couple<Integer, Integer>> TTLexpired = new ArrayList<>();
	static List<Couple<Integer, Integer>> Swap = new ArrayList<>();

	static Queue<ReplyMessage.ResponseType> lastEvents = new ArrayDeque<>();

	protected static int numResponseNegativettl;
	protected static List<Integer>getHopsDoneList = new ArrayList<>();
	protected static int numResponseRetrievednotfound;
	protected File fHDone;
	protected File fStoreDistances;

	/*
    * Because of files requested are always present in the network, failures
    * are always TTL Expiration.
    */
	private File fFailGetRequests;
	private File fSwap;
	private boolean doit;
	private int pid;

	public StatControl(String prefix) {
//		if (!Files.isDirectory(Paths.get("output"), null)) {
//			try {
//				Files.createDirectories(Paths.get("output"));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		pid = Configuration.getPid(prefix + ".protocol");
		fHDone = new File("output/hopsDone");
		fStoreDistances = new File("output/storeDistances");
		fFailGetRequests = new File("output/failRequests");
		fSwap = new File("output/swaps");
		doit = true;
	}

	protected static synchronized void handleResults(GetRoutingEntry re, ResponseType type) {




		if (lastEvents.size() == 1000){
			if (print_begin){
				System.out.println("############## STATS AFTER SIMULATION BEGINNING ################");
				printStats();
				print_begin = false;
			}
			lastEvents.remove();
		}

		lastEvents.add(type);

		if (type == ResponseType.FOUND) {
			numResponsePositive++;
			getHopsDoneList.add(re.hopsDone);
			StatControl.hopsDoneList.add(new Couple<>(
					CommonState.getIntTime(), re.hopsDone));
			StatControl.TTLexpired.add(new Couple<>(CommonState
					.getIntTime(), StatControl.TTlExpiredMessageNumber));
		} else if (type == ResponseType.TTlEXPIRED) {

			StatControl.TTlExpiredMessageNumber++;
			StatControl.TTLexpired.add(new Couple<>(CommonState
					.getIntTime(), StatControl.TTlExpiredMessageNumber));
			numResponseNegativettl++;
		} else {
			numResponseRetrievednotfound++;
		}

	}

	private static int getAVGHopsDone() {
		int v = 0;
		for (Integer i : getHopsDoneList){
			v += i;
		}
		if (getHopsDoneList.size() != 0)
			return v/getHopsDoneList.size();
		return 0;
	}

	private static synchronized void printStats(){

		long nPosRes = lastEvents.stream().filter(o -> o.equals(ResponseType.FOUND)).count();
		long nTTLRes = lastEvents.stream().filter(o -> o.equals(ResponseType.TTlEXPIRED)).count();
		long totalEvents = nPosRes + nTTLRes;

		System.out.println("###### POSITIVE REPONSE RATIO: " + ((double) nPosRes / totalEvents ));
		System.out.println("###### TTL RESPONSE RATIO: " + ((double) nTTLRes / totalEvents ));
		System.out.println("###### SAMPLE SIZE: " + totalEvents);
		System.out.println("###### AVG HOPS DONE: " + getAVGHopsDone());
	}

	protected static synchronized void swapResult(boolean success){
		if (success)
			swapSuccess++;
		Swap.add(new Couple<>(CommonState.getIntTime(), swapSuccess));
	}

	@Override
	public boolean execute() {

		if (CommonState.getTime() >= END_SIMULATION_TIME) {
			BufferedWriter b = null;
			try {
				b = new BufferedWriter(new FileWriter(fHDone));
				for (int i = 0; i < hopsDoneList.size(); i++) {
					b.write(hopsDoneList.get(i).elem1.intValue() + " "
							+ hopsDoneList.get(i).elem2.intValue() + " \n");
				}
				b.close();
				b = new BufferedWriter(new FileWriter(fFailGetRequests));
				for (int i = 0; i < TTLexpired.size(); i++) {
					b.write(TTLexpired.get(i).elem1.intValue() + " "
							+ TTLexpired.get(i).elem2.intValue() + " \n");
				}
				b.close();
				b = new BufferedWriter(new FileWriter(fStoreDistances));
				for (int i = 0; i < storeDistance.size(); i++) {
					b.write(i + " " + storeDistance.get(i).intValue() + " \n");
				}
				b.close();
				b = new BufferedWriter(new FileWriter(fSwap));
				for (int i = 0; i < Swap.size(); i++) {
					b.write(Swap.get(i).elem1.intValue() + " "
							+ Swap.get(i).elem2.intValue() + " \n");

				}

				b.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("############## STATS BEFORE SIMULATION ENDING ################");
			printStats();
			System.out.println("############## END SIMULATION ################################");
			return true;
		}
		return false;
	}
}