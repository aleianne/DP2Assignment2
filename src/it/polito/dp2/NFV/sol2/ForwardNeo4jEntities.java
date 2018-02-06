package it.polito.dp2.NFV.sol2;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.polito.dp2.NFV.lab2.ServiceException;

public class ForwardNeo4jEntities {
	
	private Client client;
	
	protected ForwardNeo4jEntities(Client client) {
		this.client = client;
	}

	// this method send the node information to the server and return the node ID
	protected String postNode(Node node) throws ServiceException {
	
		try {
			Response res = client.target(JAXClientManager.getBaseURI().path("node").build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(node));
			
			checkResponse(res);														// check the response of the server
				
			Node nodeResponse = res.readEntity(Node.class);							// convert the response into a Node class instance
			String nodeID = nodeResponse.getId();	
		
			return nodeID;															// return the node id received from the server			
				
		} catch(ResponseProcessingException | IllegalStateException e) {
			// raise a new ServiceException
			throw new ServiceException("JAX-RS client raised an exception: " + e.getMessage());
		}
 		
	}
	
	// this method is used to post a set of labels for a specific node
	protected Response postLabels(Labels labels, String nodeID) throws ServiceException {
		
		try {
			Response res = client.target(JAXClientManager.getBaseURI().path("node/" + nodeID + "/labels").build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(labels));
			
			checkResponse(res);
			return res;
		} catch(ResponseProcessingException pe) {
			// raise a new ServiceException
			throw new ServiceException("JAX-RS client raised an exception: " + pe.getMessage());
		} catch(NullPointerException npe) {
			throw new ServiceException("impossible to invoke post, the argument is null");
		}
		
	}
	
	// used to send the relationship between two nodes
	protected Response postRelationship(Relationship rel, String nodeID) throws ServiceException {
		
		try {
			Response res = client.target(JAXClientManager.getBaseURI().path("node/" + nodeID + "/relationship").build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(rel));
			
			checkResponse(res);
			return res;
		} catch(ResponseProcessingException pe) {
			throw new ServiceException("JAX-RS client raised an exception: " + pe.getMessage());
		} catch(NullPointerException npe) {
			throw new ServiceException("impossible to invoke post, the argument is null");
		}
		
	}
	
	// check the response of the server and raise a service exception if the the status code is not 200
	private void checkResponse(Response res) throws ServiceException {
		Response.StatusType resStatus = res.getStatusInfo();
		
		if (resStatus.getStatusCode() == 200 || resStatus.getStatusCode() == 201 || resStatus.getStatusCode() == 204) {
			System.out.println("Server response correctly");
		} else {
			throw new ServiceException("server returned an error: " + resStatus.getStatusCode() + " " + resStatus.getReasonPhrase());
		}
	}
}
