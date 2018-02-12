package it.polito.dp2.NFV.sol2;

import java.util.Set;

import it.polito.dp2.NFV.HostReader;
import it.polito.dp2.NFV.LinkReader;
import it.polito.dp2.NFV.NffgReader;
import it.polito.dp2.NFV.NodeReader;
import it.polito.dp2.NFV.VNFTypeReader;
import it.polito.dp2.NFV.lab2.ExtendedNodeReader;
import it.polito.dp2.NFV.lab2.NoGraphException;
import it.polito.dp2.NFV.lab2.ServiceException;

public class ExtendedNodeImpl implements ExtendedNodeReader {
	
	private NodeReader nr;
	private Set<HostReader> hostSet;
	
	protected ExtendedNodeImpl(NodeReader nr, Set<HostReader> hostSet) {
		this.nr = nr;
		this.hostSet = hostSet;
	}

	@Override
	public VNFTypeReader getFuncType() {
		return nr.getFuncType();
	}

	@Override
	public HostReader getHost() {
		return nr.getHost();
	}

	@Override
	public Set<LinkReader> getLinks() {
		return nr.getLinks();
	}

	@Override
	public NffgReader getNffg() {
		return nr.getNffg();
	}

	@Override
	public String getName() {
		return nr.getName();
	}

	@Override
	public Set<HostReader> getReachableHosts() throws NoGraphException, ServiceException {
		if (hostSet == null) {
			throw new ServiceException("impossible to return the set of extended nodes");
		}
		return hostSet;
	}

	
	
	
}
