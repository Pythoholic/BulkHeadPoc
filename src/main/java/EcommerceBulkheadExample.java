/**
 * EcommerceBulkheadExample.java
 *
 * This Java program demonstrates the implementation of the Bulkhead pattern using Resilience4j
 * to enhance the fault tolerance and stability of a payment processing service within an
 * e-commerce application. The Bulkhead pattern limits the number of concurrent requests to a
 * service, preventing it from being overwhelmed and ensuring that other parts of the system
 * remain unaffected by high load or failure conditions.
 *
 * The program configures a bulkhead for the payment processing service, allowing a maximum
 * number of concurrent transactions and using a timeout mechanism to handle overload conditions.
 * It then simulates multiple payment requests to demonstrate the behavior of the bulkhead
 * pattern in managing concurrent access and isolating service failures.
 *
 * Key Features:
 * - Bulkhead Pattern: Uses Resilience4j to limit the number of concurrent payment transactions.
 * - Service Isolation: Prevents the payment processing service from overwhelming the system.
 * - Simulated Service Work: Introduces delays to simulate real-world payment processing.
 * - Resilience4j Integration: Demonstrates the use of Resilience4j decorators for bulkhead management.
 *
 * Configuration:
 * - Payment Processing Bulkhead: Configured to allow a maximum of 5 concurrent transactions, with a maximum wait duration of 1000ms.
 *
 * Usage:
 * - This program can be executed directly, and it will simulate 10 payment requests.
 * - Observe the console output to see how the bulkhead pattern manages concurrent requests and handles overload conditions.
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
 * - Payment result: Payment successful
 * - Payment failed: Error: Payment service is currently overloaded. Please try again later.
 */

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.decorators.Decorators;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class EcommerceBulkheadExample {

    public static void main(String[] args) {
        // Configure the Bulkhead for Payment Processing Service
        BulkheadConfig paymentConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(5) // Limit to 5 concurrent payment transactions
                .maxWaitDuration(Duration.ofMillis(1000)) // Wait for up to 1000ms to acquire permission
                .build();

        BulkheadRegistry registry = BulkheadRegistry.of(paymentConfig);
        Bulkhead paymentBulkhead = registry.bulkhead("paymentService");

        // Use the Bulkhead to decorate a Supplier
        Supplier<String> decoratedPaymentSupplier = Decorators.ofSupplier(() -> {
            if (paymentBulkhead.tryAcquirePermission()) {
                try {
                    return processPayment();
                } finally {
                    paymentBulkhead.releasePermission();
                }
            } else {
                return "Error: Payment service is currently overloaded. Please try again later.";
            }
        }).withBulkhead(paymentBulkhead).decorate();

        // Simulate multiple payment requests
        for (int i = 0; i < 10; i++) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(decoratedPaymentSupplier);
            future.thenAccept(result -> System.out.println("Payment result: " + result))
                    .exceptionally(throwable -> {
                        System.out.println("Payment failed: " + throwable.getMessage());
                        return null;
                    });
        }

        // Wait for all futures to complete (for demo purposes)
        try {
            Thread.sleep(5000); // Simulate waiting time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Main method execution finished.");
    }

    private static String processPayment() {
        // Simulate payment processing
        System.out.println("Processing payment...");
        try {
            Thread.sleep(1000); // Simulate a delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Payment processed successfully.");
        return "Payment successful";
    }
}
