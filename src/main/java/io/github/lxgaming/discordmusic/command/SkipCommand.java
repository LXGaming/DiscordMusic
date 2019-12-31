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

public class SkipCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("skip");
        description("Skip the current media");
        permission("skip.base");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        
        AudioTrack audioTrack = AudioManager.getAudioPlayer(message.getGuild()).getPlayingTrack();
        if (audioTrack != null) {
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.getDescriptionBuilder()
                    .append("**Skipped** ")
                    .append("[").append(audioTrack.getInfo().title).append("](").append(audioTrack.getInfo().uri).append(")");
            AudioManager.playNext(message.getGuild());
        } else {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Nothing is currently playing.");
        }
        
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}