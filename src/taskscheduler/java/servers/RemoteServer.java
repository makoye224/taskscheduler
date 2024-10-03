package taskscheduler.java.servers;

import taskscheduler.java.exceptions.ServerException;
import taskscheduler.java.exceptions.TaskException;
import taskscheduler.java.other.RetryPolicy;
import taskscheduler.java.tasks.Task;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteServer extends Server {

    private final String remoteHost;
    private final int remotePort;
    private static final Logger logger = Logger.getLogger(RemoteServer.class.getName());

    // Constructor for RemoteServer that specifies the remote host and port
    public RemoteServer(String remoteHost, int remotePort, RetryPolicy retryPolicy) {
        super(retryPolicy);  // Call the parent Server class constructor with retry policy
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    // Override the executeTasks() method to send tasks to the remote server for execution
    @Override
    public List<Task> executeTasks() throws ServerException {
        List<Task> completedTasks = new ArrayList<>();

        // Process all tasks in the queue (inherited from the superclass) and send them to the remote server
        while (!getTasks().isEmpty()) {  // taskQueue is inherited from the Server class
            Task task = getTasks().poll();
            if (task != null) {
                try (Socket socket = new Socket(remoteHost, remotePort);
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                    // Send the serialized task to the remote server
                    out.writeObject(task);
                    out.flush();

                    // Wait for the response (ACK or FAILED)
                    String response = (String) in.readObject();
                    if ("ACK".equals(response)) {
                        logger.log(Level.INFO, "Task {0} executed successfully on remote server.", task.getId());
                        completedTasks.add(task);
                    } else {
                        logger.log(Level.SEVERE, "Task {0} execution failed on remote server.", task.getId());
                        throw new TaskException("Remote server failed to execute task: " + task.getId(), new Throwable());
                    }

                } catch (IOException | ClassNotFoundException | TaskException e) {
                    logger.log(Level.SEVERE, "Failed to execute task {0} on remote server: {1}", new Object[]{task.getId(), e.getMessage()});
                    throw new ServerException("Failed to communicate with remote server.", e);
                }
            }
        }

        return completedTasks;
    }

    @Override
    public String toString() {
        return String.format("RemoteServer [host=%s, port=%d]", remoteHost, remotePort);
    }
}
