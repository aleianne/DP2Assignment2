package it.polito.dp2.NFV.sol2;

import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NfvReaderException;
import it.polito.dp2.NFV.NfvReaderFactory;
import it.polito.dp2.NFV.lab2.ReachabilityTester;
import it.polito.dp2.NFV.lab2.ReachabilityTesterException;

public class ReachabilityTesterFactory extends it.polito.dp2.NFV.lab2.ReachabilityTesterFactory {
	
	@Override
	public ReachabilityTester newReachabilityTester() throws ReachabilityTesterException {
		try {
			NfvReader monitor = NfvReaderFactory.newInstance().newNfvReader();							// instantiate a new NFV reader
			return new ReachabilityTesteImpl(monitor);
		} catch(NfvReaderException ne) {		
			throw new ReachabilityTesterException();
		}
	}
	
}
