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

import io.github.lxgaming.discordmusic.data.AudioTrackData;
import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.handler.AudioPlayerLoadResultHandler;
import io.github.lxgaming.discordmusic.manager.AudioManager;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SearchCommand extends AbstractCommand {
    
    public SearchCommand() {
        addAlias("search");
        setDescription("Searches YouTube.");
        setPermission("search.base");
        setUsage("<Query...>");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        
        if (arguments.isEmpty()) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("Invalid arguments");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        String query = String.join(" ", arguments);
        if (StringUtils.isBlank(query)) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("Invalid query");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        AudioManager.AUDIO_PLAYER_MANAGER.loadItem("ytsearch: " + query, new AudioPlayerLoadResultHandler(AudioTrackData.of(message)));
        
        String sanitizedQuery = sanitize(query);
        embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
        embedBuilder.getDescriptionBuilder()
                .append("**Searching for **")
                .append("[").append(sanitizedQuery).append("]")
                .append("(").append("https://www.youtube.com/results?search_query=").append(sanitizedQuery).append(")");
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
    
    private String sanitize(String sequence) {
        return MarkdownSanitizer.sanitize(sequence)
                .replace("[", "\\[").replace("]", "\\]")
                .replace("(", "\\(").replace(")", "\\)")
                .replace(' ', '+');
    }
}