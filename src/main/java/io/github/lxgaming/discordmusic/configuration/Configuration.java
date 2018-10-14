/*
 * Copyright 2018 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.discordmusic.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import io.github.lxgaming.discordmusic.DiscordMusic;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Configuration {
    
    private final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();
    
    private Config config;
    
    public Configuration() {
        setConfig(new Config());
    }
    
    public void loadConfiguration() {
        setConfig((Config) loadObject(getConfig(), "config.json"));
        DiscordMusic.getInstance().getLogger().info("Loaded configuration files.");
    }
    
    public void saveConfiguration() {
        saveObject(getConfig(), "config.json");
        DiscordMusic.getInstance().getLogger().info("Saved configuration files.");
    }
    
    private Object loadObject(Object object, String name) {
        try {
            if (object == null || StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("Supplied arguments are null!");
            }
            
            File file = DiscordMusic.getInstance().getPath().resolve(name).toFile();
            if (!file.exists() && !saveObject(object, name)) {
                return object;
            }
            
            String string = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            if (StringUtils.isBlank(string)) {
                throw new IOException("File is blank!");
            }
            
            Object jsonObject = getGson().fromJson(string, object.getClass());
            if (jsonObject == null) {
                throw new JsonParseException("Failed to parse File!");
            }
            
            return jsonObject;
        } catch (IOException | OutOfMemoryError | RuntimeException ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error processing {}::loadObject", getClass().getSimpleName(), ex);
            return object;
        }
    }
    
    private boolean saveObject(Object object, String name) {
        try {
            if (object == null || StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("Supplied arguments are null!");
            }
            
            File file = DiscordMusic.getInstance().getPath().resolve(name).toFile();
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists() && parentFile.mkdirs()) {
                DiscordMusic.getInstance().getLogger().info("Successfully created directory {}.", parentFile.getName());
            }
            
            if (!file.exists() && file.createNewFile()) {
                DiscordMusic.getInstance().getLogger().info("Successfully created file {}.", file.getName());
            }
            
            Files.write(file.toPath(), getGson().toJson(object, object.getClass()).getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException | OutOfMemoryError | RuntimeException ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error processing {}::saveObject", getClass().getSimpleName(), ex);
            return false;
        }
    }
    
    private Gson getGson() {
        return gson;
    }
    
    public Config getConfig() {
        return config;
    }
    
    private void setConfig(Config config) {
        this.config = config;
    }
}