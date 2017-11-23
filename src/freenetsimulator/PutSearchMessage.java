package freenetsimulator;
import java.math.BigInteger;
import freenetsimulator.PutRoutingEntry.Phase;
import peersim.config.Configuration;
import peersim.core.Node;
public class PutSearchMessage extends TTLMessage{

    private static int defaultTTL = Configuration.getInt("PUT_TTL");
    PutRoutingEntry.Phase phase;

    public PutSearchMessage(Node sender_, BigInteger key_) {
        super(sender_, key_);
        phase = Phase.BEFORE_LOCAL_MINIMAL;
    }

    @Override
    public void resetTTL() {
        TTL = defaultTTL;
    }
}