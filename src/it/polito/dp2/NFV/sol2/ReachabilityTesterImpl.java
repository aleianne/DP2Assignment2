package it.polito.dp2.NFV.sol2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.LinkReader;
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
	
	// interface used to read the data
	private NfvReader monitor;																		

	// string used to create nffg's node property 
	private final static String nffgLabelValue = "Node";
	private final static String nffgpropertyNameValue = "name";
	private final static String nffgrelType = "ForwardTo";
		
	// string used to create host's node property 
	private final static String hostLabelValue = "Host";
	private final static String hostpropertyNameValue = "name";
	private final static String hostrelType = "AllocatedOn";
	
	private ClientState clientState;
	
	protected ReachabilityTesterImpl(NfvReader monitor) throws ReachabilityTesterException {											// instantiate a new object factory
		this.monitor = monitor;
		clientState = new ClientState();
	}

	@Override
	public void loadGraph(String nffgName) 
			throws UnknownNameException, AlreadyLoadedException, ServiceException {
		
		NffgReader nfgr;
		
		if (nffgName == null) 
			throw new UnknownNameException("the graph passed as argument is null");															// throw a new exception if the nffg name is null
		
		if ((nfgr = monitor.getNffg(nffgName)) == null)
			throw new UnknownNameException("the graph passed as argument cannot be found inside the NfvReader interface");					// throw a new exception if the nffg doesn't exist in the readere interface
		
		if (clientState.graphIsForwarded(nffgName)) 
			throw new AlreadyLoadedException("the graph is already loaded into the server");												// if the nffg list contains the nffg name throw a new already loaded exception
		
		Neo4jServiceManager neo4jService = new Neo4jServiceManager();
		Map<String, String> nodeIDmap = new HashMap<String, String> ();
		
		System.out.println("begin to send nffg-nodes to the server\n");
		sendNffgNodes(nfgr, neo4jService, nodeIDmap);
		
		System.out.println("begin to forward the NFFG relationships to the server\n");														// get the relationships from the interface, pass as parameter the nodeID map for the destination nodes
		sendNffgRelationships(nfgr, neo4jService, nodeIDmap);
		
		System.out.println("Nffg nodes correctly stored in remote DB\n");
		clientState.setGraphMap(nffgName, nodeIDmap);
	}	

	@Override
	public Set<ExtendedNodeReader> getExtendedNodes(String nffgName)
			throws UnknownNameException, NoGraphException, ServiceException {
		
		NffgReader nfgr;
		
		if (nffgName == null) 
			throw new UnknownNameException("the name passed as argument is null");
		
		if ((nfgr = monitor.getNffg(nffgName)) == null ) 
			throw new UnknownNameException("the graph doesn't exist");
		
		Set<String> nodeSet = clientState.getNodeSet(nffgName);
		
		if(nodeSet == null || nodeSet.isEmpty())
			throw new NoGraphException("the graph is not loaded into the DataBase");
		
		Set<ExtendedNodeReader> extendedNRset = new HashSet<ExtendedNodeReader> ();
		Set<HostReader> hostSet;
		Neo4jServiceManager neo4jService = new Neo4jServiceManager();
		Nodes reachableHost;
		
		for (String nodeName: nodeSet) {
			NodeReader nodeReader =  nfgr.getNode(nodeName);								// get the nodereader information about the specified node
			String nodeID = clientState.getNodeId(nffgName, nodeName);						// get the node id of the node
			
			if (nodeID == null)
				//neo4jService.closeClient();
				throw new ServiceException("the node doesn't correspond to any node contained into the interface");

			reachableHost = neo4jService.getReachableHost(nodeID);
			hostSet = getHostSet(reachableHost);
			extendedNRset.add(new ExtendedNodeImpl(nodeReader, hostSet));
			
			/*try {
				reachableHost = neo4jService.getReachableHost(nodeID);
				hostSet = getHostSet(reachableHost);
				extendedNRset.add(new ExtendedNodeImpl(nodeReader, hostSet));
			} catch(ServiceException se) {
				//neo4jService.closeClient();
				throw se;
			}*/
		}
		return extendedNRset;
	}

	@Override
	public boolean isLoaded(String nffgName) throws UnknownNameException {
		return clientState.graphIsForwarded(nffgName);
	}

	// return the hostSet starting from the 
	private Set<HostReader> getHostSet(Nodes nodes) throws ServiceException {
		List<Node> nodeList = nodes.getNode();
		Set<HostReader> hostSet = new HashSet<HostReader> ();
		
		if (nodeList.isEmpty()) {
			System.out.println("server returned an empty list of nodes");
			return hostSet;
		}
		
		for (Node nodeElement: nodeList) {																// get hostname for each node in the response
			String hostName = nodeElement.getProperties().getProperty().get(0).getValue();
			HostReader hr;
			if ((hr = monitor.getHost(hostName)) == null) 
				throw new ServiceException("the hostname returned by the server doesn't exist into nfv reader interface");
			
			hostSet.add(hr);
		}
		return hostSet;
	}

	// read the data from the nffgReader interface and perform a post versus the server
	private void sendNffgNodes(NffgReader nfgr, Neo4jServiceManager neo4jService, Map<String, String> nodeIDmap) throws ServiceException {
		// newNodeName is the name of the network node
		String newNodeName, nodeID, hostname;
		Properties newProperties = new Properties();
		newProperties.getProperty().add(new Property());

		// create the representation of the neo4j node
		// put inside the properties class, declaring its name but not the value
		Node neo4jNode = new Node();
		neo4jNode.setProperties(newProperties);
		neo4jNode.getProperties().getProperty().get(0).setName(nffgpropertyNameValue);

		// create the labels for the network node and the host
		Labels hostLabels = new Labels();
		Labels networkNodeLabels = new Labels();
		hostLabels.getLabel().add(hostLabelValue);
		networkNodeLabels.getLabel().add(nffgLabelValue);
			
		// iterate all graph nodes and forward those to the service
		for(NodeReader nr: nfgr.getNodes()) {
			newNodeName = nr.getName();
			hostname = nr.getHost().getName();
			neo4jNode.getProperties().getProperty().get(0).setValue(newNodeName);
			nodeID = neo4jService.postNode(neo4jNode, networkNodeLabels);
			nodeIDmap.put(newNodeName, nodeID);													// the id received from the server is stored inside the hash map together with the name of the node
			if(clientState.hostIsForwarded(hostname)) {
				System.out.println("host " + hostname + " is already inside the database");
			} else {
				neo4jNode.getProperties().getProperty().get(0).setName(hostpropertyNameValue);
				neo4jNode.getProperties().getProperty().get(0).setValue(hostname);
				nodeID = neo4jService.postNode(neo4jNode, hostLabels);		
				clientState.setHostMap(hostname, nodeID);												// the id received from the server is stored inside the hash map together with the name of the node
			}	
		}
	}
	
	
	// send the relationhip between the nodes
	private void sendNffgRelationships(NffgReader nfgr, Neo4jServiceManager neo4jService, Map<String, String> nodeIDmap) throws ServiceException {
		String destNodeID, srcNodeID; 
		Relationship newRelationship = new Relationship();
			
		// read all nodes inside nodeReader interface
		for(NodeReader nr: nfgr.getNodes()) {
			destNodeID = clientState.getHostId(nr.getHost().getName());
			srcNodeID = nodeIDmap.get(nr.getName());
			
			if(destNodeID == null || srcNodeID == null) {
				//neo4jService.closeClient();
				throw new ServiceException("cannot read the information about the destination and/or the source node");
			}

			newRelationship.setType(hostrelType);
			newRelationship.setDstNode(destNodeID);
			newRelationship.setSrcNode(srcNodeID);
			neo4jService.postRelationship(newRelationship);
				
			// for each link inside the node 
			for(LinkReader l: nr.getLinks()) {
				destNodeID = nodeIDmap.get(l.getDestinationNode().getName());						// get the id of the destination node using the hashmap
				srcNodeID = nodeIDmap.get(l.getSourceNode().getName());								// get the id of the source node using the hashmap
					
				if(destNodeID == null || srcNodeID == null) {
					//neo4jService.closeClient();
					throw new ServiceException("cannot read the information about the destination and/or the source node");
				}
				newRelationship.setType(nffgrelType);
				newRelationship.setDstNode(destNodeID);
				newRelationship.setSrcNode(srcNodeID);
				neo4jService.postRelationship(newRelationship);
			}					
		}
	}
	
}
