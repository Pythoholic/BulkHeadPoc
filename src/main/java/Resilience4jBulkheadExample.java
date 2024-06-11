/**
 * Resilience4jBulkheadExample.java
 *
 * This Java program demonstrates the implementation of the Bulkhead pattern using Resilience4j
 * to enhance the fault tolerance and stability of a service within an application. The Bulkhead
 * pattern limits the number of concurrent calls to a service, preventing it from being overwhelmed
 * and ensuring that other parts of the system remain unaffected by high load or failure conditions.
 *
 * The program configures a bulkhead to manage concurrent access to a simulated service, introducing
 * delays to represent real-world processing. It uses Resilience4j decorators to enforce bulkhead
 * limits and handle overload scenarios gracefully.
 *
 * Key Features:
 * - Bulkhead Pattern: Uses Resilience4j to limit the number of concurrent calls to a service.
 * - Service Isolation: Prevents a single service from overwhelming the system.
 * - Simulated Service Work: Introduces delays to simulate real-world service processing.
 * - Resilience4j Integration: Demonstrates the use of Resilience4j decorators for bulkhead management.
 *
 * Configuration:
 * - Bulkhead: Configured to allow a maximum of 10 concurrent calls, with a maximum wait duration of 500ms.
 *
 * Usage:
 * - This program can be executed directly, and it will simulate a service call with bulkhead limits.
 * - Observe the console output to see how the bulkhead pattern manages concurrent calls and handles overload conditions.
 *
 * Author: Pythoholic
 * Version: 1.0
 * Date: 11 Jun 2024
 *
 * Dependencies:
 * - Resilience4j 1.7.1
 * - Java 11 or later
 *
 * License:
 * - This code is provided under the MIT License.
 *
 * Example Output:
 * - Attempting to acquire permission from bulkhead...
 * - Permission acquired, calling service...
 * - Service is being called...
 * - Service call completed.
 * - Service A result: Service A response
 * - Bulkhead full, service call rejected
 * - Service A failed: Error: Bulkhead full
 */

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.decorators.Decorators;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Resilience4jBulkheadExample {

    public static void main(String[] args) {
        // Configure the Bulkhead
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(10)
                .maxWaitDuration(Duration.ofMillis(500))
                .build();

        BulkheadRegistry registry = BulkheadRegistry.of(config);
        Bulkhead bulkhead = registry.bulkhead("serviceA");

        // Use the Bulkhead to decorate a Supplier
        Supplier<String> decoratedSupplier = Decorators.ofSupplier(() -> {
            System.out.println("Attempting to acquire permission from bulkhead...");
            if (bulkhead.tryAcquirePermission()) { // Acquire permission
                System.out.println("Permission acquired, calling service...");
                try {
                    return callService();
                } finally {
                    System.out.println("Releasing permission...");
                    bulkhead.releasePermission(); // Release permission
                }
            } else {
                System.out.println("Bulkhead full, service call rejected");
                return "Error: Bulkhead full";
            }
        }).withBulkhead(bulkhead).decorate(); // Decorate with bulkhead for other errors

        // Execute the decorated supplier asynchronously
        CompletableFuture<String> future = CompletableFuture.supplyAsync(decoratedSupplier);

        // Process the result (non-blocking)
        future.thenAccept(result -> {
            System.out.println("Service A result: " + result);
        }).exceptionally(throwable -> {
            System.out.println("Service A failed: " + throwable.getMessage());
            return null;
        });

        future.join();

        System.out.println("Main method execution finished.");
    }

    private static String callService() {
        // Simulate service work
        System.out.println("Service is being called...");
        try {
            Thread.sleep(1000); // Simulate a delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Service call completed.");
        return "Service A response";
    }
}
