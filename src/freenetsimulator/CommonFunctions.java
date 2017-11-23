package freenetsimulator;

import java.math.BigInteger;
import peersim.core.CommonState;

public class CommonFunctions {
    public static BigInteger distance(BigInteger d1, BigInteger d2){
        return d1.subtract(d2).abs();
    }

    public static int randomInt(int value){
        return CommonState.r.nextInt(value);
    }
}