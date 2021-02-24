package com.poshmark.resourceallocator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.gson.Gson;

@SpringBootApplication
public class ResourceAllocatorApplication {

	public static void main(String[] args) throws Exception {
		// SpringApplication.run(ResourceAllocatorApplication.class, args);

		ResouceAllocaterService resouceAllocaterService = new ResouceAllocaterService();

		// test case - 1
		resouceAllocaterService.get_costs(0, 8, 29.0f);

		// test case - 2
		// resouceAllocaterService.get_costs(10, 5, 0.0f);

		// test case - 3
		// resouceAllocaterService.get_costs(20, 5, 10.0f);

		// test case - 4
		// resouceAllocaterService.get_costs(0, 0, 0.0f);

	}

}
