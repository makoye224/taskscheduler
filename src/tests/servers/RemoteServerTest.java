package tests.servers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskscheduler.java.exceptions.ServerException;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.other.Duration;
import taskscheduler.java.other.RetryPolicy;
import taskscheduler.java.other.TaskPriority;
import taskscheduler.java.servers.Server;
import taskscheduler.java.tasks.Task;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteServerTest {
    public static class ServerTest {
        private Server server;
        private Task successfulTask;
        private Task failedTask;
        private Task timeoutTask;
        private Task dependentTaskA;
        private Task dependentTaskB;

        @BeforeEach
        public void setUp() {
            RetryPolicy retryPolicy = new RetryPolicy(3, 2, true);
            server = new Server(retryPolicy );
            successfulTask = new MockTask(true, 0);  // Task that will complete successfully
            failedTask = new MockTask(false, 0);     // Task that will fail to complete
            timeoutTask = new MockTask(true, 5000);  // Task that will fail due to a timeout of 5 seconds
            dependentTaskA = new MockTask(true, 0);  // Task A that will complete successfully
            dependentTaskB = new MockTask(true, 0);  // Task B that depends on A and will complete
        }

        @Test
        public void testAddTask() throws ServerException {
            server.addTask(successfulTask);
            assertEquals(1, server.getTasks().size(), "Task should be added to the server.");
        }

        @Test
        public void testExecuteTasks() throws ServerException {
            server.addTask(successfulTask);
            server.addTask(failedTask);
            List<Task> completedTasks = server.executeTasks();
            assertEquals(1, completedTasks.size(), "Only one task should complete successfully.");
            assertTrue(completedTasks.contains(successfulTask), "The completed tasks should contain the successful task.");
        }

        @Test
        public void testGetFailedTasks() throws ServerException {
            server.addTask(successfulTask);
            server.addTask(failedTask);
            server.executeTasks(); // Execute to determine which task fails
            List<Task> failedTasks = server.getFailedTasks();
            assertEquals(1, failedTasks.size(), "Only the task that failed should be in the failed list.");
            assertTrue(failedTasks.contains(failedTask), "The failed tasks list should contain the failed task.");
        }

        @Test
        public void testTaskTimeout() throws ServerException {
            // Add a task that will fail due to timeout
            server.addTask(timeoutTask);
            server.executeTasks();
            List<Task> failedTasks = server.getFailedTasks();
            assertEquals(1, failedTasks.size(), "The task should fail due to timeout.");
            assertTrue(failedTasks.contains(timeoutTask), "The failed tasks list should contain the timeout task.");
        }

        @Test
        public void testDependentTasks() throws ServerException {
            // Task B depends on A
            ((MockTask) dependentTaskB).addDependency(dependentTaskA.getId());

            // Add both tasks to the server
            server.addTask(dependentTaskA);
            server.addTask(dependentTaskB);

            // Execute tasks
            List<Task> completedTasks = server.executeTasks();

            // Both tasks should complete successfully
            assertEquals(2, completedTasks.size(), "Both dependent tasks should complete successfully.");
            assertTrue(completedTasks.contains(dependentTaskA), "Task A should be completed.");
            assertTrue(completedTasks.contains(dependentTaskB), "Task B should be completed.");
        }

        // Mock Task class for testing
        private static class MockTask implements Task {
            private final boolean willComplete;
            private final long timeout;
            private boolean isCompleted = false;
            private Set<String> dependencies;

            MockTask(boolean willComplete, long timeout) {
                this.willComplete = willComplete;
                this.timeout = timeout;
                this.dependencies = Set.of(); // No dependencies by default
            }

            @Override
            public String getId() {
                return "Task-" + hashCode();
            }

            @Override
            public void execute() throws TaskException {
                if (timeout > 0 && willComplete) {
                    // Simulate a long-running task
                    try {
                        Thread.sleep(timeout + 2000);  // Simulate task exceeding timeout
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    throw new TaskException("Task timed out", new Throwable());
                } else if (!willComplete) {
                    throw new TaskException("Task failed to complete", new Throwable());
                } else {
                    isCompleted = true;
                }
            }

            @Override
            public boolean isCompleted() {
                return isCompleted;
            }

            @Override
            public Duration getEstimatedDuration() {
                return new Duration(BigInteger.valueOf(timeout));
            }

            @Override
            public TaskPriority getPriority() {
                return TaskPriority.LOW;
            }

            @Override
            public Set<String> getDependencies() {
                return dependencies;
            }

            public void addDependency(String taskId) {
                dependencies = Set.of(taskId);
            }

            @Override
            public long getTimeout() {
                return timeout;
            }

            @Override
            public void setTimeout(long timeout) {
                // Mock method - Not used in tests
            }
        }
    }
}
