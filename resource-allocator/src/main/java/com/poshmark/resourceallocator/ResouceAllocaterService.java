package com.poshmark.resourceallocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;

/**
 * @author Murali Palaivel
 *
 *         Optimized cloud resource allocation service
 */

@Service
public class ResouceAllocaterService {

	// server types given the problem statement
	private String server_types[] = { "large", "xlarge", "2xlarge", "4xlarge", "8xlarge", "10xlarge" };

	// input instances of different data center regions
	private JSONObject jsonObject = new JSONObject("{ \"us-east\":{ \"large\":0.12, \"xlarge\":0.23,\r\n"
			+ "	 \"2xlarge\":0.45, \"4xlarge\":0.774, \"8xlarge\":1.4, \"10xlarge\":2.82 },\r\n"
			+ "	  \"us-west\":{ \"large\":0.14, \"2xlarge\":0.413, \"4xlarge\":0.89, \"8xlarge\":1.3,\r\n"
			+ "	  \"10xlarge\":2.97 }, \"asia\":{ \"large\":0.11, \"xlarge\":0.20, \"4xlarge\":0.67,\r\n"
			+ "	  \"8xlarge\":1.18 } }");

	// total cost will be stored in 2 dimentional array
	public float[][] cost = null;

	// total CPU counts per region
	public int[] regionWiseServerCount = null;

	// consolidated count
	public List<Integer> cpuCount = new ArrayList<>();

