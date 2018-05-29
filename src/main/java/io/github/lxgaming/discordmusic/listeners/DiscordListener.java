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
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.config.Server;
import io.github.lxgaming.discordmusic.managers.AudioManager;
import io.github.lxgaming.discordmusic.managers.CommandManager;
import io.github.lxgaming.discordmusic.managers.GroupManager;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

public class DiscordListener extends ListenerAdapter {
    
    @Override
    public void onReady(ReadyEvent event) {
        DiscordMusic.getInstance().getConfig().map(Config::getAccount).ifPresent(account -> {
            if (account.getJDA() != null && StringUtils.isNotBlank(account.getGameTitle()) || account.getGameType() != null) {
                account.getJDA().getPresence().setGame(Game.of(account.getGameType(), account.getGameTitle()));
            }
        });
        
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
            CommandManager.process(event.getTextChannel(), event.getMember(), event.getMessage());
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
    
    /*
    public void testing(VoiceChannel voiceChannel) {
        AudioPlayer audioPlayer = AudioManager.getAudioPlayer(voiceChannel.getGuild());
        if (audioPlayer == null) {
            return;
        }
        
        if (voiceChannel.getMembers().size() == 1) {
            audioPlayer.setPaused(true);
        } else {
        
        }
    }
    
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        DiscordMusic.getInstance().getLogger().debug("GuildVoiceJoinEvent");
        if (event.getMember() == event.getGuild().getSelfMember() && event.getChannelJoined() == event.getGuild().getAudioManager().getQueuedAudioConnection()) {
            AudioPlayer audioPlayer = AudioManager.getAudioPlayer(event.getGuild());
            if (audioPlayer != null && event.getChannelJoined().getMembers().size() >= 1) {
            
            }
        }
        
        if (event.getChannelJoined() == event.getGuild().getAudioManager().getQueuedAudioConnection() && event.getChannelJoined().getMembers().size() == 2) {
            AudioPlayer audioPlayer = AudioManager.getAudioPlayer(event.getGuild());
            if (audioPlayer != null) {
                audioPlayer.setPaused(false);
                if (audioPlayer.getPlayingTrack() == null) {
                    AudioManager.playNext(event.getGuild());
                }
            }
        }
    }
    
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        DiscordMusic.getInstance().getLogger().debug("GuildVoiceLeaveEvent");
        if (event.getChannelLeft() == event.getGuild().getAudioManager().getConnectedChannel() && event.getChannelLeft().getMembers().size() == 1) {
            AudioPlayer audioPlayer = AudioManager.getAudioPlayer(event.getGuild());
            if (audioPlayer != null) {
                audioPlayer.setPaused(true);
            }
        }
    }
    
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        DiscordMusic.getInstance().getLogger().debug("GuildVoiceMoveEvent");
        if (event.getChannelLeft() == event.getGuild().getAudioManager().getConnectedChannel() && event.getChannelLeft().getMembers().size() == 1) {
            AudioPlayer audioPlayer = AudioManager.getAudioPlayer(event.getGuild());
            if (audioPlayer != null) {
                audioPlayer.setPaused(true);
            }
        }
        
        if (event.getChannelJoined() == event.getGuild().getAudioManager().getConnectedChannel() && event.getChannelJoined().getMembers().size() == 1) {
            AudioPlayer audioPlayer = AudioManager.getAudioPlayer(event.getGuild());
            if (audioPlayer != null) {
                audioPlayer.setPaused(true);
            }
        }
        
        if (event.getChannelJoined() == event.getGuild().getAudioManager().getConnectedChannel() && event.getChannelJoined().getMembers().size() == 2) {
            AudioPlayer audioPlayer = AudioManager.getAudioPlayer(event.getGuild());
            if (audioPlayer != null) {
                audioPlayer.setPaused(false);
                if (audioPlayer.getPlayingTrack() == null) {
                    AudioManager.playNext(event.getGuild());
                }
            }
        }
    }
    */
}