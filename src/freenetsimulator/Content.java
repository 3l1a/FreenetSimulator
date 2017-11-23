package freenetsimulator;

import java.math.BigInteger;
import java.util.Random;
import peersim.core.CommonState;

public class Content {

	public BigInteger key;
	public BigInteger GetKey(){
		return key;
	}

	public Content() {
		key = new BigInteger(128, CommonState.r);
	}

	public Content(BigInteger key_){
		key = key_;
	}
}