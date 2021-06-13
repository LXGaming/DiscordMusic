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
import io.github.lxgaming.discordmusic.configuration.category.GeneralCategory;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.StringUtils;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.BooleanUtils;

import java.util.List;

public class DebugCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("debug");
        description("For development purposes.");
        permission("debug.base");
        usage("[State]");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        GeneralCategory generalCategory = DiscordMusic.getInstance().getConfig().map(Config::getGeneralCategory).orElse(null);
        if (generalCategory == null) {
            EmbedBuilder embedBuilder = MessageManager.createErrorEmbed("GeneralCategory is unavailable");
            MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
            return;
        }
        
        Boolean state;
        if (!arguments.isEmpty()) {
            String argument = arguments.remove(0);
            if (StringUtils.isNotBlank(argument)) {
                state = BooleanUtils.toBooleanObject(argument);
                if (state == null) {
                    EmbedBuilder embedBuilder = MessageManager.createErrorEmbed(String.format("Failed to parse %s as a boolean", Toolbox.escapeMarkdown(argument)));
                    MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
                    return;
                }
            } else {
                state = null;
            }
        } else {
            state = null;
        }
        
        if (state != null) {
            generalCategory.setDebug(state);
        } else {
            generalCategory.setDebug(!generalCategory.isDebug());
        }
        
        DiscordMusic.getInstance().getConfiguration().saveConfiguration();
        DiscordMusic.getInstance().reloadLogger();
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (generalCategory.isDebug()) {
            embedBuilder.setColor(MessageManager.getColor(Color.SUCCESS));
            embedBuilder.setTitle("Debugging enabled");
        } else {
            embedBuilder.setColor(MessageManager.getColor(Color.WARNING));
            embedBuilder.setTitle("Debugging disabled");
        }
        
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}