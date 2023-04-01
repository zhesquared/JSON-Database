package client.controller;

import client.utils.CommandArgs;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class Client {

    private static String serverURL;

    private static int port;

    private static String path;

    private final static Gson gson = new Gson();

    static String outputMessage;

    public static void start(CommandArgs parameters) {
        try (FileReader reader = new FileReader("./src/main/resources/app.properties")) {
            Properties properties = new Properties();
            properties.load(reader);
            port = Integer.parseInt(properties.getProperty("server.port"));
            serverURL = properties.getProperty("server.url");
            path = properties.getProperty("client.db");
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        try (Socket socket = new Socket(serverURL, port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("Client started!");

            if (null == parameters.getType()) {
                outputMessage = new BufferedReader(new FileReader(path + parameters.getFileName()))
                        .readLine();
            } else {
                outputMessage = gson.toJson(parameters);
            }

            output.writeUTF(outputMessage);
            System.out.println("Sent:" + outputMessage);

            String inputMessage = input.readUTF();
            System.out.println("Received:" + inputMessage);

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
