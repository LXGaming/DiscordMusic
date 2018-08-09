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

package io.github.lxgaming.discordmusic.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;

public final class DiscordData {
    
    private final Guild guild;
    private final Message message;
    
    public DiscordData(Message message) {
        this(message.getGuild(), message);
    }
    
    public DiscordData(Guild guild, Message message) {
        this.guild = guild;
        this.message = message;
    }
    
    public boolean isValid() {
        return getGuild() != null && getMessage() != null;
    }
    
    public Guild getGuild() {
        return guild;
    }
    
    public Message getMessage() {
        return message;
    }
    
    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("channel", getMessage().getChannel().getIdLong());
        jsonObject.addProperty("guild", getGuild().getIdLong());
        jsonObject.addProperty("message", getMessage().getIdLong());
        jsonObject.addProperty("user", getMessage().getAuthor().getIdLong());
        return new Gson().toJson(jsonObject);
    }
}