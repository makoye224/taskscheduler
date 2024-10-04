package taskscheduler.java.other;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AlertSystem {

    private static final Logger logger = Logger.getLogger(AlertSystem.class.getName());

    // Define different levels of alerts
    public enum AlertLevel {
        INFO, WARNING, CRITICAL
    }

    // Trigger an alert with a custom message and a default WARNING level
    public void triggerAlert(String message) {
        triggerAlert(AlertLevel.WARNING, message);
    }

    // Trigger an alert with a specified alert level and message
    public void triggerAlert(AlertLevel level, String message) {
        switch (level) {
            case INFO, WARNING:
                sendConsoleAlert(level, message);
                logAlert(level, message);
                break;
            case CRITICAL:
                sendConsoleAlert(level, message);
                logAlert(level, message);
                sendEmailAlert(level, message);
                break;
        }
    }

    // Send an alert to the console (basic output)
    private void sendConsoleAlert(AlertLevel level, String message) {
        System.out.println("[" + level + "] ALERT: " + message);
    }

    // Log the alert using the Java logging system
    private void logAlert(AlertLevel level, String message) {
        switch (level) {
            case INFO:
                logger.log(Level.INFO, message);
                break;
            case WARNING:
                logger.log(Level.WARNING, message);
                break;
            case CRITICAL:
                logger.log(Level.SEVERE, message);
                break;
        }
    }

    // Mocked method to send an email alert for critical issues
    private void sendEmailAlert(AlertLevel level, String message) {
        // Simulating email sending process (In real use case, integrate with an email service)
        System.out.println("Sending email alert...");
        System.out.println("To: admin@company.com");
        System.out.println("Subject: [" + level + "] Server Alert");
        System.out.println("Message: " + message);
    }

    // Method to create an alert based on specific thresholds
    public void checkForHighFailureRate(double failureRate, double threshold) {
        if (failureRate > threshold) {
            triggerAlert(AlertLevel.CRITICAL, "Failure rate exceeded threshold! Current failure rate: " + (failureRate * 100) + "%");
        } else {
            triggerAlert(AlertLevel.INFO, "Failure rate within acceptable limits: " + (failureRate * 100) + "%");
        }
    }

    public void checkForHighTaskQueue(int taskQueueSize, int threshold) {
        if (taskQueueSize > threshold) {
            triggerAlert(AlertLevel.WARNING, "Task queue size exceeded threshold! Current size: " + taskQueueSize);
        } else {
            triggerAlert(AlertLevel.INFO, "Task queue size is under control: " + taskQueueSize);
        }
    }

    public void checkForHighTotalLoad(long totalLoad, long threshold) {
        if (totalLoad > threshold) {
            triggerAlert(AlertLevel.CRITICAL, "Total load exceeded threshold! Current load: " + totalLoad + " ms");
        } else {
            triggerAlert(AlertLevel.INFO, "Total load is within acceptable limits: " + totalLoad + " ms");
        }
    }
}
