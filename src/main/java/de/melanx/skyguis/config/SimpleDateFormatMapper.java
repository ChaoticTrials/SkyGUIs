package de.melanx.skyguis.config;

import com.google.gson.JsonPrimitive;
import org.moddingx.libx.annotation.config.RegisterMapper;
import org.moddingx.libx.config.gui.ConfigEditor;
import org.moddingx.libx.config.mapper.ValueMapper;
import org.moddingx.libx.config.validator.ValidatorInfo;

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

    @Override
    public ConfigEditor<SimpleDateFormat> createEditor(ValidatorInfo<?> validator) {
        return ConfigEditor.unsupported(new SimpleDateFormat("dd.MM.yyyy HH:mm"));
    }
}
