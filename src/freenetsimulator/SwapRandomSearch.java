package freenetsimulator;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;

public class SwapRandomSearch extends FreenetMessage {
    public static int HTLMAX_DEFAULT = Configuration.getInt("SWAPHTLMAX");
    public static int HTLMIN_DEFAULT = Configuration.getInt("SWAPHTLMIN");
    public int HTL;
    public SwapMessage sm;
    public int distance;

    public SwapRandomSearch(Node sender_, SwapMessage sm_) {
        super(sender_);
        HTL = HTLMIN_DEFAULT + CommonState.r.nextInt(HTLMAX_DEFAULT - HTLMIN_DEFAULT);
        sm = sm_;
        distance = 0;
    }
}