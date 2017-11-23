package freenetsimulator;

import peersim.core.CommonState;
import peersim.core.Node;

public class PutMessage extends FreenetMessage {
	public int replication;
	private Content content;

	public PutMessage(Node sender_, Content content_) {
		super(sender_);
		_messageId = CommonState.r.nextLong();
		content = content_;
		replication = FreenetProtocol.replication;
	}

	public PutMessage( Node sender_, Long id_, Content content_) {

		super(sender_);
		content = content_;
		_messageId = id_;
		replication = FreenetProtocol.replication;
	}

	public Content getContent() {
		return content;
	}

}