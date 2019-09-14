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

package io.github.lxgaming.discordmusic.configuration.category;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Activity;

public class AccountCategory {
    
    private long id;
    private String name;
    private String token;
    private String gameTitle;
    private Activity.ActivityType gameType;
    private OnlineStatus onlineStatus;
    private SpeakingMode speakingMode;
    
    public AccountCategory() {
        setId(0L);
        setName("Unknown");
        setToken("token");
        setGameTitle("music");
        setGameType(Activity.ActivityType.DEFAULT);
        setOnlineStatus(OnlineStatus.ONLINE);
        setSpeakingMode(SpeakingMode.VOICE);
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getGameTitle() {
        return gameTitle;
    }
    
    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }
    
    public Activity.ActivityType getGameType() {
        return gameType;
    }
    
    public void setGameType(Activity.ActivityType gameType) {
        this.gameType = gameType;
    }
    
    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }
    
    public void setOnlineStatus(OnlineStatus onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
    
    public SpeakingMode getSpeakingMode() {
        return speakingMode;
    }
    
    public void setSpeakingMode(SpeakingMode speakingMode) {
        this.speakingMode = speakingMode;
    }
}