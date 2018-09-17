package it.polito.dp2.NFV.sol2;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

import it.polito.dp2.NFV.lab2.ReachabilityTesterException;
import it.polito.dp2.NFV.lab2.ServiceException;

public class JAXClientManager {
	
	private static Client JAXClient;
	private static JAXClientManager clientManager = new JAXClientManager();
	private static final String clientPropertyName = "it.polito.dp2.NFV.lab2.URL";
	private static String clientURL;
	
	protected JAXClientManager() {
		JAXClient = ClientBuilder.newClient();
		System.out.println("JAX-RS client created");
		clientURL = System.getProperty(clientPropertyName);
	}
	
	protected static UriBuilder getBaseURI() {
		return UriBuilder.fromUri(clientURL + "/data");
	}
	
	protected static Client getClientInstance() {
		return JAXClient;
	}
}
