package freenetsimulator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import freenetsimulator.PutRoutingEntry.Phase;
import freenetsimulator.ReplyMessage.ResponseType;
import freenetsimulator.SwapMessage.Type;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;

public class FreenetProtocol implements EDProtocol {

	public static final int MAX_ID_BIG_LENGTH = 16;
	public static final String MAXID = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
	public static int replication;

	enum SwapState {
		noSwap, sentRequest, waitResponse, waitData
	}

	private SwapState swapstate = SwapState.noSwap;
	private Node swapnode = null;
	final String prefix;
	public BigInteger id;

	/**
	 * Trusted neighbors
	 */
	public List<Node> Neighbors;
	private HashMap<Node, BigInteger> NeighborsIds;
	private LinkedHashMap<BigInteger, Content> StoreElements;
	private LinkedHashMap<Long, PutRoutingEntry> putRoutingTable;
	private LinkedHashMap<Long, GetRoutingEntry> getRoutingTable;
	private LinkedHashMap<Long, SwapRoutingEntry> swapRoutingTable;

	public FreenetProtocol(final String prefix_) {
		prefix = prefix_;
		id = null;
		replication = Configuration.getInt(prefix_ + ".replication");
		Neighbors = new ArrayList<>();
		NeighborsIds = new HashMap<>();
		StoreElements = new LinkedHashMap<>();
		putRoutingTable = new LinkedHashMap<>();
		getRoutingTable = new LinkedHashMap<>();
		swapRoutingTable = new LinkedHashMap<>();
	}

	@Override
	public Object clone() {
		FreenetProtocol fn = new FreenetProtocol(prefix);
		fn.Neighbors = new ArrayList<>();
		fn.NeighborsIds = new HashMap<>();
		fn.StoreElements = new LinkedHashMap<>();
		fn.putRoutingTable = new LinkedHashMap<>();
		fn.getRoutingTable = new LinkedHashMap<>();
		fn.swapRoutingTable = new LinkedHashMap<>();
		return fn;
	}

	@Override
	public void processEvent(Node node_, int pid_, Object event_) {
		if (event_.getClass() == PutMessage.class)
			handlePutMessage(node_, pid_, event_);
		else if (event_.getClass() == GetReply.class)
			handleReplyGetMessage(node_, pid_, event_);
		else if (event_.getClass() == PutReply.class)
			handleReplyPutMessage(node_, pid_, event_);
		else if (event_.getClass() == GetMessage.class)
			handleGetMessage(node_, pid_, event_);
		else if (event_.getClass() == PutSearchMessage.class)
			handlePutSearchMessage(node_, pid_, event_);
		else if (event_.getClass() == IdMessage.class) {
			// update neighbors id
			IdMessage message = (IdMessage) event_;
			NeighborsIds.put(message.getSender(), message.getId());

		} else if (event_.getClass() == SwapMessage.class) {
			//automata for ids swapping
			handleSwapMessage(node_, pid_, event_);
		} else if (event_.getClass() == SwapRandomSearch.class)
			handleSwapSearch(node_, pid_, event_);
	}

	/**
	 * Handles put search reply.
	 *
	 * @param node_
	 * @param pid_
	 * @param event_
	 */
	protected void handleReplyPutMessage(Node node_, int pid_, Object event_) {
		PutReply message = (PutReply) event_;
		PutRoutingEntry re = putRoutingTable.get(message.getMessageId());
		re.update(message);
		if (message.getDest() == node_) {
// && message.getResponseType() != ResponseType.NOTFOUND) {
			sendPutMessage(re, node_, pid_);
		} else {
			routeRequest(node_, pid_, re);
		}
	}


