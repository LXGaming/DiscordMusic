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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.manager.AudioManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QueueCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("queue");
        addAlias("list");
        description("Displays all of the media that is queued.");
        permission("queue.base");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        
        BlockingQueue<AudioTrack> audioQueue = AudioManager.getAudioQueue(message.getGuild());
        if (audioQueue == null || audioQueue.isEmpty()) {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Nothing queued");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
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
        
        embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
        if ((audioQueue.size() - index) > 0) {
            embedBuilder.setFooter("and " + (audioQueue.size() - index) + " more...", null);
        }
        
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}