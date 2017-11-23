package freenetsimulator;

import java.math.BigInteger;
import freenetsimulator.ReplyMessage.ResponseType;
import peersim.core.Node;

public class PutRoutingEntry extends RoutingEntry{

    public enum Phase {
        BEFORE_LOCAL_MINIMAL,
        LOCAL_MINIMAL,
        AFTER_LOCAL_MINIMAL
    }

    public PutRoutingEntry(PutSearchMessage message_, BigInteger id_, Node node_) {
        super(message_, id_, node_);
        pathLenght = 0;
        localMinimum = false;
        found = false;
        ttlexpired = false;
        stopRouting = false;

        phase = message_.phase;
        // this is uset to evaluate when reset TTL TODO toglierE?
        bestDistance = message_.bestDistance;
        // this is used to evaluate put routing.
        nodeFoundDistance = CommonFunctions.distance(id_, message_.getKey());
    }

    /**
     * Used to distinguish the phase of put discovery.
     */
    protected Phase phase;

    /**
     * Value of Best node.
     */
    public BigInteger nodeFoundDistance;

    /**
     * Path length for best node.
     */
    public int pathLenght;

    /**
     * this value is used to route put operation to best node previously computed.
     */
    public Node routeForPut;

    /**
     * Flag uses to put protocol to check if node is a local minimum.
     */
    public boolean localMinimum;

    /**
     * Update the routing table with information leaded by reply.
     * @param reply
     */
    public void update(PutReply reply){
        updateCommon(reply);
        //stopRouting = true;
        if (reply.getResponseType() == ResponseType.TTlEXPIRED){
            ttlexpired = true;
        } else if (reply.getResponseType() == ResponseType.FOUND){
            found = true;
        }
        yetVisited.add(reply._previous);
        if (reply.nodeFound != null && reply.nodeFoundDistance.compareTo(nodeFoundDistance) < 0 ){
            pathLenght = reply.pathlenght + 1;
            routeForPut = reply._previous;
            nodeFoundDistance = reply.bestDistance;
            nodeFound = reply.nodeFound;
        }
    }

    @Override
    public ReplyMessage getReply() {
        PutReply reply = new PutReply(currentNode, sender, pathLenght, (found? ResponseType.FOUND :
                (ttlexpired? ResponseType.TTlEXPIRED : ResponseType.NOTFOUND)));
        compileReply(reply);
        reply.nodeFound = nodeFound;
        reply.nodeFoundDistance = nodeFoundDistance;
        reply.pathlenght = pathLenght;
        reply._messageId = id;
        reply.TTL = TTL;
        return reply;
    }

    @Override
    public TTLMessage getMessage() {
        
    /*
    * In this cases no message has to be forwarded.*/

        /* If is local minimal put search message must be sent to each neighbor.
        * In this implementation messages are sent sequentially.
        *
        * */
        if (phase != Phase.LOCAL_MINIMAL)
            if (ttlexpired)
                return null;
        if (found || stopRouting)
            return null;
        PutSearchMessage message = new PutSearchMessage(sender, key);
        message.phase = (phase == Phase.LOCAL_MINIMAL? Phase.AFTER_LOCAL_MINIMAL : phase);
        message._messageId = id;
        message.setTTL(TTL);
        message.bestDistance = bestDistance;
        return message;
    }

    @Override
    public boolean routingStrategy() {
        return false;
    }

}