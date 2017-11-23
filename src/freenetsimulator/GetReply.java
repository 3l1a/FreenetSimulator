package freenetsimulator;

import java.math.BigInteger;
import peersim.core.Node;

public class GetReply extends ReplyMessage {

    /**
     * These information are useful to bring statistics backward in routing path.
     10 */
    public int hopsDone;

    /**
     * Content retrieved by get.
     15 */
    public Content content;

    public GetReply(Node sender_, Node dest_, long id, ResponseType type) {
        super(sender_, dest_, id, type);
        bestDistance = new BigInteger(FreenetProtocol.MAXID, 16);
        content = null;
        hopsDone = 0;
    }
}