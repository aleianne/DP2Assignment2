package it.polito.dp2.NFV.sol2;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.polito.dp2.NFV.lab2.ServiceException;

public class Neo4jServiceManager {
	
	private Client client;
	private Response serverResponse;
	
	protected Neo4jServiceManager() {
		client = JAXClientManager.getClientInstance();
	}

	// this method send the node information to the server and return the node ID
	protected String postNode(Node node, Labels labels) throws ServiceException {
		try {
			serverResponse = client.target(JAXClientManager.getBaseURI().path("node").build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(node));
			
			checkResponse(serverResponse);														// check the response of the server
			Node nodeResponse = serverResponse.readEntity(Node.class);							// convert the response into a Node class instance
			String nodeID = nodeResponse.getId();	
			
			serverResponse= client.target(JAXClientManager.getBaseURI().path("node/" + nodeID + "/labels").build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(labels));
			
			checkResponse(serverResponse);
			return nodeID;																	// return the node id received from the server			
		} catch(ResponseProcessingException | IllegalStateException e) {
			client.close();
			throw new ServiceException("JAX-RS client raised an exception: " + e.getMessage());
		}
	}
	
	// this method is used to post a set of labels for a specific node
	/*protected Response postLabels(Labels labels, String nodeID) throws ServiceException {
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
		
	}*/
	
	// used to send the relationship between two nodes
	protected Response postRelationship(Relationship rel) throws ServiceException {
		try {
			String nodeID = rel.getSrcNode();
			serverResponse = client.target(JAXClientManager.getBaseURI().path("node/" + nodeID + "/relationship").build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(rel));
			
			checkResponse(serverResponse);
			return serverResponse;
		} catch(ResponseProcessingException pe) {
			client.close();
			throw new ServiceException("JAX-RS client raised an exception: " + pe.getMessage());
		} catch(NullPointerException npe) {
			client.close();
			throw new ServiceException("impossible to invoke post, the argument is null");
		}
	}
	
	// return all the host reachable by the specified node
	protected Response getReachableHost(String nodeID) throws ServiceException {
		try {
			serverResponse = client.target(JAXClientManager.getBaseURI()
												.path("/node/" + nodeID)
												.queryParam("nodeLabel", "Host")
												.build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.get();
			
			checkResponse(serverResponse);
			return serverResponse;
		} catch(IllegalArgumentException ie) {
			client.close();
			throw new ServiceException("JAX-RS returned an exception: " + ie.getMessage());
		}
	}
	
	// check the response of the server and raise a service exception if the the status code is not 200
	private void checkResponse(Response res) throws ServiceException {
		Response.StatusType resStatus = res.getStatusInfo();
		if (resStatus.getStatusCode() != 200 &&
				resStatus.getStatusCode() != 201 &&
				resStatus.getStatusCode() != 204) {
			client.close();
			throw new ServiceException("server returned an error: " + resStatus.getStatusCode() + " " + resStatus.getReasonPhrase());
		}
	}
}