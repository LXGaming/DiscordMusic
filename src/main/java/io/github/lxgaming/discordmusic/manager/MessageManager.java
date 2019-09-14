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

package io.github.lxgaming.discordmusic.manager;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.VerificationLevelException;
import net.dv8tion.jda.internal.entities.DataMessage;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MessageManager {
    
    private static final List<Message> MESSAGES = Collections.synchronizedList(Toolbox.newArrayList());
    
    public static void buildColors() {
        Map<Color, String> colors = DiscordMusic.getInstance().getConfig().map(Config::getColors).orElse(null);
        if (colors == null) {
            return;
        }
        
        colors.putIfAbsent(Color.ERROR, "#C13737");
        colors.putIfAbsent(Color.SUCCESS, "#46A84B");
        colors.putIfAbsent(Color.WARNING, "#EAA245");
        
        for (Color color : Color.values()) {
            colors.putIfAbsent(color, "#7289DA");
        }
    }
    
    public static void sendMessage(MessageChannel messageChannel, MessageEmbed messageEmbed) {
        sendMessage(messageChannel, new MessageBuilder().setEmbed(messageEmbed).build());
    }
    
    public static void sendTemporaryMessage(MessageChannel messageChannel, MessageEmbed messageEmbed) {
        sendTemporaryMessage(messageChannel, new MessageBuilder().setEmbed(messageEmbed).build());
    }
    
    public static void sendMessage(MessageChannel messageChannel, Message message) {
        sendMessage(messageChannel, message, success -> {
        });
    }
    
    public static void sendTemporaryMessage(MessageChannel messageChannel, Message message) {
        sendMessage(messageChannel, message, success -> {
            if (DiscordMusic.getInstance().getConfig().map(Config::isDeleteMessages).orElse(false)) {
                getMessages().add(success);
            }
        });
    }
    
    public static void sendMessage(MessageChannel messageChannel, Message message, Consumer<Message> success) {
        sendMessage(messageChannel, message, success, failure -> DiscordMusic.getInstance().getLogger().error("Encountered an error processing {}::sendMessage", "MessageManager", failure));
    }
    
    public static void sendMessage(MessageChannel messageChannel, Message message, Consumer<Message> success, Consumer<Throwable> failure) {
        try {
            messageChannel.sendMessage(message).queue(success, failure);
        } catch (IllegalArgumentException | InsufficientPermissionException | UnsupportedOperationException | VerificationLevelException ex) {
            failure.accept(ex);
        }
    }
    
    public static int getColor(Color color) {
        return DiscordMusic.getInstance().getConfig().map(Config::getColors).map(colors -> colors.get(color)).map(NumberUtils::createInteger).orElse(0);
    }
    
    public static String getMessageContent(Message message) {
        if (message instanceof DataMessage) {
            return message.getContentRaw();
        }
        
        return message.getContentDisplay();
    }
    
    public static void removeMessages(Collection<String> messages) {
        getMessages().removeIf(message -> messages.contains(message.getId()));
    }
    
    public static List<Message> getMessages() {
        return MESSAGES;
    }
}