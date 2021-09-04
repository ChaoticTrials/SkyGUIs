package de.melanx.easyskyblockmanagement.config;

import io.github.noeppi_noeppi.libx.annotation.config.RegisterConfig;
import io.github.noeppi_noeppi.libx.config.Config;

import java.text.SimpleDateFormat;

@RegisterConfig(client = true)
public class ClientConfig {

    @Config({
            "See here for information how to configure this:",
            "https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/text/SimpleDateFormat.html",
            "Examples:",
            "dd.MM.yyyy HH:mm         - 07.12.1997 23:30",
            "yyyy-MM-dd'T'HH:mm:ss    - 1997-12-07T23:30:00",
            "EEE, d MMM yyyy HH:mm:ss - Sun, 7 Dec 1997 23:30:00"
    })
    public static SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy HH:mm");
}
