package freenetsimulator;

import java.math.BigInteger;
import freenetsimulator.ReplyMessage.ResponseType;
import peersim.core.Node;

public class GetRoutingEntry extends RoutingEntry{
    public int hopsDone;

    public GetRoutingEntry(GetMessage message_, BigInteger id_, Node node_) {
        super(message_, id_, node_);
        bestDistance = message_.bestDistance;
        hopsDone = message_.hopsDone;
        found = false;
        ttlexpired = false;
    }

    public void update(GetReply reply){
        updateCommon(reply);
        yetVisited.add(reply._previous);
        if (reply.getResponseType() == ResponseType.FOUND)
            found = true;
        else if (reply.getResponseType() == ResponseType.TTlEXPIRED)
            ttlexpired = true;
        hopsDone = reply.hopsDone;
    }

    @Override
    public ReplyMessage getReply() {
        GetReply reply = new GetReply(currentNode, sender, id,
                (found? ResponseType.FOUND : (ttlexpired? ResponseType.TTlEXPIRED : ResponseType.NOTFOUND)));
        compileReply(reply);
        reply.hopsDone = hopsDone;
        reply.TTL = TTL;
        return reply;
    }

    @Override
    public TTLMessage getMessage() {
        if (ttlexpired || found)
            return null;
        GetMessage message = new GetMessage(sender, key);
        message.TTL = TTL;
        message.bestDistance = bestDistance;
        message._messageId = id;
        message.hopsDone = hopsDone;
        return message;
    }

    @Override
    public boolean routingStrategy() {
        return false;
    }

}
