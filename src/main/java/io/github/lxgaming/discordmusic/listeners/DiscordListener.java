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

package io.github.lxgaming.discordmusic.listeners;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.config.Server;
import io.github.lxgaming.discordmusic.managers.AudioManager;
import io.github.lxgaming.discordmusic.managers.CommandManager;
import io.github.lxgaming.discordmusic.managers.GroupManager;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordListener extends ListenerAdapter {
    
    @Override
    public void onReady(ReadyEvent event) {
        AudioSourceManagers.registerRemoteSources(AudioManager.getAudioPlayerManager());
        for (Guild guild : event.getJDA().getGuilds()) {
            AudioManager.registerAudioPlayer(guild);
            GroupManager.registerServer(guild);
            GroupManager.getServer(guild).map(Server::getAutoJoinChannel).map(guild::getVoiceChannelById).ifPresent(guild.getAudioManager()::openAudioConnection);
        }
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getMessage().isEdited() || event.getAuthor().isFake()) {
            return;
        }
        
        if (event.isFromType(ChannelType.TEXT)) {
            CommandManager.process(event.getMessage());
        }
    }
    
    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        MessageManager.removeMessages(Toolbox.newLinkedHashSet(event.getMessageId()));
    }
    
    @Override
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        MessageManager.removeMessages(event.getMessageIds());
    }
    
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        AudioManager.registerAudioPlayer(event.getGuild());
    }
    
    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        AudioManager.unregisterAudioPlayer(event.getGuild());
    }
    
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getChannelJoined().getMembers().size() < 2) {
            return;
        }
        
        if (event.getMember() == event.getGuild().getSelfMember() || event.getChannelJoined() == event.getGuild().getAudioManager().getConnectedChannel()) {
            AudioManager.play(event.getGuild());
        }
    }
    
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getMember() == event.getGuild().getSelfMember()) {
            AudioManager.getAudioPlayer(event.getGuild()).setPaused(true);
            return;
        }
        
        if (event.getChannelLeft() == event.getGuild().getAudioManager().getConnectedChannel() && event.getChannelLeft().getMembers().size() <= 1) {
            AudioManager.getAudioPlayer(event.getGuild()).setPaused(true);
        }
    }
    
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getMember() == event.getGuild().getSelfMember()) {
            if (event.getChannelJoined().getMembers().size() <= 1) {
                AudioManager.getAudioPlayer(event.getGuild()).setPaused(true);
            } else {
                AudioManager.play(event.getGuild());
            }
            
            return;
        }
        
        if (event.getChannelLeft() == event.getGuild().getAudioManager().getConnectedChannel() && event.getChannelLeft().getMembers().size() <= 1) {
            AudioManager.getAudioPlayer(event.getGuild()).setPaused(true);
            return;
        }
        
        if (event.getChannelJoined() == event.getGuild().getAudioManager().getConnectedChannel()) {
            AudioManager.play(event.getGuild());
        }
    }
}