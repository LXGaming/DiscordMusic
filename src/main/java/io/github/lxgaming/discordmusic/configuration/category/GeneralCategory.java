/*
 * Copyright 2019 Alex Thomson
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

package io.github.lxgaming.discordmusic.configuration.category;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import java.util.Set;

public class GeneralCategory {
    
    public static final int DEFAULT_VOLUME = 50;
    public static final int DEFAULT_MAX_VOLUME = 200;
    public static final int DEFAULT_SEARCH_LIMIT = 5;
    
    @SerializedName("debug")
    private boolean debug = false;
    
    @SerializedName("commandPrefix")
    private String commandPrefix = "!Music";
    
    @SerializedName("defaultVolume")
    private int defaultVolume = DEFAULT_VOLUME;
    
    @SerializedName("maxVolume")
    private int maxVolume = DEFAULT_MAX_VOLUME;
    
    @SerializedName("searchLimit")
    private int searchLimit = DEFAULT_SEARCH_LIMIT;
    
    @SerializedName("allowedSources")
    private Set<String> allowedSources = Sets.newLinkedHashSet();
    
    public GeneralCategory() {
        // Bandcamp
        allowedSources.add("bandcamp.com");
        
        // Nico
        allowedSources.add("nicovideo.jp");
        allowedSources.add("www.nicovideo.jp");
        
        // SoundCloud
        allowedSources.add("m.soundcloud.com");
        allowedSources.add("soundcloud.com");
        allowedSources.add("www.soundcloud.com");
        
        // Twitch
        allowedSources.add("go.twitch.tv");
        allowedSources.add("twitch.tv");
        allowedSources.add("www.twitch.tv");
        
        // Vimeo
        allowedSources.add("vimeo.com");
        
        // YouTube
        allowedSources.add("m.youtube.com");
        allowedSources.add("music.youtube.com");
        allowedSources.add("www.youtube.com");
        allowedSources.add("youtu.be");
        allowedSources.add("youtube.com");
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
    
    public int getSearchLimit() {
        return searchLimit;
    }
    
    public void setSearchLimit(int searchLimit) {
        this.searchLimit = searchLimit;
    }
    
    public Set<String> getAllowedSources() {
        return allowedSources;
    }
}