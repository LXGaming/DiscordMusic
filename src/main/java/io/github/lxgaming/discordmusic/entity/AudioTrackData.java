/*
 * Copyright 2020 Alex Thomson
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

package io.github.lxgaming.discordmusic.entity;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Objects;

public class AudioTrackData {
    
    private final Guild guild;
    private final MessageChannel channel;
    private final User user;
    
    private AudioTrackData(Guild guild, MessageChannel channel, User user) {
        this.guild = guild;
        this.channel = channel;
        this.user = user;
    }
    
    public static AudioTrackData of(Message message) {
        if (message.isFromType(ChannelType.TEXT)) {
            return new AudioTrackData(message.getGuild(), message.getChannel(), message.getAuthor());
        } else {
            return new AudioTrackData(null, message.getChannel(), message.getAuthor());
        }
    }
    
    public static AudioTrackData of(Guild guild, MessageChannel channel, User user) {
        return new AudioTrackData(guild, channel, user);
    }
    
    public Guild getGuild() {
        return guild;
    }
    
    public MessageChannel getChannel() {
        return channel;
    }
    
    public User getUser() {
        return user;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        AudioTrackData data = (AudioTrackData) obj;
        return Objects.equals(guild, data.guild)
                && Objects.equals(channel, data.channel)
                && Objects.equals(user, data.user);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(guild, channel, user);
    }
}