package it.polito.dp2.NFV.sol2;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClientState {

	private Map<String, Map<String, String>> graphMap;
	private Map<String, String> hostMap;
	
	protected ClientState() {
		graphMap = new HashMap<String, Map<String, String>> ();
		hostMap = new HashMap<String, String> ();
	}
	
	// insert in the graph map a set of nodes and their relative id returned by the server 
	protected void setGraphMap(String nffgName, Map<String, String> nodeMap) {
		graphMap.put(nffgName, nodeMap);
	}
	
	protected void setHostMap(String hostname, String nodeID) {
		hostMap.put(hostname, nodeID);
	}
	
	protected boolean hostIsForwarded(String hostName)  {
		if(hostMap.get(hostName) == null) 
			return false;
		else 
			return true;
	}
	
	protected boolean graphIsForwarded(String graphName) {
		if(graphMap.get(graphName) == null) 
			return false;
		else 
			return true;
	} 
	
	protected String getHostId(String hostname) {
		return hostMap.get(hostname);
	}
	
	// get the node id 
	protected String getNodeId(String nffgName, String nodeName) {
		Map<String, String> nodeMap = graphMap.get(nffgName);
		if(nodeMap == null) 
			return null;
		else 
			return nodeMap.get(nodeName);
	}
		
	// return a set containing the nodes inside the graph
	protected Set<String> getNodes(String nffgName) {
		Map<String, String> nodeMap = graphMap.get(nffgName);
		if(nodeMap == null) 
			return null;
		else 
			return nodeMap.keySet();
	}
}
