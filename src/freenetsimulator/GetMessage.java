package freenetsimulator;
import java.math.BigInteger;
import peersim.config.Configuration;
import peersim.core.Node;

public class GetMessage extends TTLMessage{
    protected GetMessage(Node sender_, BigInteger key_) {
        super(sender_, key_);
    }

    @Override
    public void resetTTL() {
        TTL = Configuration.getInt("GET_TTL");
    }
}