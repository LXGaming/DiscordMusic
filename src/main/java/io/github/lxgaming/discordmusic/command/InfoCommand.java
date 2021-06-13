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

import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.entity.Color;
import io.github.lxgaming.discordmusic.manager.MessageManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Message;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InfoCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("info");
        addAlias("information");
        addAlias("version");
        description("Displays bot information.");
        permission("info.base");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(DiscordMusic.NAME + " v" + DiscordMusic.VERSION, DiscordMusic.SOURCE, message.getJDA().getSelfUser().getEffectiveAvatarUrl());
        embedBuilder.setColor(MessageManager.getColor(Color.DEFAULT));
        embedBuilder.addField("Uptime", Toolbox.getDuration(getUptime(), TimeUnit.MILLISECONDS, false, TimeUnit.SECONDS), false);
        embedBuilder.addField("Authors", DiscordMusic.AUTHORS, false);
        embedBuilder.addField("Source", DiscordMusic.SOURCE, false);
        embedBuilder.addField("Website", DiscordMusic.WEBSITE, false);
        embedBuilder.addField("Java Version", System.getProperty("java.version"), false);
        embedBuilder.addField("Dependencies", ""
                + "\n- " + "JDA v" + JDAInfo.VERSION
                + "\n- " + "JDA-Utilities v" + JDAUtilitiesInfo.VERSION
                + "\n- " + "LavaPlayer v" + PlayerLibrary.VERSION, false);
        MessageManager.sendTemporaryMessage(message.getChannel(), embedBuilder.build());
    }
    
    private long getUptime() {
        return Duration.between(DiscordMusic.getInstance().getStartTime(), Instant.now()).toMillis();
    }
}