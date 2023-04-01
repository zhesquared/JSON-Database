package client.utils;

import com.beust.jcommander.Parameter;
import com.google.gson.JsonElement;

public class CommandArgs {

    @Parameter(names = {"--input", "-in"}, description = "input file name")
    private String fileName;

    @Parameter(names = {"--type", "-t"}, description = "operation type")
    private String type;

    @Parameter(names = {"--key", "-k"}, converter = JsonConverter.class)
    private JsonElement key;

    @Parameter(names = {"--value", "-v"}, converter = JsonConverter.class)
    private JsonElement value;


    public String getFileName() {
        return fileName;
    }

    public String getType() {
        return type;
    }

    public JsonElement getKey() {
        return key;
    }

    public JsonElement getValue() {
        return value;
    }
}