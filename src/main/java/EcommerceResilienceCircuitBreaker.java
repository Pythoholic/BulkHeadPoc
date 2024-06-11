import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class EcommerceResilienceCircuitBreaker {

    public static void main(String[] args) {
        // Configure the Bulkhead for Payment Processing Service
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(5) // Limit to 5 concurrent payment transactions
                .maxWaitDuration(Duration.ofMillis(1000)) // Wait for up to 1000ms to acquire permission
                .build();

        BulkheadRegistry bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead("paymentService");

        // Configure the Circuit Breaker for Payment Processing Service
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(20) // Open the circuit breaker if 20% of requests fail
                .waitDurationInOpenState(Duration.ofMillis(5000)) // Keep the circuit breaker open for 5 seconds
                .slidingWindowSize(10) // Use a sliding window of 10 requests for metrics
                .build();

        CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentService");

        // Simulate multiple payment requests
        for (int i = 0; i < 20; i++) { // Increase the number of requests to 20
            String correlationId = UUID.randomUUID().toString();
            Supplier<String> decoratedSupplier = Decorators.ofSupplier(() -> processPayment(correlationId))
                    .withBulkhead(bulkhead)
                    .withCircuitBreaker(circuitBreaker)
                    .decorate();

            CompletableFuture<String> future = CompletableFuture.supplyAsync(decoratedSupplier);
            future.thenAccept(result -> log(correlationId, "Payment result: " + result))
                    .exceptionally(throwable -> {
                        log(correlationId, "Payment failed: " + throwable.getMessage());
                        return null;
                    });
        }

        // Wait for all futures to complete (for demo purposes)
        try {
            Thread.sleep(15000); // Simulate waiting time increased to 15 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Main method execution finished.");
    }

    private static String processPayment(String correlationId) {
        log(correlationId, "Processing payment...");
        try {
            Thread.sleep(1000); // Simulate a delay
            // Simulate a failure condition (for demonstration purposes)
            if (Math.random() > 0.5) { // Increase failure rate to 50%
                throw new RuntimeException("Payment processing failed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log(correlationId, "Payment processed successfully.");
        return "Payment successful";
    }

    private static void log(String correlationId, String message) {
        System.out.println("[" + correlationId + "] [" + Thread.currentThread().getName() + "] " + message);
    }
}
