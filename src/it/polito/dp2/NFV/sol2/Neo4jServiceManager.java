package it.polito.dp2.NFV.sol2;

import javax.ws.rs.ProcessingException;
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

	// this method send the node informations to the server and return the node ID
	protected String postNode(Node node, Labels labels) throws ServiceException {
		try {
			serverResponse = client.target(JAXClientManager.getBaseURI().path("/node").build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(node));

			checkResponse(serverResponse);
			Node nodeResponse = serverResponse.readEntity(Node.class);
			String nodeID = nodeResponse.getId();

			serverResponse = client.target(JAXClientManager.getBaseURI().path("/node/" + nodeID + "/labels").build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(labels));

			checkResponse(serverResponse);
			return nodeID;
		} catch (ProcessingException | IllegalStateException e) {
			throw new ServiceException("JAX-RS client has raised an exception: " + e.getMessage());
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
	protected String postRelationship(Relationship rel) throws ServiceException {
		try {
			String nodeID = rel.getSrcNode();                                                                                    // get the source node from the relation instance
			serverResponse = client.target(JAXClientManager.getBaseURI().path("/node/" + nodeID + "/relationships").build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.post(Entity.xml(rel));

			checkResponse(serverResponse);
			return serverResponse.readEntity(Relationship.class).getId();
		} catch (IllegalStateException | IllegalArgumentException | ProcessingException e) {
			throw new ServiceException("JAX-RS client has raised an exception: " + e.getMessage());
		}
	}

	// return all the host reachable by the specified node
	protected Nodes getReachableHost(String nodeID) throws ServiceException {
		try {
			serverResponse = client.target(JAXClientManager.getBaseURI()
					.path("/node/" + nodeID + "/reachableNodes")
					.queryParam("nodeLabel", "Host")
					.build())
					.request()
					.accept(MediaType.APPLICATION_XML)
					.get();

			checkResponse(serverResponse);
			return serverResponse.readEntity(Nodes.class);
		} catch (ProcessingException | IllegalArgumentException | IllegalStateException e) {
			throw new ServiceException("JAX-RS raised an exception: " + e.getMessage());
		}
	}

	// close the instance of the jax rs client
	protected void closeClient() {
		client.close();
	}

	// check the response of the server and raise a service exception if the the status code is not 200
	private void checkResponse(Response res) throws ServiceException {
		Response.StatusType resStatus = res.getStatusInfo();
		int statusCode = resStatus.getStatusCode();
		if (statusCode >= 400 && statusCode <= 599)
			throw new ServiceException("server returned an error: " + resStatus.getStatusCode() + " " + resStatus.getReasonPhrase());
	}
}
