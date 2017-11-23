package freenetsimulator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import peersim.core.Node;

public class SwapMessage extends IdMessage{

	private List<Content> contentList;
	private Type type;
	public Node dest;
	public Collection<BigInteger> neighborsIds;

	enum Type{
		Request,
		Accept,
		Start,
		Error
	}

	public SwapMessage(Node sender_, Node dest_, SwapMessage.Type type_, BigInteger id_) {
		super(sender_, id_);
		contentList = null;
		neighborsIds = null;
		type = type_;
		dest = dest_;
	}

	public Type getType(){
		return type;
	}

	public void addContent(Content content_){
		if (contentList == null)
		contentList = new ArrayList<Content>();
		contentList.add(content_);
	}

	public List<Content> getContentList(){
		return contentList;
	}
}