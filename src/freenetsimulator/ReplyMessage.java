package freenetsimulator;

import java.math.BigInteger;
import peersim.core.Node;

public class ReplyMessage extends FreenetMessage{

    public enum ResponseType {
        FOUND,
        TTlEXPIRED,
        NOTFOUND
    }

    public ResponseType _type;

    public BigInteger bestDistance;

    /**
     * Response message are routed in order to reach destination node.
     20 * This field is used to check if the current node is destination node.
     */
    public Node _dest;

    public int TTL;

    public ResponseType getResponseType(){
        return _type;
    }

    public ReplyMessage(Node sender_, Node dest_, long id, ResponseType type_) {
        super(sender_);
        _dest = dest_;
        _messageId = id;
        _type = type_;

    }

    public Node getDest(){
        return _dest;
    }
}