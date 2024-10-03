package taskscheduler.java.other;

import java.math.BigInteger;

public class Duration {
    private BigInteger duration;  // Stores duration in a numeric format that can handle large values

    // Constructor to initialize Duration with a BigInteger value
    public Duration(BigInteger duration) {
        this.duration = duration;
    }

    // Copy constructor for defensive copying
    public Duration(Duration other) {
        if (other == null || other.getDuration() == null) {
            throw new IllegalArgumentException("Duration to copy cannot be null");
        }
        this.duration = new BigInteger(other.getDuration().toString());  // Defensive copy using a new BigInteger instance
    }

    // Getter method to retrieve the duration value
    public BigInteger getDuration() {
        return duration;
    }

    // Setter method to update the duration value
    public void setDuration(BigInteger duration) {
        this.duration = duration;
    }

    // Method to add a specified duration to the current duration
    public void addDuration(BigInteger duration) {
        this.duration = this.duration.add(duration);
    }

    // Method to subtract a specified duration from the current duration
    public void subtractDuration(BigInteger duration) {
        this.duration = this.duration.subtract(duration);
    }

    // Compares this duration with another Duration object
    public int compareDurations(Duration durationInput) {
        return this.duration.compareTo(durationInput.getDuration());
    }

    // Static factory method to create Duration from milliseconds
    public static Duration ofMillis(long millis) {
        return new Duration(BigInteger.valueOf(millis));
    }
}

