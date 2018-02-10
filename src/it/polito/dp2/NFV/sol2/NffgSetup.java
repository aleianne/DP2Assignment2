package it.polito.dp2.NFV.sol2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;

import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.lab2.ServiceException;

// this class contain the status of the rest interaction between a client and the server
public class NffgSetup {
																
	private Map<String, Map<String, String>> graphNodeIDmap;										// hash map key->nodeName value->nodeID
	private Map<String, String> hostIDmap;															// hash map key->hostName value->nodeID
	
	private Set<StringPair> nodeRelSet;																// this set contain all the relationships submitted by this client
	
	// string used to create nffg's node property 
	private final String nffglabelValue = "Node";
	private final String nffgpropertyNameValue = "name";
	private final String nffgrelType = "ForwardTo";
	
	// string used to create host's node property 
	private final String hostlabelValue = "Host";
	private final String hostpropertyNameValue = "name";
	private final String hostrelType = "AllocatedOn";
	
	protected NffgSetup() {
		// instantiate all the data structure used inside the client
		graphNodeIDmap = new HashMap<String , Map<String, String>> ();
		hostIDmap = new HashMap<String, String> ();
		nodeRelSet = new HashSet<StringPair> ();
	}
	
	// return true if the nffg exist into the NFV reader interface, false otherwise
	protected boolean searchNffg(String nffgName) {
		if (graphNodeIDmap.get(nffgName) == null) 
			return false;
		else 
			return true;
	}
	
	protected Map<String, String> getNodeIDmap(String nffgName) {
		return graphNodeIDmap.get(nffgName);
	}
	
	// read the data from the nffg interface and perform a post versus the server
	protected void sendNffgNodes(NffgReader nfgr) throws ServiceException {
		if(nfgr == null)
			throw new ServiceException("application cannot forward the data to the server");
											
		Map<String, String> nodeIDmap = new HashMap<String, String> ();
		
		String nodeName, nodeID;
		Neo4jServiceManager neo4jService = new Neo4jServiceManager();
		
		Properties newProperties = new Properties();
		newProperties.getProperty().add(new Property());
	
		Node neo4jNode = new Node();
		neo4jNode.setProperties(newProperties);
		
		Labels hostLabels = new Labels();
		Labels networkNodeLabels = new Labels();
		hostLabels.getLabel().add(nffgLabelValue);
		networkNodeLabels.getLabels().add(hostLabelValue);
		
		// intereate all the node in the graph and forward it to the web service
		for(NodeReader nr: nfgr.getNodes()) {
			nodeName = nr.getName();
			
			neo4jNode.getProperties().getProperty().get(0).setName(nffgpropertyNameValue);
			neo4jNode.getProperties().getProperty().get(0).setValue(nodeName);
			
			nodeID = neo4jService.postNode(neo4jNode, networkNodeLabels);
			nodeIDmap.put(nodeName, nodeID);													// the id received from the server is stored inside the hash map together with the name of the node
		
			// check if an host is already loaded
			if(hostIDmap.get(nr.getHost().getName()) == null) {
				nodeName = nr.getHost().getName();
				
				// set the property for the specified node
				neo4jNode.getProperties().getProperty().get(0).setName(hostpropertyNameValue);
				neo4jNode.getProperties().getProperty().get(0).setValue(nodeName);
				
				// forward the node with his labels
				nodeID = neo4jService.postNode(neo4jNode, hostLabels);		
				hostIDmap.put(nodeName, nodeID);												// the id received from the server is stored inside the hash map together with the name of the node
			} else {
				System.out.println("host " + nr.getHost().getName() + " is already inside the database");
			}	
		}
		graphNodeIDmap.put(nfgr.getName(), nodeIDmap);										// associate the hashmap just created with the nffg graph name
	}
	
	// send the relationhip between the nodes
	protected void sendNffgRelationships(NffgReader nfgr, Neo4jServiceManager neo4jService) throws ServiceException {	
		Map<String , String> nodeIDmap;
		
		if((nodeIDmap = graphNodeIDmap.get(nfgr.getName())) == null) 
			throw new ServiceException();
		
		String destNodeID, srcNodeID; 
		
		// instantiate a new relationship object used to create the XML request
		Relationship newRelationship = new Relationship();
		
		// read all the node inside interface
		for(NodeReader nr: nfgr.getNodes()) {
			Set<LinkReader> lrSet = nr.getLinks();
			
			destNodeID = hostIDmap.get(nr.getHost().getName());
			srcNodeID = nodeIDmap.get(nr.getName());
			
			// lauch a new exception if the hashmaps return null
			if(destNodeID == null || srcNodeID == null)
				throw new ServiceException("cannot read the information about the destination and/or the source node");
			
			StringPair nodeRel = new StringPair(nr.getName(), nr.getHost().getName());
			
			// check if the relationships between two nodes are already loaded into the DB 
			if(!nodeRelSet.contains(nodeRel)) {
				newRelationship.setType(hostrelType);
				newRelationship.setDstNode(destNodeID);
				newRelationship.setSrcNode(srcNodeID);
				neo4jService.postRelationship(newRelationship);
				nodeRelSet.add(nodeRel);																	// add the relationship into the set 
			} else {
				System.out.println("the relationship is already into the database");
			}
			
			// for each link inside the node 
			for(LinkReader l: lrSet) {
				destNodeID = nodeIDmap.get(l.getDestinationNode().getName());						// get the id of the destination node using the hashmap
				srcNodeID = nodeIDmap.get(l.getSourceNode().getName());								// get the id of the source node using the hashmap
				
				if(destNodeID == null || srcNodeID == null) 
					throw new ServiceException("cannot read the information about the destination and/or the source node");
				
				StringPair nodeRel2 = new StringPair(l.getSourceNode().getName(), l.getDestinationNode().getName());
				
				// check if the relationships between two nodes are already loaded into the DB 
				if(!nodeRelSet.contains(nodeRel2)) {
					newRelationship.setType(nffgrelType);
					newRelationship.setDstNode(destNodeID);
					newRelationship.setSrcNode(srcNodeID);
					neo4jService.postRelationship(newRelationship);
					nodeRelSet.add(nodeRel2);																	// add the relationship into the set 
				} else {
					System.out.println("the relatioship is already into the database");
				}
			}					
		}
	}

}
