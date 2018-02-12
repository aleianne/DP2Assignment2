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
	private final String nffgLabelValue = "Node";
	private final String nffgpropertyNameValue = "name";
	private final String nffgrelType = "ForwardTo";
		
	// string used to create host's node property 
	private final String hostLabelValue = "Host";
	private final String hostpropertyNameValue = "name";
	private final String hostrelType = "AllocatedOn";
	
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
		
		Set<String> nodeSet = clientState.getNodes(nffgName);
		
		if(nodeSet == null || nodeSet.isEmpty()) {
			throw new NoGraphException("the graph is not loaded into the DataBase");
		}
		
		Set<ExtendedNodeReader> extendedNRset = new HashSet<ExtendedNodeReader> ();
		Set<HostReader> hostSet;
		Neo4jServiceManager neo4jService = new Neo4jServiceManager();
		Nodes reachableHost;
		// for each element in the map get the extended node
		for (String nodeName: nodeSet) {
			NodeReader nodeReader =  nfgr.getNode(nodeName);								// get the nodereader information about the specified node
			String nodeID = clientState.getNodeId(nffgName, nodeName);						// get the node id of the node
			
			// check if the node is null or not
			if (nodeID == null) 
				throw new ServiceException("the node not correspond to any node contained into the interface");
			
			reachableHost = neo4jService.getReachableHost(nodeID);
			hostSet = getHostSet(reachableHost);
			extendedNRset.add(new ExtendedNodeImpl(nodeReader, hostSet));
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

	// read the data from the nffg interface and perform a post versus the server
	protected void sendNffgNodes(NffgReader nfgr, Neo4jServiceManager neo4jService, Map<String, String> nodeIDmap) throws ServiceException {
		// ntwNodeName is the name of the network node
		String ntwNodeName, nodeID, hostname;
		Properties newProperties = new Properties();
		newProperties.getProperty().add(new Property());
		
		Node neo4jNode = new Node();
		neo4jNode.setProperties(newProperties);
			
		Labels hostLabels = new Labels();
		Labels networkNodeLabels = new Labels();
		hostLabels.getLabel().add(hostLabelValue);
		networkNodeLabels.getLabel().add(nffgLabelValue);
			
		// intereate all the node in the graph and forward it to the web service
		for(NodeReader nr: nfgr.getNodes()) {
			ntwNodeName = nr.getName();
			neo4jNode.getProperties().getProperty().get(0).setName(nffgpropertyNameValue);
			neo4jNode.getProperties().getProperty().get(0).setValue(ntwNodeName);
			nodeID = neo4jService.postNode(neo4jNode, networkNodeLabels);
			nodeIDmap.put(ntwNodeName, nodeID);													// the id received from the server is stored inside the hash map together with the name of the node
			
			hostname = nr.getHost().getName();
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
	protected void sendNffgRelationships(NffgReader nfgr, Neo4jServiceManager neo4jService, Map<String, String> nodeIDmap) throws ServiceException {			
		String destNodeID, srcNodeID; 
		Relationship newRelationship = new Relationship();
			
		// read all the node inside interface
		for(NodeReader nr: nfgr.getNodes()) {
			destNodeID = clientState.getHostId(nr.getHost().getName());
			srcNodeID = nodeIDmap.get(nr.getName());
			
			if(destNodeID == null || srcNodeID == null)
				throw new ServiceException("cannot read the information about the destination and/or the source node");
				
			newRelationship.setType(hostrelType);
			newRelationship.setDstNode(destNodeID);
			newRelationship.setSrcNode(srcNodeID);
			neo4jService.postRelationship(newRelationship);
				
			// for each link inside the node 
			for(LinkReader l: nr.getLinks()) {
				destNodeID = nodeIDmap.get(l.getDestinationNode().getName());						// get the id of the destination node using the hashmap
				srcNodeID = nodeIDmap.get(l.getSourceNode().getName());								// get the id of the source node using the hashmap
					
				if(destNodeID == null || srcNodeID == null) 
					throw new ServiceException("cannot read the information about the destination and/or the source node");
					
				newRelationship.setType(nffgrelType);
				newRelationship.setDstNode(destNodeID);
				newRelationship.setSrcNode(srcNodeID);
				neo4jService.postRelationship(newRelationship);
			}					
		}
	}
	
}
