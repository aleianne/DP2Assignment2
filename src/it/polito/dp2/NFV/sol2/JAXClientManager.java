package it.polito.dp2.NFV.sol2;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

import it.polito.dp2.NFV.lab2.ReachabilityTesterException;
import it.polito.dp2.NFV.lab2.ServiceException;

public class JAXClientManager {
	
	private static Client client;
	
	private static JAXClientManager clientManager = new JAXClientManager();
	
	protected JAXClientManager() {
		// instantiate a new JAX-RS client
		client = ClientBuilder.newClient();
		System.out.println("JAX-RS client created");
	}
	
	protected static UriBuilder getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/Neo4JSimpleXML/webapi/data/");
	}
	
	protected  static Client getClientInstance() throws ServiceException {
		if(client == null) 
			throw new ServiceException("impossible to establish a connection with the server");
		else 
			return client;
	}
	
}
