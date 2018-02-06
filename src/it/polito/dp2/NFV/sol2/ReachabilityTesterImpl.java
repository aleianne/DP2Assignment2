package it.polito.dp2.NFV.sol2;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NfvReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.lab2.AlreadyLoadedException;
import it.polito.dp2.NFV.lab2.ExtendedNodeReader;
import it.polito.dp2.NFV.lab2.NoGraphException;
import it.polito.dp2.NFV.lab2.ReachabilityTester;
import it.polito.dp2.NFV.lab2.ReachabilityTesterException;
import it.polito.dp2.NFV.lab2.ServiceException;
import it.polito.dp2.NFV.lab2.UnknownNameException;

public class ReachabilityTesterImpl implements ReachabilityTester {
	
	private NfvReader monitor;																		// interface used to read the data

	private JAXClientManager clientManager;
	private NffgSetup nffgLoader;
	
	protected ReachabilityTesterImpl(NfvReader monitor) throws ReachabilityTesterException {											// instantiate a new object factory
		this.monitor = monitor;
		
		nffgLoader = new NffgSetup();
		
		// define the clientManager in order to return the intance of the client at every invocation of the clientManager
		clientManager = new JAXClientManager();
		
		System.out.println("ReachabilityTester instantiated");
	}

	@Override
	public void loadGraph(String nffgName) throws UnknownNameException, AlreadyLoadedException, ServiceException {
	
		NffgReader nfgr;
		
		if (nffgName == null) 
			throw new UnknownNameException("the graph passed as argument is null");															// throw a new exception if the nffg name is null
		
		if ((nfgr = monitor.getNffg(nffgName)) == null)
			throw new UnknownNameException("the graph passed as argument cannot be found inside the NfvReader interface");					// throw a new exception if the nffg doesn't exist in the readere interface
		
		if (nffgLoader.searchNffg(nffgName)) 
			throw new AlreadyLoadedException("the graph is already loaded into the server");												// if the nffg list contains the nffg name throw a new already loaded exception
		
		try {
			
			System.out.println("begin to send nffg-nodes to the server\n");
			nffgLoader.sendNffgNodes(client, nfgr);
			
			System.out.println("begin to forward the NFFG relationships to the server\n");						// get the relationships from the interface, pass as parameter the nodeID map for the destination nodes
			nffgLoader.sendNffgRelationships(client, nfgr);
		
			System.out.println("Nffg nodes correctly stored in remote DB\n");
			
		} catch(ServiceException se) {
			// close the client and re throw this exception outside the library
			client.close();
			throw se;
		}
		
	}	

	@Override
	public Set<ExtendedNodeReader> getExtendedNodes(String nffgName)
			throws UnknownNameException, NoGraphException, ServiceException {
		
		NffgReader nfgr;	
		
		if (nffgName == null) 
			throw new UnknownNameException("the name passed as argument is null");
		
		if ((nfgr = monitor.getNffg(nffgName)) == null ) 
			throw new UnknownNameException("the graph doesn't exist");
		
		if (nffgLoader.searchNffg(nffgName)) 
			throw new NoGraphException("the graph is not loaded into the database");
		
		Map<String, String> nodeIDmap = nffgLoader.getNodeIDmap(nffgName);
		Set<ExtendedNodeReader> extendedNRset = new HashSet<ExtendedNodeReader> ();
		
		// define the entry point of the JAX-RS framework
		Client client = clientManager.getClientInstance();		
		
		// for each element in the map get the extended node
		for (String nodeName: nodeIDmap.keySet()) {
			
			NodeReader node =  nfgr.getNode(nodeName);								// get the nodereader information about the specified node
			String nodeID = nodeIDmap.get(nodeName);								// get the node id of the node
			
			// check if the node is null or not
			if (node == null || nodeID == null) {
				client.close();
				throw new ServiceException("the node not correspond to any node contained into the interface");
			}
			
			Response res;
			
			try {
				//String targetResource = "http://localhost:8080/Neo4JSimpleXML/webapi/data/node/" + nodeID + "/reachableNodes?nodeLabel=Host";
				res = client.target(JAXClientManager.getBaseURI()
													.path("/node/" + nodeID)
													.queryParam("nodeLabel", "Host")
													.build())
						.request()
						.accept(MediaType.APPLICATION_XML)
						.get();
				
			} catch(IllegalArgumentException ie) {
				client.close();
				throw new ServiceException("JAX-RS returned an exception: " + ie.getMessage());
			}
			
			StatusType resStatus = res.getStatusInfo();
			if (resStatus.getStatusCode() == 200) {
				System.out.println("response for node " + nodeID + "received correclty");
			} else {
				//close the client and then raise a new ServiceException
				client.close();
				throw new ServiceException("server returned an error: " + resStatus.getStatusCode() + " " + resStatus.getReasonPhrase());
			}
			
			try {
				
				Nodes nodes = res.readEntity(Nodes.class);
				extendedNRset.add(new ExtendedNodeImpl(node, getHostSet(nodes)));
				
			} catch(ServiceException se) {
				client.close();
				throw se;
			}
			
		}
		
		return extendedNRset;
	}

	@Override
	public boolean isLoaded(String nffgName) throws UnknownNameException {
			
		if (nffgName == null) 
			throw new UnknownNameException();							
		
		return (nffgLoader.searchNffg(nffgName));
	}

	// return the hostSet starting from the 
	private Set<HostReader> getHostSet(Nodes nodes) throws ServiceException {
		
		List<Node> nodeList = nodes.getNode();
		Set<HostReader> hostSet = new HashSet<HostReader> ();
		
		if (nodeList.isEmpty()) {
			System.out.println("server returned an empty list of nodes");
			return hostSet;
		}
		
		for (Node n: nodeList) {																// get hostname for each node in the response
			String hostName = n.getProperties().getProperty().get(0).getValue();
			HostReader hr;
		
			if ((hr = monitor.getHost(hostName)) == null) {
				throw new ServiceException("the hostname returned by the server doesn't exist into nfv reader interface");
			}
			
			hostSet.add(hr);
		}
		
		return hostSet;
	}
		
}
