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

package io.github.lxgaming.discordmusic.command;

import com.google.common.collect.Sets;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.List;
import java.util.Set;

public class JoinCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("join");
        description("Connects to your current voice channel or one matching the name provided.");
        permission("join.base");
        usage("[Channel name]");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        
        Set<VoiceChannel> voiceChannels = Sets.newLinkedHashSet();
        if (arguments.isEmpty()) {
            if (!message.getMember().getVoiceState().inVoiceChannel()) {
                embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
                embedBuilder.setTitle("You are not in a voice channel");
                MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
                return;
            }
            
            voiceChannels.add(message.getMember().getVoiceState().getChannel());
        } else {
            voiceChannels.addAll(message.getGuild().getVoiceChannelsByName(StringUtils.join(arguments, " "), true));
        }
        
        if (voiceChannels.isEmpty()) {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Unable to find the specified voice channel");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        for (VoiceChannel voiceChannel : voiceChannels) {
            if (message.getGuild().getAudioManager().getConnectedChannel() == voiceChannel) {
                embedBuilder.getDescriptionBuilder().append("Already connected to ").append(voiceChannel.getName());
                continue;
            }
            
            try {
                message.getGuild().getAudioManager().openAudioConnection(voiceChannel);
                embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
                embedBuilder.getDescriptionBuilder().setLength(0);
                embedBuilder.setTitle("Joining " + voiceChannel.getName());
                MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
                return;
            } catch (IllegalArgumentException | UnsupportedOperationException ex) {
                embedBuilder.getDescriptionBuilder().setLength(0);
                embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
                embedBuilder.setTitle("An error has occurred.");
                embedBuilder.getDescriptionBuilder().append("```").append(StringUtils.defaultIfBlank(ex.getMessage(), "Unknown")).append("```");
                MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
                return;
            } catch (InsufficientPermissionException ex) {
                embedBuilder.setTitle("Insufficient permission for " + voiceChannel.getName());
                embedBuilder.getDescriptionBuilder().append(StringUtils.defaultIfBlank(ex.getMessage(), "Unknown"));
            }
        }
        
        embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
        embedBuilder.setTitle("Failed to join voice channel");
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}