	public List<Dict> get_costs(int cpus, int hours, float price) throws Exception {

		// allocated resource will be assigned in list of Dicts
		List<Dict> list = null;
		try {
			// calculating the total_cost based on the hours
			prizingBasedOnHours(hours);
			if (cpus == 0 && price != 0) {
				list = withoutCPU(price);
			} else if (price == 0 && cpus != 0) {
				list = withoutPrice(cpus);
			} else if (price != 0 && price != 0) {
				list = withPriceMarginAndCPU(cpus, price);
			} else {
				throw new Exception("Both cpu and price cant be zero. Invalid request !!!!!!!!!!!!!!");
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
		System.out.println(new Gson().toJson(list));
		return list;
	}

	public List<Dict> withoutCPU(float price) {
		int len = 0;
		int optimal_server_combo[][] = new int[jsonObject.keySet().size()][server_types.length];
		len = 0;
		float reduce_price = 0.0f;
		for (int i = 0; i < cost.length; i++) {
			reduce_price = price;
			len = server_types.length;
			while (len > 0) {
				if (cost[i][len - 1] < 0) {
					len = len - 1;
				} else if (cost[i][len - 1] <= reduce_price) {
					reduce_price = reduce_price - cost[i][len - 1];
					if (cpuCount.get(len - 1) > 0) {
						optimal_server_combo[i][len - 1] = optimal_server_combo[i][len - 1] + 1;
					}
				} else {
					len = len - 1;
				}
			}
		}

		return getOptimalServerCombo(optimal_server_combo);
	}

	public List<Dict> withoutPrice(int cpus) {
		int len = 0;
		int reduce_price = 0;
		int totalCPUCount = 0;
		int tempCpuCount = 0;
		int optimal_server_combo[][] = new int[jsonObject.keySet().size()][server_types.length];
		// System.out.println("total number of cpus : " + totalCPUCount);
		for (int i = 0; i < cost.length; i++) {
			len = 6;
			totalCPUCount = regionWiseServerCount[i];
			tempCpuCount = cpus;
			while (len > 0) {
				if (tempCpuCount > totalCPUCount) {
					if (totalCPUCount > 0) {
						reduce_price = Math.floorDiv(tempCpuCount, totalCPUCount);
						tempCpuCount = tempCpuCount % totalCPUCount;
					} else {
						optimal_server_combo[i][0] = optimal_server_combo[i][0] + 1;
						break;
					}
					for (int j = 0; j < len; j++) {
						if (cpuCount.get(j) > 0)
							optimal_server_combo[i][j] += reduce_price;
						else
							optimal_server_combo[i][j] = 0;
					}
				} else {
					if (cpuCount.get(len - 1) > 0) {
						totalCPUCount = totalCPUCount - cpuCount.get(len - 1);
					}
					len = len - 1;
				}
			}
		}
		return getOptimalServerCombo(optimal_server_combo);
	}

	//
	public List<Dict> withPriceMarginAndCPU(int cpus, float price) {
		int len = 0;
		int optimal_server_combo[][] = new int[jsonObject.keySet().size()][server_types.length];
		int incr = 0;
		int totalCPUCount = 0;
		int tempCpuCount = 0;
		// System.out.println("total number of cpus : " + totalCPUCount);
		for (int i = 0; i < cost.length; i++) {
			len = 6;
			totalCPUCount = regionWiseServerCount[i];
			tempCpuCount = cpus;
			float maxPrice = 0.0f;
			while (maxPrice <= price) {
				if (tempCpuCount > totalCPUCount) {
					if (totalCPUCount > 0) {
						incr = Math.floorDiv(tempCpuCount, totalCPUCount);
						tempCpuCount = tempCpuCount % totalCPUCount;
					} else {
						optimal_server_combo[i][0] = optimal_server_combo[i][0] + 1;
						break;
					}
					for (int j = 0; j < len; j++) {
						if (cpuCount.get(j) > 0) {
							optimal_server_combo[i][j] += incr;
							maxPrice *= optimal_server_combo[i][j];
						} else
							optimal_server_combo[i][j] = 0;
					}
					if (maxPrice > price) {
						for (int j = 0; j < len; j++) {
							if (cpuCount.get(j) > 0) {
								optimal_server_combo[i][j] -= incr;
							} else
								optimal_server_combo[i][j] = 0;
						}
					}
				} else {
					if (len == 0) {
						break;
					}
					if (cpuCount.get(len - 1) > 0) {
						totalCPUCount = totalCPUCount - cpuCount.get(len - 1);
					}
					len = len - 1;
				}
			}
		}
		return getOptimalServerCombo(optimal_server_combo);
	}

	// calculating the total price based on the hours of resource required
	public void prizingBasedOnHours(float hours) {
		int totalServerTypes = server_types.length;
		Iterator<String> keys = jsonObject.keys();
		regionWiseServerCount = new int[jsonObject.keySet().size()];
		cost = new float[jsonObject.keySet().size()][totalServerTypes];
		int i = 0;
		int serverCount = 0;
		while (keys.hasNext()) {
			String key = keys.next();
			JSONObject obj = (JSONObject) jsonObject.get(key);
			serverCount = 0;
			for (int j = 0; j < server_types.length; j++) {
				if (obj.has(server_types[j])) {
					obj.put(server_types[j], obj.getFloat(server_types[j]) * hours);
					cost[i][j] = obj.getFloat(server_types[j]);
					int totalCpuPerServer = (int) Math.pow(2, j);
					serverCount += totalCpuPerServer;
					cpuCount.add(totalCpuPerServer);
				} else {
					// if datacenter does not have the server type
					cost[i][j] = -1 * hours;
					cpuCount.add(0);
				}
			}
			regionWiseServerCount[i] = serverCount;
			i++;
		}
//		System.out.println("Total Cost : " + Arrays.deepToString(cost));
//		System.out.println(cpuCount);
//		System.out.println(Arrays.toString(regionWiseServerCount));
	}

	public List<Dict> getOptimalServerCombo(int server_combo[][]) {
		Set<String> set = jsonObject.keySet();
		List<Dict> list = new ArrayList<>();
		int i = 0;
		for (String region : set) {
			Dict dict = new Dict();
			dict.setRegion(region);
			float totalCost = 0;
			List<String> servers = new ArrayList();
			for (int k = 0; k < server_types.length; k++) {
				// System.out.println(server_combo[i][k]);
				JSONObject obj = (JSONObject) jsonObject.get(region);
				if (server_combo[i][k] > 0 && obj.has(server_types[k])) {
					totalCost += cost[i][k] * server_combo[i][k];
					servers.add("(" + server_types[k] + "," + server_combo[i][k] + ")");
				}
			}
			// System.out.println(servers);
			dict.setTotal_cost("$" + totalCost);
			dict.setServers(servers);
			list.add(dict);
			i++;
		}
		// Dict is comparable
		// sorting the list based on the total_cost
		Collections.sort(list);
		return list;
	}

//	public static void main(String ar[]) {
//		get_costs(0, 8, 29.0f);
//		get_costs(0, 8, 29.0f);
//		// get_costs(10, 5, 0.0f);
//		// get_costs(20, 5, 10.0f);
//	}
}
