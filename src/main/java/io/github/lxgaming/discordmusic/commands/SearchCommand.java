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

import io.github.lxgaming.discordmusic.handlers.AudioPlayerLoadResultHandler;
import io.github.lxgaming.discordmusic.managers.AudioManager;
import io.github.lxgaming.discordmusic.managers.MessageManager;
import io.github.lxgaming.discordmusic.util.DiscordData;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SearchCommand extends AbstractCommand {
    
    public SearchCommand() {
        addAlias("search");
        setDescription("Searches YouTube.");
        setPermission("command.search");
        setUsage("<Query...>");
    }
    
    @Override
    public void execute(TextChannel textChannel, Member member, Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Toolbox.DEFAULT);
        
        if (arguments.isEmpty()) {
            embedBuilder.setColor(Toolbox.ERROR);
            embedBuilder.setTitle("Invalid arguments");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        String query = Toolbox.filter(String.join(" ", arguments));
        if (StringUtils.isBlank(query)) {
            embedBuilder.setColor(Toolbox.ERROR);
            embedBuilder.setTitle("Invalid query");
            MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
            return;
        }
        
        AudioManager.getAudioPlayerManager().loadItem("ytsearch: " + query, new AudioPlayerLoadResultHandler(new DiscordData(message, textChannel, member)));
        
        embedBuilder.setTitle("Search query");
        embedBuilder.appendDescription(query);
        MessageManager.sendMessage(textChannel, embedBuilder.build(), true);
    }
}