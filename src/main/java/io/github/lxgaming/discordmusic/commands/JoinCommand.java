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

package io.github.lxgaming.discordmusic.commands;

import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.DiscordUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

public class JoinCommand extends AbstractCommand {
    
    public JoinCommand() {
        addAlias("join");
        setDescription("Connects to your current voice channel or one matching the name provided.");
        setPermission("command.join");
        setUsage("[Channel name]");
    }
    
    @Override
    public void execute(TextChannel textChannel, Member member, Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(DiscordUtil.DEFAULT);
        
        Set<VoiceChannel> voiceChannels = DiscordUtil.newLinkedHashSet();
        if (arguments.isEmpty()) {
            if (!member.getVoiceState().inVoiceChannel()) {
                embedBuilder.setColor(DiscordUtil.WARNING);
                embedBuilder.setTitle("You are not in a voice channel");
                MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
                return;
            }
            
            voiceChannels.add(member.getVoiceState().getChannel());
        } else {
            voiceChannels.addAll(member.getGuild().getVoiceChannelsByName(StringUtils.join(arguments, " "), true));
        }
        
        if (voiceChannels.isEmpty()) {
            embedBuilder.setColor(DiscordUtil.WARNING);
            embedBuilder.setTitle("Unable to find the specified voice channel");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        for (VoiceChannel voiceChannel : voiceChannels) {
            if (member.getGuild().getAudioManager().getConnectedChannel() == voiceChannel) {
                embedBuilder.getDescriptionBuilder().append("Already connected to ").append(voiceChannel.getName());
                continue;
            }
            
            try {
                member.getGuild().getAudioManager().openAudioConnection(voiceChannel);
                embedBuilder.setColor(DiscordUtil.SUCCESS);
                embedBuilder.getDescriptionBuilder().setLength(0);
                embedBuilder.setTitle("Joining " + voiceChannel.getName());
                MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
                return;
            } catch (GuildUnavailableException | IllegalArgumentException | UnsupportedOperationException ex) {
                embedBuilder.getDescriptionBuilder().setLength(0);
                embedBuilder.setColor(DiscordUtil.ERROR);
                embedBuilder.setTitle("Encountered an error");
                embedBuilder.getDescriptionBuilder().append(StringUtils.defaultIfBlank(ex.getMessage(), "Unknown"));
                MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
                return;
            } catch (InsufficientPermissionException ex) {
                embedBuilder.setTitle("Insufficient permission for " + voiceChannel.getName());
                embedBuilder.getDescriptionBuilder().append(StringUtils.defaultIfBlank(ex.getMessage(), "Unknown"));
            }
        }
        
        embedBuilder.setColor(DiscordUtil.WARNING);
        embedBuilder.setTitle("Failed to join voice channel");
        MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
    }
}