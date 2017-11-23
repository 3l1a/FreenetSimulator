package freenetsimulator;

import java.math.BigInteger;

import peersim.core.Node;

public class ResponseMessage extends RequestProtocolMessage{
	
	public enum ResponseType {
		ok,
		TTlExpired,
        visited,
		Found
	}

	private ResponseType _type;
	
	/**
	 * Response message are routed in order to reach destination node.
	 * This field is used to check if the current node is destination node.
	 */
	private Node _dest;
	
	
	public Node nodeFound;
	public BigInteger nodeFoundProximity;
	public int pathlenght;
	
	/**
	 * Content retrieved by get.
	 */
	public Content content;
	
	/**
	 * These information are useful to bring statistics backward in routing path. 
	 */
	public int hopsDone;
	public int TTL;
	
	
	public ResponseType getResponseType(){
		return _type;
	}
	
	public ResponseMessage(Node sender_, Node dest_, long id, ResponseType type_, OperType opType_) {
		super(sender_, opType_);
		_dest = dest_;
		_messageId = id;
		_type = type_;
		nodeFound = null;
		nodeFoundProximity = null;
		pathlenght = 0;
		content = null;
		hopsDone = 0;
		TTL = 0;
	}
	
	public Node getDest(){
		return _dest;
	}
}
