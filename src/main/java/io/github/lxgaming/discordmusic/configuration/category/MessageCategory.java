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

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.entity.Emote;

import java.util.Map;

public class MessageCategory {
    
    @SerializedName("actionTimeout")
    private long actionTimeout = 900000L; // 15 Minute
    
    @SerializedName("colors")
    private Map<Color, String> colors = Maps.newHashMap();
    
    @SerializedName("deleteInterval")
    private long deleteInterval = 300000L; // 5 Minute
    
    @SerializedName("deleteInvoking")
    private boolean deleteInvoking = true;
    
    @SerializedName("deleteMessages")
    private boolean deleteMessages = true;
    
    @SerializedName("emotes")
    private Map<Emote, String> emotes = Maps.newHashMap();
    
    @SerializedName("sendTyping")
    private boolean sendTyping = true;
    
    public long getActionTimeout() {
        return actionTimeout;
    }
    
    public Map<Color, String> getColors() {
        return colors;
    }
    
    public long getDeleteInterval() {
        return deleteInterval;
    }
    
    public boolean isDeleteInvoking() {
        return deleteInvoking;
    }
    
    public boolean isDeleteMessages() {
        return deleteMessages;
    }
    
    public Map<Emote, String> getEmotes() {
        return emotes;
    }
    
    public boolean isSendTyping() {
        return sendTyping;
    }
}