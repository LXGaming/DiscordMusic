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

import io.github.lxgaming.discordmusic.configuration.config.Account;
import io.github.lxgaming.discordmusic.configuration.config.Server;
import io.github.lxgaming.discordmusic.services.MessageService;
import io.github.lxgaming.discordmusic.util.Color;
import io.github.lxgaming.discordmusic.util.Toolbox;

import java.util.Map;
import java.util.Set;

public class Config {
    
    private boolean debug;
    private String commandPrefix;
    private int defaultVolume;
    private int maxVolume;
    private boolean deleteInvoking;
    private boolean deleteMessages;
    private Account account;
    private Set<String> allowedSources;
    private Set<Server> servers;
    private Map<Color, String> colors;
    private MessageService messageService;
    
    public Config() {
        setDebug(false);
        setCommandPrefix("M!");
        setDefaultVolume(25);
        setMaxVolume(150);
        setDeleteInvoking(true);
        setDeleteMessages(true);
        setAccount(new Account());
        setAllowedSources(Toolbox.newLinkedHashSet(
                "bandcamp.com", // Bandcamp
                "beam.pro", "mixer.com", "www.beam.pro", "www.mixer.com", // Beam
                "nicovideo.jp", "www.nicovideo.jp", // Nico
                "m.soundcloud.com", "soundcloud.com", "www.soundcloud.com", // SoundCloud
                "go.twitch.tv", "twitch.tv", "www.twitch.tv", // Twitch
                "vimeo.com", // Vimeo
                "m.youtube.com", "music.youtube.com", "www.youtube.com", "youtu.be", "youtube.com" // YouTube
        ));
        setServers(Toolbox.newLinkedHashSet());
        setColors(Toolbox.newHashMap());
        setMessageService(new MessageService());
    }
    
    public boolean isDebug() {
        return debug;
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    public String getCommandPrefix() {
        return commandPrefix;
    }
    
    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }
    
    public int getDefaultVolume() {
        return defaultVolume;
    }
    
    public void setDefaultVolume(int defaultVolume) {
        this.defaultVolume = defaultVolume;
    }
    
    public int getMaxVolume() {
        return maxVolume;
    }
    
    public void setMaxVolume(int maxVolume) {
        this.maxVolume = maxVolume;
    }
    
    public boolean isDeleteInvoking() {
        return deleteInvoking;
    }
    
    public void setDeleteInvoking(boolean deleteInvoking) {
        this.deleteInvoking = deleteInvoking;
    }
    
    public boolean isDeleteMessages() {
        return deleteMessages;
    }
    
    public void setDeleteMessages(boolean deleteMessages) {
        this.deleteMessages = deleteMessages;
    }
    
    public Account getAccount() {
        return account;
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }
    
    public Set<String> getAllowedSources() {
        return allowedSources;
    }
    
    public void setAllowedSources(Set<String> allowedSources) {
        this.allowedSources = allowedSources;
    }
    
    public Set<Server> getServers() {
        return servers;
    }
    
    public void setServers(Set<Server> servers) {
        this.servers = servers;
    }
    
    public Map<Color, String> getColors() {
        return colors;
    }
    
    public void setColors(Map<Color, String> colors) {
        this.colors = colors;
    }
    
    public MessageService getMessageService() {
        return messageService;
    }
    
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
}