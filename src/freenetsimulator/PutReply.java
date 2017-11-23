package freenetsimulator;

import java.math.BigInteger;
import peersim.core.Node;

public class PutReply extends ReplyMessage {
    public Node nodeFound;
    public int pathlenght;
    public BigInteger nodeFoundDistance;

    public PutReply(Node sender_, Node dest_, long id, ResponseType type) {
        super(sender_, dest_, id, type);
        nodeFound = null;
        bestDistance = null;
        nodeFoundDistance = null;
        pathlenght = 0;
    }

}
