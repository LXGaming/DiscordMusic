/*
 * Copyright 2021 Alex Thomson
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

package io.github.lxgaming.discordmusic.menu;

import com.google.common.collect.Sets;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReactionMenu extends Menu {
    
    private final Set<String> reactions;
    private final Function<MessageReactionAddEvent, Boolean> action;
    private final Consumer<Message> finalAction;
    
    private ReactionMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                         Set<String> reactions, Function<MessageReactionAddEvent, Boolean> action,
                         Consumer<Message> finalAction) {
        super(waiter, users, roles, timeout, unit);
        this.reactions = reactions;
        this.action = action;
        this.finalAction = finalAction;
    }
    
    @Override
    public final void display(MessageChannel channel, String content) {
        initialize(channel.sendMessage(content));
    }
    
    @Override
    public final void display(MessageChannel channel, MessageEmbed messageEmbed) {
        initialize(channel.sendMessage(messageEmbed));
    }
    
    public final void display(MessageChannel channel, Message message) {
        initialize(channel.sendMessage(message));
    }
    
    @Override
    protected final void initialize(RestAction<Message> restAction) {
        restAction.queue(success -> {
            for (String reaction : reactions) {
                Emote emote = getEmote(success.getJDA(), reaction);
                if (emote != null) {
                    success.addReaction(emote).queue();
                } else {
                    success.addReaction(reaction).queue();
                }
            }
            
            waitForEvent(success);
        }, failure -> DiscordMusic.getInstance().getLogger().error("Encountered an error while sending {}: {}", Toolbox.getClassSimpleName(getClass()), failure));
    }
    
    private void waitForEvent(Message message) {
        waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
            if (event.getMessageIdLong() != message.getIdLong()) {
                return false;
            }
            
            String reaction;
            if (event.getReactionEmote().isEmote()) {
                reaction = event.getReactionEmote().getId();
            } else {
                reaction = event.getReactionEmote().getName();
            }
            
            if (!reactions.contains(reaction)) {
                return false;
            }
            
            return isValidUser(event.getUser(), event.getGuild());
        }, (MessageReactionAddEvent event) -> {
            if (action.apply(event)) {
                waitForEvent(message);
            } else {
                finalAction.accept(message);
            }
        }, timeout, unit, () -> finalAction.accept(message));
    }
    
    private Emote getEmote(JDA jda, String id) {
        try {
            return jda.getEmoteById(id);
        } catch (Exception ex) {
            return null;
        }
    }
    
    public static class Builder extends Menu.Builder<Builder, ReactionMenu> {
        
        private final Set<String> reactions = Sets.newLinkedHashSet();
        private Function<MessageReactionAddEvent, Boolean> action;
        private Consumer<Message> finalAction = message -> {
        };
        
        @Override
        public ReactionMenu build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!reactions.isEmpty(), "Must have at least one reactions");
            Checks.check(action != null, "Must provide an action consumer");
            Checks.check(finalAction != null, "Must provide an final action consumer");
            return new ReactionMenu(waiter, users, roles, timeout, unit, reactions, action, finalAction);
        }
        
        public Builder addReactions(String... emojis) {
            Collections.addAll(this.reactions, emojis);
            return this;
        }
        
        public Builder addReactions(Emote... emotes) {
            for (Emote emote : emotes) {
                addReactions(emote.getId());
            }
            
            return this;
        }
        
        public Builder setAction(Function<MessageReactionAddEvent, Boolean> action) {
            this.action = action;
            return this;
        }
        
        public Builder setFinalAction(Consumer<Message> finalAction) {
            this.finalAction = finalAction;
            return this;
        }
    }
}