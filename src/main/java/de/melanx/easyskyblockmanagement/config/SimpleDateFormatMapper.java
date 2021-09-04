package de.melanx.easyskyblockmanagement.config;

import com.google.gson.JsonPrimitive;
import io.github.noeppi_noeppi.libx.annotation.config.RegisterMapper;
import io.github.noeppi_noeppi.libx.config.ValueMapper;

import java.text.SimpleDateFormat;

@RegisterMapper
public class SimpleDateFormatMapper implements ValueMapper<SimpleDateFormat, JsonPrimitive> {

    @Override
    public Class<SimpleDateFormat> type() {
        return SimpleDateFormat.class;
    }

    @Override
    public Class<JsonPrimitive> element() {
        return JsonPrimitive.class;
    }

    @Override
    public SimpleDateFormat fromJson(JsonPrimitive json) {
        return new SimpleDateFormat(json.getAsString());
    }

    @Override
    public JsonPrimitive toJson(SimpleDateFormat value) {
        return new JsonPrimitive(value.toPattern());
    }
}
