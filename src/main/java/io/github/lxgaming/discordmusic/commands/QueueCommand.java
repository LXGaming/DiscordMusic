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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.lxgaming.discordmusic.managers.AudioManager;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QueueCommand extends AbstractCommand {
    
    public QueueCommand() {
        addAlias("queue");
        addAlias("list");
        setDescription("Displays all of the media that is queued.");
        setPermission("command.queue");
    }
    
    @Override
    public void execute(TextChannel textChannel, Member member, Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(textChannel.getJDA().getSelfUser().getName(), null, textChannel.getJDA().getSelfUser().getEffectiveAvatarUrl());
        embedBuilder.setColor(Toolbox.DEFAULT);
        
        BlockingQueue<AudioTrack> audioQueue = AudioManager.getAudioQueue(member.getGuild());
        if (audioQueue == null || audioQueue.isEmpty()) {
            embedBuilder.setColor(Toolbox.WARNING);
            embedBuilder.setTitle("Nothing queued");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        int index = 0;
        for (AudioTrack audioTrack : audioQueue) {
            if (index >= 10) {
                continue;
            }
            
            index++;
            if (embedBuilder.getDescriptionBuilder().length() != 0) {
                embedBuilder.getDescriptionBuilder().append("\n");
            }
            
            embedBuilder.getDescriptionBuilder().append("**").append(index).append(".** ");
            embedBuilder.getDescriptionBuilder().append("[").append(audioTrack.getInfo().title).append("](").append(audioTrack.getInfo().uri).append(")");
        }
        
        embedBuilder.setColor(Toolbox.SUCCESS);
        if ((audioQueue.size() - index) > 0) {
            embedBuilder.setFooter("and " + (audioQueue.size() - index) + " more...", null);
        }
        
        MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
    }
}