	/**
	 * Handles swap search message. In this phase random routing is performed in
	 * order to find the second node with which computing swap.
	 *
	 * @param node_
	 * @param pid_
	 * @param event_
	 */
	protected void handleSwapSearch(Node node_, int pid_, Object event_) {
		SwapRandomSearch message = (SwapRandomSearch) event_;
		SwapRoutingEntry re = swapRoutingTable.get(message.getMessageId());
		if (re == null) {
			re = new SwapRoutingEntry();
			/* if is start message */
			re.previous = (message.getSender() == null ? node_ : message.getPrevious());
			swapRoutingTable.put(message._messageId, re);
		} else {
			message.HTL = 0;
		}

		if (message.HTL > 0) {
			re.next = Neighbors.get(CommonState.r.nextInt(Neighbors.size()));
			message.HTL--;
			sendMessage(node_, message, re.next, pid_);
		} else {

			re.next = node_;

			/*
			* Check if routing is end in start node and avoid swap in positive
			* case.
			*/
			if (message.getSender() == node_) {
				swapstate = SwapState.noSwap;
				return;
			}

			SwapMessage sm = message.sm;
			sm.dest = node_;
			sm.setPrevious(re.previous);

			/*
			* At this point route path is built and control moves to
			* swapMessage Handler.
			*/
			handleSwapMessage(node_, pid_, sm);
		}
	}

	/**
	 * Send put message toward computed destination.
	 *
	 * @param re
	 * @param node_
	 * * @param pid_
	 */
	protected void sendPutMessage(PutRoutingEntry re, Node node_, int pid_) {
		Content content = new Content();
		content.key = re.key;
		PutMessage message = new PutMessage(node_, re.id, content);
		if (re.routeForPut != null) {
			sendMessage(node_, message, re.routeForPut, pid_);
		}
	}

	/**
	 * Handles put message. Notice that this message bring data through the routing path yet computed
	 * by put search request.
	 *
	 * @param node_
	 * @param pid_
	 * @param event_
	 */
	protected void handlePutMessage(Node node_, int pid_, Object event_) {
		PutMessage message = (PutMessage) event_;
		PutRoutingEntry re;

		if ((re = putRoutingTable.get(message.getMessageId())) == null) {
			store(message.getContent());
			return;
		}

		if (re.pathLenght < replication && re != null) {
			if (re.pathLenght == 0)
				StatControl.storeDistance.add(distance(id, message.getContent()
						.GetKey()));
			store(message.getContent());
		}

		if (re.pathLenght >= 0) {
			sendPutMessage(re, node_, pid_);
		}
	}

	/**
	 * Handles put search messages.
	 *
	 * @param node_
	 * @param pid_
	 * @param event_
	 */
	protected void handlePutSearchMessage(Node node_, int pid_, Object event_) {

		PutSearchMessage message = (PutSearchMessage) event_;
		PutRoutingEntry re = null;
		Content content = null;

		if (distance(message.key, id).compareTo(message.bestDistance) < 0) {
			message.resetTTL();
			message.bestDistance = distance(message.key, id);
		}

		/** Very improbable case */
		if ((content = StoreElements.get(message.getKey())) != null) {
			if (message.getSender() == node_) {
				return;
			}

			PutReply reply = new PutReply(node_, message.getSender(),
					message.getMessageId(), ResponseType.FOUND);
			sendMessage(node_, reply, message.getPrevious(), pid_);
		}

		/**
		 * if routing has yet reached this node, send not found message.
		 */
		else if ((re = putRoutingTable.get(message.getMessageId())) != null) {
			PutReply reply = new PutReply(node_, message.getSender(),
					message.getMessageId(), ResponseType.NOTFOUND);
			reply.TTL = message.getTTL();
			sendMessage(node_, reply, message.getPrevious(), pid_);
		} else {
			re = new PutRoutingEntry(message, id, node_);
			if (re.phase == Phase.BEFORE_LOCAL_MINIMAL)
				if (isLocalMinimum(message.key, message._previous))
					re.phase = Phase.LOCAL_MINIMAL;
			putRoutingTable.put(message.getMessageId(), re);
			
			/* If TTL is over */
			if (message.decrTTL()) {
				re.ttlexpired = true;
			} else {
				re.TTL = message.TTL;
			}

			routeRequest(node_, pid_, re);
		}
	}


