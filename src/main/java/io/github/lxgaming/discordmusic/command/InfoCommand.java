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

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.Color;
import io.github.lxgaming.discordmusic.util.Reference;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Message;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class InfoCommand extends AbstractCommand {
    
    public InfoCommand() {
        addAlias("info");
        addAlias("version");
        setDescription("Displays bot information.");
        setPermission("command.info");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(Reference.APP_NAME + " v" + Reference.APP_VERSION, Reference.SOURCE, message.getJDA().getSelfUser().getEffectiveAvatarUrl());
        embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
        embedBuilder.addField("Uptime", Toolbox.getTimeString(Duration.between(DiscordMusic.getInstance().getStartTime(), Instant.now()).toMillis()), false);
        embedBuilder.addField("Authors", Reference.AUTHORS, false);
        embedBuilder.addField("Source", Reference.SOURCE, false);
        embedBuilder.addField("Website", Reference.WEBSITE, false);
        embedBuilder.addField("Dependencies", ""
                + "\n- " + "JDA (Java Discord API) v" + JDAInfo.VERSION
                + "\n- " + "LavaPlayer v" + PlayerLibrary.VERSION, false);
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
}