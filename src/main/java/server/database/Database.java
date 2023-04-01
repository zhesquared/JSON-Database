package server.database;

import com.google.gson.*;
import server.controller.Session;

import java.io.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class Database {

    public final static String PATH = "./src/main/java/server/data/db.json";
    private JsonObject database;
    private JsonElement currentElement;
    private String complexKey = null;

    private final Gson gson = new Gson();

    public Database() {

        try {
            File dbFile = new File(PATH);
            if (dbFile.createNewFile()) {
                System.out.println("New database file is created: " + PATH);
                database = new JsonObject();
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(PATH));
                database = gson.fromJson(reader.readLine(), JsonObject.class);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public BiConsumer<JsonElement, JsonElement> set = (key, value) -> {
        JsonObject currentDatabase = database;
        if (key.isJsonPrimitive()) {
            database.add(key.getAsString(), value);
        } else {
            JsonArray keys = key.getAsJsonArray();
            String newKey = keys.remove(keys.size() - 1).getAsString();
            for (var currentKey : keys) {
                if (!currentDatabase.has(currentKey.getAsString()) ||
                        currentDatabase.get(currentKey.getAsString()).isJsonPrimitive()) {
                    currentDatabase.add(currentKey.getAsString(), new JsonObject());
                }
                currentDatabase = (JsonObject) currentDatabase.get(currentKey.getAsString());
            }
            currentDatabase.add(newKey, value);
        }
        write();
    };

    public Consumer<JsonElement> delete = (key) -> {
        var Object = key.isJsonPrimitive()
                ? database.remove(key.getAsString())
                : currentElement.getAsJsonObject().remove(complexKey);
        write();
    };

    public Function<JsonElement, JsonElement> get = (key) -> {
        if (key.isJsonPrimitive() || key.getAsJsonArray().size() == 1) {
            return database.get(key.getAsString());
        }
        return currentElement;
    };

    public Predicate<JsonElement> hasKey = (key) -> {
        Boolean isContainKey = false;
        currentElement = database;
        if (key.isJsonPrimitive() || key.getAsJsonArray().size() == 1) {
            isContainKey = database.has(key.getAsString());
        } else {
            JsonArray keys = key.getAsJsonArray();
            complexKey = keys.get(keys.size() - 1).getAsString();
            for (int i = 0; i < keys.size() - 1; i++) {
                currentElement = ((JsonObject) currentElement).get(keys.get(i).getAsString());
                if (!currentElement.isJsonObject()) {
                    isContainKey = false;
                    break;
                }
                isContainKey = ((JsonObject) currentElement).has(complexKey);
            }
            if (Session.request.getType().equals("get")) {
                currentElement = ((JsonObject) currentElement).get(complexKey);
            }
        }
        return isContainKey;
    };

    private void write() {
        try (FileWriter writer = new FileWriter(PATH)) {
            writer.write(new GsonBuilder().create().toJson(database));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}