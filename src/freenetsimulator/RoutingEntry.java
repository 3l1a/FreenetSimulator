package freenetsimulator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import peersim.core.Node;

public abstract class RoutingEntry {

	public boolean found;
	public boolean ttlexpired;
	public Node currentNode;

	/**
	 * Id of processed message.
	 */
	public long id;

	/**
	 * Previous node in searching routing.
	 */
	public Node previous;

	/**
	 * Key of searching.
	 */
	public BigInteger key;

	/**
	 * List of node yet visited.
	 */
	public List<Node> yetVisited;

	/**
	 * Best node for store content.
	 */
	public Node nodeFound;

	/**
	 * TTL of message.
	 */
	public int TTL;

	/**
	 * Sender of request.
	 */
	public Node sender;

	/**
	 * Used in put routing.
	 */
	public boolean sendTTL;

	/**
	 * Distance of best node found.
	 */
	public BigInteger bestDistance;

	/**
	 * Flag used to stop routing.
	 */
	protected boolean stopRouting;

	/**
	 *
	 * @param id_
	 * @param previous_
	 * @param key_
	 */
	public RoutingEntry(long id_, Node previous_, BigInteger key_){
		id = id_;
		previous = previous_;
		yetVisited = new ArrayList<Node>();
		key = key_;
		sender = null;
		ttlexpired = false;
		found = false;
	}

	/**
	 *
	 * @param message_
	 * @param id_
	 * @param node_
	 */
	public RoutingEntry(TTLMessage message_, BigInteger id_, Node node_){
		currentNode = node_;
		id = message_.getMessageId();
		TTL = message_.getTTL();
		previous = message_.getPrevious();
		yetVisited = new ArrayList<Node>();
		yetVisited.add(message_.getPrevious());
		key = message_.getKey();
		sender = message_.getSender();
		nodeFound = node_;
		sendTTL = false;
	}

	/**
	 * This method encapsulates common operation of updating. This procedure is called by update method of instance
	 * of this abstract class.
	 * @param message
	 */
	protected void updateCommon(ReplyMessage message){

		if (message.bestDistance != null && message.bestDistance.compareTo(bestDistance) < 0){
			bestDistance = message.bestDistance;
		}
		TTL = message.TTL;
	}

	/**
	 * Common routing for compile reply. This is called by getReply method in instantiated class.
	 * @param message
	 */
	public void compileReply(ReplyMessage message){
		message.bestDistance = bestDistance;
		message.TTL = TTL;
	}

	/**
	 * Get a reply message to send back in routing path.
	 * @return
	 */
	public abstract ReplyMessage getReply();

	/**
	 * Get message to send in routing. This procedure returns null if no message must be sent.
	 * @return
	 */
	public abstract TTLMessage getMessage();
	public abstract boolean routingStrategy();
}
		