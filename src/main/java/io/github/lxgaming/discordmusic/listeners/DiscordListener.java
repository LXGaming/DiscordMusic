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
import io.github.lxgaming.discordmusic.configuration.config.Account;
import io.github.lxgaming.discordmusic.configuration.config.Server;
import io.github.lxgaming.discordmusic.managers.AccountManager;
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
import org.apache.commons.lang3.StringUtils;

public class DiscordListener extends ListenerAdapter {
    
    @Override
    public void onReady(ReadyEvent event) {
        Account account = AccountManager.getAccount().orElse(null);
        if (account != null) {
            long id = event.getJDA().getSelfUser().getIdLong();
            String name = Toolbox.filter(event.getJDA().getSelfUser().getName());
            
            if (account.getId() != id || !StringUtils.equals(account.getName(), name)) {
                DiscordMusic.getInstance().getLogger().info("Account {} ({}) -> {} ({})", account.getName(), account.getId(), name, id);
                account.setId(id);
                account.setName(name);
                DiscordMusic.getInstance().getConfiguration().saveConfiguration();
            }
        }
        
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
        DiscordMusic.getInstance().getLogger().debug("GuildVoiceJoinEvent - Joined: {} ({})",
                event.getChannelJoined().getName(), event.getChannelJoined().getMembers().size());
        
        if (event.getChannelJoined().getMembers().size() < 2) {
            return;
        }
        
        if (event.getMember() == event.getGuild().getSelfMember() || event.getChannelJoined() == event.getGuild().getAudioManager().getConnectedChannel()) {
            AudioManager.play(event.getGuild());
        }
    }
    
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        DiscordMusic.getInstance().getLogger().debug("GuildVoiceLeaveEvent - Left: {} ({})",
                event.getChannelLeft().getName(), event.getChannelLeft().getMembers().size());
        
        if (event.getMember() == event.getGuild().getSelfMember()) {
            AudioManager.pause(event.getGuild());
            return;
        }
        
        if (event.getChannelLeft() == event.getGuild().getAudioManager().getConnectedChannel() && event.getChannelLeft().getMembers().size() < 2) {
            AudioManager.pause(event.getGuild());
        }
    }
    
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        DiscordMusic.getInstance().getLogger().debug("GuildVoiceMoveEvent - Joined: {} ({}), Left: {} ({})",
                event.getChannelJoined().getName(), event.getChannelJoined().getMembers().size(),
                event.getChannelLeft().getName(), event.getChannelLeft().getMembers().size());
        
        if (event.getMember() == event.getGuild().getSelfMember()) {
            if (event.getChannelJoined().getMembers().size() < 2) {
                AudioManager.pause(event.getGuild());
            } else {
                AudioManager.play(event.getGuild());
            }
            
            return;
        }
        
        if (event.getChannelLeft() == event.getGuild().getAudioManager().getConnectedChannel() && event.getChannelLeft().getMembers().size() < 2) {
            AudioManager.pause(event.getGuild());
            return;
        }
        
        if (event.getChannelJoined() == event.getGuild().getAudioManager().getConnectedChannel()) {
            AudioManager.play(event.getGuild());
        }
    }
}