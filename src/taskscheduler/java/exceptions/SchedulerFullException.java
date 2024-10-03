package taskscheduler.java.exceptions;

public class SchedulerFullException extends RuntimeException {
    public SchedulerFullException(String message) {
        super(message);
    }
}
