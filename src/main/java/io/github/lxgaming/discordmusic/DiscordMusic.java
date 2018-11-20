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

package io.github.lxgaming.discordmusic;

import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.Configuration;
import io.github.lxgaming.discordmusic.managers.AccountManager;
import io.github.lxgaming.discordmusic.managers.CommandManager;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.managers.ServiceManager;
import io.github.lxgaming.discordmusic.util.Reference;
import io.github.lxgaming.discordmusic.util.ShutdownHook;
import io.github.lxgaming.discordmusic.util.Toolbox;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

public class DiscordMusic {
    
    private static DiscordMusic instance;
    private final Instant startTime;
    private final Logger logger;
    private final Path path;
    private final Configuration configuration;
    
    public DiscordMusic() {
        instance = this;
        startTime = Instant.now();
        logger = LogManager.getLogger(Reference.APP_ID);
        path = Toolbox.getPath().orElse(null);
        configuration = new Configuration();
    }
    
    public void loadDiscordMusic() {
        getLogger().info("Initializing...");
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        
        getLogger().info("Loading configuration...");
        getConfiguration().loadConfiguration();
        
        reloadLogger();
        AccountManager.buildAccount();
        AccountManager.reloadAccount();
        CommandManager.buildCommands();
        MessageManager.buildColors();
        ServiceManager.buildServices();
        getConfiguration().saveConfiguration();
        getLogger().info("{} v{} has loaded", Reference.APP_NAME, Reference.APP_VERSION);
    }
    
    public void reloadLogger() {
        if (getConfig().map(Config::isDebug).orElse(false)) {
            Configurator.setLevel(getLogger().getName(), Level.DEBUG);
            getLogger().debug("Debug mode enabled.");
        } else {
            Configurator.setLevel(getLogger().getName(), Level.INFO);
            getLogger().info("Debug mode disabled.");
        }
    }
    
    public static DiscordMusic getInstance() {
        return instance;
    }
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Path getPath() {
        return path;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public Optional<Config> getConfig() {
        if (getConfiguration() != null) {
            return Optional.ofNullable(getConfiguration().getConfig());
        }
        
        return Optional.empty();
    }
}