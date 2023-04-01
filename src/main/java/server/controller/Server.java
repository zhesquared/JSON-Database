package server.controller;

import server.database.Database;

import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server {

    static int port;

    public static String path;

    static int serverAwaitBeforeShutdown;

    final static Database dataBase = new Database();

    static ReadWriteLock lock = new ReentrantReadWriteLock();

    static Lock readLock = lock.readLock();

    static Lock writeLock = lock.writeLock();

    static ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    static List<Runnable> unfinishedTasks;

    final static String EXIT = "exit";

    public static void start() {
        try (FileReader reader = new FileReader("./src/main/resources/app.properties")) {
            Properties properties = new Properties();
            properties.load(reader);
            port = Integer.parseInt(properties.getProperty("server.port"));
            serverAwaitBeforeShutdown = Integer.parseInt(properties.getProperty("server.await"));
            path = properties.getProperty("server.db");
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server started!");
            while (!executors.isShutdown()) {
                Socket socket = server.accept();
                System.out.println("New client is accepted");

                Future<String> task = executors.submit(new Session(socket));

                if (task.get().equals(EXIT)) {
                    executors.shutdown();

                    if (!executors.awaitTermination(serverAwaitBeforeShutdown, TimeUnit.MILLISECONDS)) {
                        unfinishedTasks = executors.shutdownNow();
                        System.out.println("Some incoming requests is in processed");
                    } else {
                        System.out.println("All requests processed");
                    }
                }
            }
        } catch (IOException | ExecutionException | InterruptedException exception) {
            exception.printStackTrace();
        }
    }
}
