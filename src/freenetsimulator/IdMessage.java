package freenetsimulator;

import java.math.BigInteger;
import peersim.core.Node;

public class IdMessage extends FreenetMessage{
	private BigInteger id;

	public IdMessage(Node sender_, BigInteger id_) {
		super(sender_);
		id = id_;
	}

	public BigInteger getId(){
		return id;
	}
 }