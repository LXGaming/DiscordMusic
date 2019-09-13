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

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.Color;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;

public class SourcesCommand extends AbstractCommand {
    
    public SourcesCommand() {
        addAlias("sources");
        setDescription("Displays the allows sources which media can be played from.");
        setPermission("command.sources");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
        if (!DiscordMusic.getInstance().getConfig().map(Config::getAllowedSources).isPresent()) {
            embedBuilder.setColor(MessageManager.getColor(Color.ERROR));
            embedBuilder.setTitle("Configuration error!");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        embedBuilder.setTitle("Sources:");
        for (String source : DiscordMusic.getInstance().getConfig().map(Config::getAllowedSources).get()) {
            if (embedBuilder.getDescriptionBuilder().length() != 0) {
                embedBuilder.getDescriptionBuilder().append("\n");
            }
            
            embedBuilder.getDescriptionBuilder().append(source);
        }
        
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}