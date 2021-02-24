package com.poshmark.resourceallocator;

import java.util.List;

/**
 * @author Murali
 * entity model for send the allocated 
 * resources to the consumer
 *
 */
public class Dict implements Comparable<Dict> {

	private String region;
	private String total_cost;
	private List<String> servers;

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getTotal_cost() {
		return total_cost;
	}

	public void setTotal_cost(String total_cost) {
		this.total_cost = total_cost;
	}

	public List<String> getServers() {
		return servers;
	}

	public void setServers(List<String> servers) {
		this.servers = servers;
	}

	@Override
	public int compareTo(Dict obj) {
		float thisObj=Float.parseFloat(this.total_cost.substring(1));
		float comparableObj=Float.parseFloat(obj.total_cost.substring(1));
		if(thisObj==comparableObj) return 0;
		return thisObj<comparableObj?-1:1;
	}

}
