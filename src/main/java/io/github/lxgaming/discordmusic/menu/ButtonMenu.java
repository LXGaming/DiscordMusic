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
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class ButtonMenu extends Menu {
    
    private final Set<ActionRow> actionRows;
    private final Function<ButtonClickEvent, Boolean> action;
    private final Consumer<Message> finalAction;
    
    private ButtonMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                       Set<ActionRow> actionRows, Function<ButtonClickEvent, Boolean> action,
                       Consumer<Message> finalAction) {
        super(waiter, users, roles, timeout, unit);
        this.actionRows = actionRows;
        this.action = action;
        this.finalAction = finalAction;
    }
    
    public final void display(MessageChannel channel, String content) {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setActionRows(actionRows);
        messageBuilder.setContent(content);
        initialize(channel.sendMessage(messageBuilder.build()));
    }
    
    public final void display(MessageChannel channel, MessageEmbed messageEmbed) {
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setActionRows(actionRows);
        messageBuilder.setEmbed(messageEmbed);
        initialize(channel.sendMessage(messageBuilder.build()));
    }
    
    protected final void initialize(RestAction<Message> restAction) {
        restAction.queue(success -> {
            waitForEvent(success);
        }, failure -> DiscordMusic.getInstance().getLogger().error("Encountered an error while sending {}: {}", Toolbox.getClassSimpleName(getClass()), failure));
    }
    
    private void waitForEvent(Message message) {
        waiter.waitForEvent(ButtonClickEvent.class, event -> {
            if (event.getMessageIdLong() != message.getIdLong()) {
                return false;
            }
            
            if (!isValidUser(event.getUser(), event.getGuild())) {
                return false;
            }
            
            if (!event.isAcknowledged()) {
                event.deferReply().queue();
            }
            
            return true;
        }, (ButtonClickEvent event) -> {
            if (action.apply(event)) {
                waitForEvent(message);
            } else {
                finalAction.accept(message);
            }
        }, timeout, unit, () -> finalAction.accept(message));
    }
    
    public static class Builder extends Menu.Builder<Builder, ButtonMenu> {
        
        private final Set<ActionRow> actionRows = Sets.newLinkedHashSet();
        private Function<ButtonClickEvent, Boolean> action;
        private Consumer<Message> finalAction = message -> {
        };
        
        @Override
        public ButtonMenu build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!actionRows.isEmpty(), "Must have at least one actionRow");
            Checks.check(action != null, "Must provide an action consumer");
            Checks.check(finalAction != null, "Must provide an final action consumer");
            return new ButtonMenu(waiter, users, roles, timeout, unit, actionRows, action, finalAction);
        }
        
        public Builder addActionRows(ActionRow... actionRows) {
            Collections.addAll(this.actionRows, actionRows);
            return this;
        }
        
        public Builder setAction(Function<ButtonClickEvent, Boolean> action) {
            this.action = action;
            return this;
        }
        
        public Builder setFinalAction(Consumer<Message> finalAction) {
            this.finalAction = finalAction;
            return this;
        }
    }
}