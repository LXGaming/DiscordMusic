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

import io.github.lxgaming.discordmusic.util.Toolbox;

import java.util.Set;

public class GeneralCategory {
    
    public static final int DEFAULT_VOLUME = 50;
    public static final int DEFAULT_MAX_VOLUME = 200;
    public static final int DEFAULT_SEARCH_LIMIT = 5;
    
    private boolean debug = false;
    private String commandPrefix = "!Music";
    private int defaultVolume = DEFAULT_VOLUME;
    private int maxVolume = DEFAULT_MAX_VOLUME;
    private int searchLimit = DEFAULT_SEARCH_LIMIT;
    private Set<String> allowedSources = Toolbox.newLinkedHashSet(
            "bandcamp.com", // Bandcamp
            "beam.pro", "mixer.com", "www.beam.pro", "www.mixer.com", // Mixer
            "nicovideo.jp", "www.nicovideo.jp", // Nico
            "m.soundcloud.com", "soundcloud.com", "www.soundcloud.com", // SoundCloud
            "go.twitch.tv", "twitch.tv", "www.twitch.tv", // Twitch
            "vimeo.com", // Vimeo
            "m.youtube.com", "music.youtube.com", "www.youtube.com", "youtu.be", "youtube.com" // YouTube
    );
    
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