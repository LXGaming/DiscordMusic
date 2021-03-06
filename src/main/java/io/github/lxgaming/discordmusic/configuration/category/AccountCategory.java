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

import com.google.gson.annotations.SerializedName;
import io.github.lxgaming.discordmusic.DiscordMusic;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.audio.SpeakingMode;
import net.dv8tion.jda.api.entities.Activity;

public class AccountCategory {
    
    @SerializedName("id")
    private long id = 0L;
    
    @SerializedName("name")
    private String name = "Unknown";
    
    @SerializedName("token")
    private String token = "";
    
    @SerializedName("ownerId")
    private long ownerId = 0L;
    
    @SerializedName("activityTitle")
    private String activityTitle = DiscordMusic.NAME;
    
    @SerializedName("activityType")
    private Activity.ActivityType activityType = Activity.ActivityType.DEFAULT;
    
    @SerializedName("onlineStatus")
    private OnlineStatus onlineStatus = OnlineStatus.ONLINE;
    
    @SerializedName("speakingMode")
    private SpeakingMode speakingMode = SpeakingMode.VOICE;
    
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
    
    public long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getActivityTitle() {
        return activityTitle;
    }
    
    public Activity.ActivityType getActivityType() {
        return activityType;
    }
    
    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }
    
    public SpeakingMode getSpeakingMode() {
        return speakingMode;
    }
}