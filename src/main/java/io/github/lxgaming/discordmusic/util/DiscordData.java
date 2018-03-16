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
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public final class DiscordData {
    
    private final Guild guild;
    private final Message message;
    private final TextChannel textChannel;
    private final User user;
    
    public DiscordData(Message message) {
        this(message, message.getTextChannel(), message.getMember());
    }
    
    public DiscordData(Message message, TextChannel textChannel, Member member) {
        this(member.getGuild(), message, textChannel, member.getUser());
    }
    
    public DiscordData(Guild guild, Message message, TextChannel textChannel, User user) {
        this.guild = guild;
        this.message = message;
        this.textChannel = textChannel;
        this.user = user;
    }
    
    public boolean isValid() {
        return !(getGuild() == null || getMessage() == null || getTextChannel() == null || getUser() == null);
    }
    
    public Guild getGuild() {
        return guild;
    }
    
    public Message getMessage() {
        return message;
    }
    
    public TextChannel getTextChannel() {
        return textChannel;
    }
    
    public User getUser() {
        return user;
    }
    
    @Override
    public String toString() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("guild", DiscordUtil.getIdLong(getGuild()).orElse(0L));
        jsonObject.addProperty("message", DiscordUtil.getIdLong(getMessage()).orElse(0L));
        jsonObject.addProperty("textChannel", DiscordUtil.getIdLong(getTextChannel()).orElse(0L));
        jsonObject.addProperty("user", DiscordUtil.getIdLong(getUser()).orElse(0L));
        return new Gson().toJson(jsonObject);
    }
}