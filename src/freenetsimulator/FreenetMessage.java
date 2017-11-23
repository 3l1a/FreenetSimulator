package freenetsimulator;

import peersim.core.CommonState;
import peersim.core.Node;

public abstract class FreenetMessage {
	protected long _messageId;
	protected Node _sender;
	protected Node _previous;

	public void setPrevious(Node node_){
		_previous = node_;
	}

	public Node getPrevious(){
		return _previous;
	}

	public FreenetMessage(Node sender_) {
		_messageId = CommonState.r.nextLong();
		_sender = sender_;
	}

	public Node getSender(){
		return _sender;
	}

	public long getMessageId(){
		return _messageId;
	}
}