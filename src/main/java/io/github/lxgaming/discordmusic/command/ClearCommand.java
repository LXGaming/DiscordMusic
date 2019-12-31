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
import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.manager.AudioManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ClearCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("clear");
        description("Clears all of the media that is queued.");
        permission("clear.base");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        
        BlockingQueue<AudioTrack> audioQueue = AudioManager.getAudioQueue(message.getGuild());
        if (audioQueue == null) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("AudioQueue is unavailable");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        if (!audioQueue.isEmpty()) {
            audioQueue.clear();
            AudioManager.playNext(message.getGuild());
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.setTitle("Queue cleared");
        } else {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Nothing queued");
        }
        
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}