package taskscheduler.java;

import taskscheduler.java.exceptions.ServerException;
import taskscheduler.java.other.PerformanceMonitor;
import taskscheduler.java.servers.Server;
import taskscheduler.java.tasks.Task;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

public class TaskScheduler {

    private static final Logger logger = Logger.getLogger(TaskScheduler.class.getName());

    // PriorityQueue to store servers with load balancing (lightest load first)
    private final PriorityBlockingQueue<Server> servers = new PriorityBlockingQueue<>(
            11,
            Comparator.comparing(Server::getTotalLoad, BigInteger::compareTo)  // Compare servers based on their load
    );

    // Adds a server (local or remote) to the scheduler
    public void addServer(Server server) {
        Objects.requireNonNull(server, "Server cannot be null");
        servers.add(server);  // Add to the unified server queue
    }

    // Schedules a task to the server with the least load
    public void scheduleTask(Task task) throws ServerException {
        Objects.requireNonNull(task, "Task cannot be null");

        // Poll the server with the least load (can be local or remote)
        Server leastLoadedServer = servers.poll();
        if (leastLoadedServer != null) {
            // Add the task to the least loaded server
            leastLoadedServer.addTask(task);

            // Reinsert the server back into the queue with updated load
            servers.add(leastLoadedServer);
        } else {
            throw new ServerException("No available server to schedule the task.", new Exception());
        }
    }

    // Executes tasks across all servers and returns the results
    public Map<Server, List<Task>> executeAll() throws ServerException {
        Map<Server, List<Task>> results = new HashMap<>();

        for (Server server : servers) {
            try {
                // Execute tasks on each server and collect the results
                List<Task> completedTasks = server.executeTasks();
                results.put(server, completedTasks);

                // Log server performance (optional)
                PerformanceMonitor performanceMonitor = new PerformanceMonitor(server);
                performanceMonitor.printStatistics();

            } catch (ServerException e) {
                throw new ServerException("Failed executing server tasks.", e);
            }
        }

        return results;
    }

    // Retrieve all servers (useful for external access, logging, etc.)
    public PriorityBlockingQueue<Server> getServers() {
        return new PriorityBlockingQueue<>(servers);
    }
}

