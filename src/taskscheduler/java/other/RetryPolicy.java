package taskscheduler.java.other;

public class RetryPolicy {

    private final int maxRetries;
    private final long baseDelay;  // Base delay in milliseconds
    private final boolean exponentialBackoff;

    public RetryPolicy(int maxRetries, long baseDelayInSeconds, boolean exponentialBackoff) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries must be non-negative");
        }
        if (baseDelayInSeconds < 0) {
            throw new IllegalArgumentException("Delay must be non-negative");
        }

        this.maxRetries = maxRetries;
        this.baseDelay = baseDelayInSeconds * 1000;  // Convert seconds to milliseconds
        this.exponentialBackoff = exponentialBackoff;
    }

    // Get the maximum number of retries
    public int getMaxRetries() {
        return maxRetries;
    }

    // Calculate the delay for the given retry attempt (1-based index)
    public long getDelay(int attempt) {
        if (attempt <= 0) {
            throw new IllegalArgumentException("Attempt number must be greater than 0");
        }
        return exponentialBackoff ? baseDelay * (long) Math.pow(2, attempt - 1) : baseDelay;
    }
}
