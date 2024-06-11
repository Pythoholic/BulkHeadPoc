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
