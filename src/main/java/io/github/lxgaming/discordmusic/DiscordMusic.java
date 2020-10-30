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
import io.github.lxgaming.discordmusic.configuration.category.GeneralCategory;
import io.github.lxgaming.discordmusic.manager.AccountManager;
import io.github.lxgaming.discordmusic.manager.AudioManager;
import io.github.lxgaming.discordmusic.manager.CommandManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.manager.TaskManager;
import io.github.lxgaming.discordmusic.task.MessageTask;
import io.github.lxgaming.discordmusic.util.ShutdownHook;
import io.github.lxgaming.discordmusic.util.Toolbox;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.time.Instant;
import java.util.Optional;

public class DiscordMusic {
    
    public static final String ID = "discordmusic";
    public static final String NAME = "DiscordMusic";
    public static final String VERSION = "@version@";
    public static final String AUTHORS = "LX_Gaming";
    public static final String SOURCE = "https://github.com/LXGaming/DiscordMusic";
    public static final String WEBSITE = "https://lxgaming.github.io/";
    
    private static DiscordMusic instance;
    private final Instant startTime;
    private final Logger logger;
    private final Configuration configuration;
    
    public DiscordMusic() {
        instance = this;
        this.startTime = Instant.now();
        this.logger = LogManager.getLogger(DiscordMusic.NAME);
        this.configuration = new Configuration(Toolbox.getPath());
    }
    
    public void load() {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        getLogger().info("Initializing...");
        if (!reload()) {
            getLogger().error("Failed to load");
            return;
        }
        
        // Internal
        TaskManager.prepare();
        
        // Discord
        MessageManager.prepare();
        CommandManager.prepare();
        AudioManager.prepare();
        AccountManager.prepare();
        
        getConfiguration().saveConfiguration();
        
        TaskManager.schedule(new MessageTask());
        getLogger().info("{} v{} has loaded", DiscordMusic.NAME, DiscordMusic.VERSION);
    }
    
    public boolean reload() {
        getConfiguration().loadConfiguration();
        if (!getConfig().isPresent()) {
            return false;
        }
        
        getConfiguration().saveConfiguration();
        reloadLogger();
        return true;
    }
    
    public void reloadLogger() {
        if (getConfig().map(Config::getGeneralCategory).map(GeneralCategory::isDebug).orElse(false)) {
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
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public Optional<Config> getConfig() {
        return Optional.ofNullable(getConfiguration().getConfig());
    }
}