package client.utils;

import com.beust.jcommander.IStringConverter;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class JsonConverter implements IStringConverter<JsonElement> {
    @Override
    public JsonElement convert(String value) {
        return new JsonPrimitive(value);
    }
}