	/**
	 * Handles Get Messages.
	 *
	 * @param node_
	 * @param pid_
	 * @param event_
	 */
	protected void handleGetMessage(Node node_, int pid_, Object event_) {
		GetMessage message = (GetMessage) event_;
		GetRoutingEntry re = null;
		Content content = null;
		message.hopsDone++;

		if (distance(message.key, id).compareTo(message.bestDistance) < 0) {
			message.resetTTL();
			message.bestDistance = distance(message.key, id);
		}

		if ((content = StoreElements.get(message.getKey())) != null) {

			/**
			 * Whether the storing node of requested file is the starting node. In this case
			 * is considered a positive result and routing ends.
			 */
			if (message.getSender() == node_) {
				StatControl.numPositiveResponse++;
				return;
			}

			GetReply reply = new GetReply(node_, message.getSender(),
					message.getMessageId(), ResponseType.FOUND);
			reply.hopsDone = message.hopsDone;
			sendMessage(node_, reply, message.getPrevious(), pid_);
		}

		/**
		 * if routing has yet reached this node send not found.
		 */
		else if ((re = getRoutingTable.get(message.getMessageId())) != null) {
			GetReply reply = new GetReply(node_, message.getSender(),
					message.getMessageId(), ResponseType.NOTFOUND);
			reply.TTL = message.getTTL();
			reply.hopsDone = message.getHopsDone();
			sendMessage(node_, reply, message.getPrevious(), pid_);
		} else {
			re = new GetRoutingEntry(message, id, node_);
			getRoutingTable.put(message.getMessageId(), re);

			/* If TTL is over */
			if (message.decrTTL()) {
				re.ttlexpired = true;
			} else {
				re.TTL = message.TTL;
			}

			routeRequest(node_, pid_, re);
		}
	}

	/**
	 * Handles get reply messages.
	 *
	 * @param node_
	 * @param pid_
	 * @param event_
	 */
	protected void handleReplyGetMessage(Node node_, int pid_, Object event_) {
		GetReply message = (GetReply) event_;
		GetRoutingEntry re = getRoutingTable.get(message.getMessageId());
		re.update(message);
		if (message.getDest() == node_
				&& message.getResponseType() != ResponseType.NOTFOUND) {
			StatControl.handleResults(re, message.getResponseType());
		} else {
			routeRequest(node_, pid_, re);
		}
	}

