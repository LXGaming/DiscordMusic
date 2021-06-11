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

package io.github.lxgaming.discordmusic.listener;

import com.google.common.collect.Sets;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.AccountCategory;
import io.github.lxgaming.discordmusic.configuration.category.GuildCategory;
import io.github.lxgaming.discordmusic.manager.AudioManager;
import io.github.lxgaming.discordmusic.manager.CommandManager;
import io.github.lxgaming.discordmusic.manager.DiscordManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.manager.TaskManager;
import io.github.lxgaming.discordmusic.util.StringUtils;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class DiscordListener {
    
    @SubscribeEvent
    public void onReady(ReadyEvent event) {
        AccountCategory category = DiscordMusic.getInstance().getConfig().map(Config::getAccountCategory).orElse(null);
        if (category != null) {
            long id = event.getJDA().getSelfUser().getIdLong();
            String name = Toolbox.filter(event.getJDA().getSelfUser().getName());
            
            if (category.getId() != id || !StringUtils.equals(category.getName(), name)) {
                DiscordMusic.getInstance().getLogger().info("Account {} ({}) -> {} ({})", category.getName(), category.getId(), name, id);
                category.setId(id);
                category.setName(name);
            }
            
            if (category.getOwnerId() == 0L) {
                ApplicationInfo applicationInfo = event.getJDA().retrieveApplicationInfo().complete();
                DiscordMusic.getInstance().getLogger().info("OwnerId {} -> {}", category.getOwnerId(), applicationInfo.getOwner().getIdLong());
                category.setOwnerId(applicationInfo.getOwner().getIdLong());
            }
        }
        
        AudioSourceManagers.registerRemoteSources(AudioManager.AUDIO_PLAYER_MANAGER);
        for (Guild guild : event.getJDA().getGuilds()) {
            AudioManager.register(guild);
            DiscordManager.register(guild);
        }
        
        DiscordMusic.getInstance().getConfiguration().saveConfiguration();
    }
    
    @SubscribeEvent
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getMessage().isEdited()) {
            return;
        }
        
        if (event.isFromType(ChannelType.TEXT)) {
            TaskManager.schedule(() -> CommandManager.execute(event.getMessage()));
        }
    }
    
    @SubscribeEvent
    public void onMessageDelete(MessageDeleteEvent event) {
        MessageManager.removeMessages(Sets.newHashSet(event.getMessageId()));
    }
    
    @SubscribeEvent
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        MessageManager.removeMessages(event.getMessageIds());
    }
    
    @SubscribeEvent
    public void onGuildJoin(GuildJoinEvent event) {
        AudioManager.register(event.getGuild());
    }
    
    @SubscribeEvent
    public void onGuildLeave(GuildLeaveEvent event) {
        AudioManager.unregister(event.getGuild());
    }
    
    @SubscribeEvent
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        DiscordMusic.getInstance().getLogger().debug("GuildVoiceJoinEvent - Joined: {} ({})",
                event.getChannelJoined().getName(), event.getChannelJoined().getMembers().size());
        
        GuildCategory guildCategory = DiscordManager.getGuildCategory(event.getGuild());
        if (guildCategory == null) {
            DiscordMusic.getInstance().getLogger().warn("GuildCategory is unavailable");
            return;
        }
        
        if (event.getChannelJoined().getMembers().size() < 2) {
            return;
        }
        
        if (event.getMember() == event.getGuild().getSelfMember() || event.getChannelJoined() == event.getGuild().getAudioManager().getConnectedChannel()) {
            if (guildCategory.isAutoPlay()) {
                AudioManager.play(event.getGuild());
            }
        }
    }
    
    @SubscribeEvent
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        DiscordMusic.getInstance().getLogger().debug("GuildVoiceLeaveEvent - Left: {} ({})",
                event.getChannelLeft().getName(), event.getChannelLeft().getMembers().size());
        
        GuildCategory guildCategory = DiscordManager.getGuildCategory(event.getGuild());
        if (guildCategory == null) {
            DiscordMusic.getInstance().getLogger().warn("GuildCategory is unavailable");
            return;
        }
        
        if (event.getMember() == event.getGuild().getSelfMember()) {
            if (guildCategory.isAutoPause()) {
                AudioManager.pause(event.getGuild());
            }
            return;
        }
        
        if (event.getChannelLeft() == event.getGuild().getAudioManager().getConnectedChannel() && event.getChannelLeft().getMembers().size() < 2) {
            if (guildCategory.isAutoPlay()) {
                AudioManager.play(event.getGuild());
            }
        }
    }
    
    @SubscribeEvent
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        DiscordMusic.getInstance().getLogger().debug("GuildVoiceMoveEvent - Joined: {} ({}), Left: {} ({})",
                event.getChannelJoined().getName(), event.getChannelJoined().getMembers().size(),
                event.getChannelLeft().getName(), event.getChannelLeft().getMembers().size());
        
        GuildCategory guildCategory = DiscordManager.getGuildCategory(event.getGuild());
        if (guildCategory == null) {
            DiscordMusic.getInstance().getLogger().warn("GuildCategory is unavailable");
            return;
        }
        
        if (event.getMember() == event.getGuild().getSelfMember()) {
            if (event.getChannelJoined().getMembers().size() < 2) {
                if (guildCategory.isAutoPause()) {
                    AudioManager.pause(event.getGuild());
                }
            } else {
                if (guildCategory.isAutoPlay()) {
                    AudioManager.play(event.getGuild());
                }
            }
            
            return;
        }
        
        if (event.getChannelLeft() == event.getGuild().getAudioManager().getConnectedChannel() && event.getChannelLeft().getMembers().size() < 2) {
            if (guildCategory.isAutoPause()) {
                AudioManager.pause(event.getGuild());
            }
            return;
        }
        
        if (event.getChannelJoined() == event.getGuild().getAudioManager().getConnectedChannel()) {
            if (guildCategory.isAutoPlay()) {
                AudioManager.play(event.getGuild());
            }
        }
    }
}