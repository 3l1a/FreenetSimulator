package freenetsimulator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import freenetsimulator.SwapMessage.Type;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
public class EventGenerator implements Control {

	private final int pid;
	private int getRequests;
	private int putRequests;
	private int swapRequests;
	private int swapRequest2;
	private int pause;
	private simulationType type;
	private int getRequestTemp;
	private int swapperget;
	private List<BigInteger> contentKey;

	private enum simulationType {
		MIXED,
		SPLITTED
	}

	public EventGenerator(String prefix) {
		pid = Configuration.getPid(prefix + ".protocol");
		putRequests = Configuration.getInt(prefix + ".putRequests");
		swapRequests = Configuration.getInt(prefix + ".swapRequests");
		getRequests = Configuration.getInt(prefix + ".getRequests");

		if (Configuration.getString(prefix + ".type").equals("mixed")) {
			type = simulationType.MIXED;
			swapperget = Configuration.getInt(prefix + ".swapperget");
		} else {
			type = simulationType.SPLITTED;
			swapRequest2 = Configuration.getInt(prefix + ".swapRequests2");
			getRequestTemp = 0;
		}
		contentKey = new ArrayList<BigInteger>();
		pause = 10;
	}

	@Override
	public boolean execute() {
		Node sender = Network.get(CommonState.r.nextInt(Network.size()));
		if (type == simulationType.SPLITTED) {


			if (pause-- >= 0)
				return false;
			if (swapRequests-- > 0) {
				SwapMessage message = new SwapMessage(sender, sender,
						Type.Start, null);

				EDSimulator.add(0, message, sender, pid);
			} else if (putRequests-- > 0) {
				Content content = new Content();
				contentKey.add(content.GetKey());
				TTLMessage message = new PutSearchMessage(sender, content.key);
				EDSimulator.add(0, message, sender, pid);
			} else if (swapRequest2-- > 0) {
				SwapMessage message = new SwapMessage(sender, sender,
						Type.Start, null);
				EDSimulator.add(0, message, sender, pid);
			} else if (getRequests-- > 0) {

				TTLMessage message = new GetMessage(
						sender,
						contentKey.get(CommonState.r.nextInt(contentKey.size())));
				EDSimulator.add(0, message, sender, pid);
			}
		} else if (getRequests > 0) {
			//handle continued swap request.
			if (swapRequests-- > 0) {
				SwapMessage message = new SwapMessage(sender, sender,
						Type.Start, null);
				EDSimulator.add(0, message, sender, pid);
			} else if (putRequests-- > 0) {
				Content content = new Content();
				contentKey.add(content.GetKey());
				TTLMessage message = new PutSearchMessage(sender, content.key);
				EDSimulator.add(0, message, sender, pid);
				if (putRequests == 1)
					pause = 1000;
			} else {
				if (getRequestTemp == 0) {
					getRequestTemp = 10;
					swapRequest2 = getRequestTemp * swapperget;
				}
				if (swapRequest2-- > 0) {
					SwapMessage message = new SwapMessage(sender, sender,
							Type.Start, null);
					EDSimulator.add(0, message, sender, pid);
				} else if (getRequestTemp-- > 0) {
					getRequests--;
					TTLMessage message = new GetMessage(
							sender,
							contentKey.get(CommonState.r.nextInt(contentKey.size())));
					EDSimulator.add(0, message, sender, pid);
				}
			}
		}
		return false;
	}
}
