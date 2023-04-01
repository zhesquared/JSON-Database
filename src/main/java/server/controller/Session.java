package server.controller;

import client.utils.CommandArgs;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Callable;

import static server.controller.Server.*;


public class Session implements Callable<String> {

    private final Socket socket;

    Session(Socket socket) {
        this.socket = socket;
    }

    final static Gson gson = new Gson();

    final static Map<String, String> OK = Map.of(
            "response", "OK");

    final static JsonObject ERROR = gson.toJsonTree(Map.of(
            "response", "ERROR",
            "reason", "No such key"))
            .getAsJsonObject();

    String serverMessage = "Server is running";

    private final JsonObject okResponse = gson.toJsonTree(OK).getAsJsonObject();

    public static CommandArgs request = null;

    public String call() {
        JsonObject response = ERROR;

        try (DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream()))
        {
            String inputString = input.readUTF();

            request = gson.fromJson(inputString, CommandArgs.class);

            switch (request.getType()) {
                case "get" -> {
                    readLock.lock();
                    if (dataBase.hasKey.test(request.getKey())) {
                        response = okResponse;
                        response.add("value", dataBase.get.apply(request.getKey()));
                    }
                    readLock.unlock();
                }

                // DELETE
                case "delete" -> {
                    writeLock.lock();
                    if (dataBase.hasKey.test(request.getKey())) {
                        dataBase.delete.accept(request.getKey());
                        response = okResponse;
                    }
                    writeLock.unlock();
                }

                // SET
                case "set" -> {
                    writeLock.lock();
                    dataBase.set.accept(request.getKey(), request.getValue());
                    response = okResponse;
                    writeLock.unlock();
                }

                // TERMINATE SESSION
                case EXIT -> {
                    response = okResponse;
                    serverMessage = EXIT;
                }
            }

            // SEND TO CLIENT
            output.writeUTF(gson.toJson(response));

        } catch (IOException e) {
            System.out.printf("Client session failed at initiation!\nMsg:%s%n", e.getMessage());
        }

        return serverMessage;
    }
}