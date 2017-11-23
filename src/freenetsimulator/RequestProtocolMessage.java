package freenetsimulator;

import peersim.core.Node;

public class RequestProtocolMessage extends FreenetMessage {

	public enum OperType {
		get,
		put
	}
	
	public RequestProtocolMessage(Node sender_, OperType op_){
		super(sender_);
		opType = op_;
	}
	
	protected OperType opType;
	public OperType getOpType(){
		return opType;
	}
	
	
}
