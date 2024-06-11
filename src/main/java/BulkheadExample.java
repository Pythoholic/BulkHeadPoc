/**
 * BulkheadExample.java
 *
 * This Java program demonstrates the implementation of the Bulkhead pattern using thread pools
 * to isolate different services or components within an application. The Bulkhead pattern
 * enhances the fault tolerance and stability of the system by limiting the number of concurrent
 * tasks for each service, preventing one service from overwhelming the entire system.
 *
 * The program creates two separate thread pools for two services (Service A and Service B) and
 * limits the number of concurrent tasks for each service. It simulates the execution of tasks
 * for each service and ensures that the services can operate independently without affecting
 * each other.
 *
 * Key Features:
 * - Bulkhead Pattern: Uses separate thread pools to isolate services and limit concurrent tasks.
 * - Service Isolation: Ensures that each service operates within its own resource constraints.
 * - Simulated Service Work: Introduces delays to simulate real-world service processing.
 *
 * Configuration:
 * - Service A Thread Pool: Configured to handle a maximum of 10 concurrent tasks.
 * - Service B Thread Pool: Configured to handle a maximum of 5 concurrent tasks.
 *
 * Usage:
 * - This program can be executed directly, and it will submit tasks to both services.
 * - Observe the console output to see the responses from each service and how the bulkhead pattern operates.
 *
 * Author: Pythoholic
 * Version: 1.0
 * Date: 11 Jun 2024
 *
 * Dependencies:
 * - Java 8 or later
 *
 * License:
 * - This code is provided under the MIT License.
 *
 * Example Output:
 * - Response from Service A: Service A response
 * - Response from Service B: Service B response
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BulkheadExample {

    // Create thread pools for each service/component
    private final ExecutorService serviceAThreadPool = Executors.newFixedThreadPool(10); // Limit to 10 concurrent tasks
    private final ExecutorService serviceBThreadPool = Executors.newFixedThreadPool(5);  // Limit to 5 concurrent tasks

    public Future<String> callServiceA() {
        return serviceAThreadPool.submit(() -> {
            // Simulate service A work
            Thread.sleep(2000); // Simulate a delay
            return "Service A response";
        });
    }

    public Future<String> callServiceB() {
        return serviceBThreadPool.submit(() -> {
            // Simulate service B work
            Thread.sleep(1000); // Simulate a delay
            return "Service B response";
        });
    }

    public void shutdown() {
        serviceAThreadPool.shutdown();
        serviceBThreadPool.shutdown();
    }

    public static void main(String[] args) throws Exception {
        BulkheadExample bulkheadExample = new BulkheadExample();

        // Submit tasks to each service
        Future<String> responseA = bulkheadExample.callServiceA();
        Future<String> responseB = bulkheadExample.callServiceB();

        // Get the responses
        System.out.println("Response from Service A: " + responseA.get());
        System.out.println("Response from Service B: " + responseB.get());

        // Shutdown the thread pools
        bulkheadExample.shutdown();
    }
}