	/**
	 * Handles Swap message when communication with other node has established.
	 *
	 * @param node_
	 * @param pid_
	 * @param event_
	 */
	protected void handleSwapMessage(Node node_, int pid_, Object event_) {
		SwapMessage message = (SwapMessage) event_;
		if (message.getType() == Type.Error) {
			swapstate = SwapState.noSwap;
			swapnode = null;
			return;
		}
		SwapRoutingEntry re = swapRoutingTable.get(message._messageId);
		if (message.dest != node_) {
			if (re == null) return;
			Node toSend = (message.getPrevious() == re.previous ? re.next
					: re.previous);

			sendMessage(node_, message, toSend, pid_);
		} else {
			switch (swapstate) {
				case noSwap:
					if (message.getType() == Type.Start) {
						// send swap message and change status
						SwapMessage sm = new SwapMessage(node_, node_, Type.Request, id);
						sm.neighborsIds = (Collection<BigInteger>) NeighborsIds.values();
						SwapRandomSearch searchMessage = new SwapRandomSearch(node_, sm);
						re = new SwapRoutingEntry();
						re.previous = node_;
						re.next = Neighbors.get(CommonState.r.nextInt(Neighbors.size()));
						swapRoutingTable.put(sm._messageId, re);
						searchMessage._messageId = sm._messageId;
						sendMessage(node_, searchMessage, re.next, pid_);
						swapstate = SwapState.sentRequest;
					} else if (message.getType() == Type.Request) {
						// Compute whether perform swap.
						SwapMessage sm;
						swapnode = message.getSender();
						if (checkSwap(message.getId(), message.neighborsIds)) {
							StatControl.swapResult(true);
							re = swapRoutingTable.get(message._messageId);
							/*
							* Put current id in SwapMessage to send and update id
							* with id received.
							*/
							sm = new SwapMessage(node_, swapnode, Type.Accept, id);
							id = message.getId();
							sm._messageId = message._messageId;
							addNearestContents(sm.getId(), sm);
							swapstate = SwapState.waitData;
						} else {
							StatControl.swapResult(false);
							sm = new SwapMessage(node_, swapnode, Type.Error, id);
							sm._messageId = message._messageId;
						}
						sendMessage(node_, sm, message.getPrevious(), pid_);
					}
					break;

				case sentRequest:

					if (message.getType() == Type.Accept) {

						swapnode = message.getSender();
						// send swap message and change status
						SwapMessage sm = new SwapMessage(node_, swapnode, Type.Accept, id);
						sm._messageId = message._messageId;
						id = message.getId();
						addNearestContents(sm.getId(), sm);
						if (message.getContentList() != null)
							for (Content content : message.getContentList()) {
								// If nodes has yet this content is sent back in order
								// to maintain replication factor.
								if (StoreElements.get(content) != null)
									sm.addContent(content);
								else
									store(content);
							}
						swapstate = SwapState.noSwap;
						sendMessage(node_, sm, message.getPrevious(), pid_);
						sendNewId(node_, pid_);
					} else {
						swapstate = SwapState.noSwap;
					}
					break;

				case waitData:
					if (message.getType() == Type.Accept) {
						// send swap message and change status
						if (message.getContentList() != null)
							for (Content content : message.getContentList())
								store(content);
						sendNewId(node_, pid_);
						swapstate = SwapState.noSwap;
					}
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Check if current node is a local minimal.
	 *
	 * @param id_
	 * @param previous
	 * @return Return true if current node is local minimal in current routing path.
	 */
	protected boolean isLocalMinimum(BigInteger id_, Node previous) {
		if (previous == null)
			return false; // if it is starting node

		Node node = findNearestNeighbor(id, Neighbors, true);
		if (node == null && distance(id_, NeighborsIds.get(previous)).compareTo(distance(id_, id)) > 0)
			return true;
		return false;
	}

	/**
	 *
	 * Common function to route message for both put and get requests. It retrieves message to send
	 * and destination from routing table and in failure case, get a reply and send it back.
	 * Decision like sending a message or send back a reply is taken by the routing table and the
	 * behavior is implemented differently in put and get using polymorphism.
	 *
	 * @param node_
	 * @param pid_
	 * @param re
	 */
	protected void routeRequest(Node node_, int pid_, RoutingEntry re) {
		List<Node> l = new ArrayList<Node>(Neighbors);
		l.removeAll(re.yetVisited);
		Node nodeToSend = findNearestNeighbor(re.key, l, re.routingStrategy());
		TTLMessage message = re.getMessage();
		if (nodeToSend != null && message != null) {
			sendMessage(node_, message, nodeToSend, pid_);
		} else {
			if (re.previous == null)
				return; /*change here. Check if routing ends at the first node.*/
			ReplyMessage reply = re.getReply();
			sendMessage(node_, reply, re.previous, pid_);
		}
	}

	/**
	 * Send new Id to all neighbors. Is useful after a swap operation.
	 *
	 * @param sender
	 * @param pid
	 */
	private void sendNewId(Node sender, int pid) {
		IdMessage message = new IdMessage(sender, id);
		for (Node n : Neighbors) {
			sendMessage(sender, message, n, pid);
		}
	}
	/**
	 * Method used in swap context. Attach content nearer to the other node of swap in outgoing message.
	 *
	 * @param id2
	 * @param ms
	 */
	private void addNearestContents(BigInteger id2, SwapMessage ms) {
		for (Content content : StoreElements.values()) {
			if (distance(id2, content.GetKey()).compareTo(
					distance(id, content.GetKey())) < 0) {
				ms.addContent(content);
				StoreElements.remove(content);
			}
		}
	}

	/**
	 * Store a new content.
	 * @param content_
	 */
	protected void store(Content content_) {
		StoreElements.put(content_.GetKey(), content_);
	}

	/**
	 * Add neighbor to neighbor list.
	 * @param n_
	 */
	public void AddNeighbor(Node n_) {
		Neighbors.add(n_);
	}

	/**
	 *
	 * @param id_
	 * Id to find.
	 * @param candidates_
	 * List of node to analyze.
	 * @param stopInLocalMin
	 * If true, return null if all candidates are farther than the
	 * current note.
	 * @return The nearest candidate from given id in candidate list.
	 */
	protected Node findNearestNeighbor(BigInteger id_, List<Node> candidates_, boolean stopInLocalMin) {
		Node candidate = null;
		BigInteger distance = (stopInLocalMin ? distance(id, id_)
				: new BigInteger(MAXID, MAX_ID_BIG_LENGTH));
		for (Node node : candidates_) {
			if (NeighborsIds.get(node) == null)
				continue;
			BigInteger d = distance(NeighborsIds.get(node), id_);
			if (distance.compareTo(d) > 0) {
				distance = d;
				candidate = node;
			}
		}
		return candidate;
	}

	/**
	 * Update id of a neighbor. Useful in swap context.
	 *
	 * @param node_
	 * @param id_
	 */
	public void updateNeighbor(Node node_, BigInteger id_) {
		NeighborsIds.put(node_, id_);
	}

	/**
	 * Retrieve the id of a node in the neighbor list.
	 *
	 * @param node_
	 * @return Id of given node.
	 */
	public BigInteger getNodeid(Node node_) {
		BigInteger i = NeighborsIds.get(node_);
		if (i == null)
			return BigInteger.ZERO;
		return i;
	}

	/**
	 * Distance between two ids.
	 *
	 * @param k1_
	 * @param k2_
	 * @return
	 */
	public static BigInteger distance(BigInteger k1_, BigInteger k2_) {
		return k1_.subtract(k2_).abs();
	}

	/**
	 * Send message checking whether parameters are correctly passed.
	 *
	 * @param node_
	 * @param message
	 * @param dest
	 * @param pid_
	 */
	protected void sendMessage(Node node_, FreenetMessage message, Node dest,
							   int pid_) {
		if (dest == null || node_ == null)
			throw new RuntimeException("Error in params");
		message.setPrevious(node_);
		EDSimulator.add(0, message, dest, pid_);
	}

	/**
	 * @param id2_ Id of potentially peer for swap.
	 *
	 * * @param id2Neighbors List of neighbors of the second peer
	 * @return True if swap must be performed, false otherwise.
	 */
	protected boolean checkSwap(BigInteger id2_,
								Collection<BigInteger> id2Neighbors) {
		BigInteger ndistance1 = BigInteger.ONE, ndistance2 =
				BigInteger.ONE, swappeddistance1 = BigInteger.ONE, swappeddistance2 = BigInteger.ONE;

		for (BigInteger idn : NeighborsIds.values()) {
			if (idn.compareTo(id2_) == 0)
				continue; // continue if neighbor refers to id2
			ndistance1 = ndistance1.multiply(id.subtract(idn).abs());
			swappeddistance2 = swappeddistance2.multiply(id2_.subtract(idn).abs());
		}
		for (BigInteger idn : id2Neighbors) {
			if (idn.compareTo(id) == 0)
				continue; // continue if neighbor refers to me
			ndistance2 = ndistance2.multiply(id2_.subtract(idn).abs());
			swappeddistance1 = swappeddistance1
					.multiply(id.subtract(idn).abs());
		}

		BigInteger normaldistance = ndistance1.multiply(ndistance2);
		BigInteger swappeddistance = swappeddistance1.multiply(swappeddistance2);

		if (normaldistance.compareTo(swappeddistance) > 0)
			return true;

		/*
		* Probabilistic check.
		*/
		if (swappeddistance.compareTo(BigInteger.ZERO) != 0)
			if (CommonState.r.nextFloat() < normaldistance.divide(swappeddistance).doubleValue())
				return true;
		return false;
	}

}
