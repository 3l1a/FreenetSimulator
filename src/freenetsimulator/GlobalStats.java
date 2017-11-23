package freenetsimulator;

import java.util.ArrayList;
import java.util.List;

public class GlobalStats {
	public static int numResponseRetrievednotfound = 0;
	public static int numResponsePositive = 0;
	public static int numResponseNegativettl = 0;
	public static List<Integer >hopsDoneList = new ArrayList<>();

	
	public static int getAVGHopsDone(){
		int v = 0;
		for (Integer i : hopsDoneList){
			v += i;
		}
		if (hopsDoneList.size() != 0)
			return v/hopsDoneList.size();
		return 0;
	}

}
