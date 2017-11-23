package freenetsimulator;
import java.math.BigInteger;
import peersim.config.Configuration;
import peersim.core.Node;

/**
 10 *
 * @author eliap_000
 *
 * Represents messages with TTL in Freenet Protocol.
 * This kind of message has key and TTL fields.
15 * TTLMessage is used for forward either get or put request.
 */
public abstract class TTLMessage extends FreenetMessage{

    protected int TTL;
    protected static int defaultTTL = Configuration.getInt("GET_TTL");
    protected static int putdefaultTTL = Configuration.getInt("PUT_TTL");
    protected BigInteger key;
    protected BigInteger bestDistance;
    public int hopsDone;

    public void incHops(){
        hopsDone++;
    }

    public int getHopsDone(){
        return hopsDone;
    }

    protected TTLMessage(Node sender_, BigInteger key_){
        super(sender_);
        resetTTL();
        key = key_;
        bestDistance = new BigInteger(FreenetProtocol.MAXID, 16);
    }

    public BigInteger getKey(){
        return key;
    }

    /**
     * Decrements TTL value.
     * @return After decrementing, return true if TTL has reached 0, false otherwise.
     */
    public boolean decrTTL(){
        if (TTL <= 0) return true;
        TTL --;
        return false;
    }

    /**
     * reset TTL to default value.
     */
    public abstract void resetTTL();

    /**
     * Set TTL with specific value.
     * @param ttl_
     */
    public void setTTL(int ttl_){
        TTL = ttl_;
    }

    /**
     * Get TTL of message.
     70 * @return
     */
    public int getTTL(){
        return TTL;
    }
}
		