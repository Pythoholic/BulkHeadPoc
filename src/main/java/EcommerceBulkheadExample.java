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
