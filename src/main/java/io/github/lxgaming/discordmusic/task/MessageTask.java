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

package io.github.lxgaming.discordmusic.task;

import io.github.lxgaming.common.task.Task;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.MessageCategory;
import io.github.lxgaming.discordmusic.manager.MessageManager;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class MessageTask extends Task {
    
    @Override
    public boolean prepare() {
        interval(1L, TimeUnit.SECONDS);
        type(Type.FIXED_DELAY);
        return true;
    }
    
    @Override
    public void execute() throws Exception {
        Instant deleteTime = DiscordMusic.getInstance().getConfig()
                .map(Config::getMessageCategory)
                .map(MessageCategory::getDeleteInterval)
                .map(deleteInterval -> Instant.now().minusMillis(deleteInterval))
                .orElse(null);
        if (deleteTime == null) {
            DiscordMusic.getInstance().getLogger().warn("Failed to calculate delete time");
            return;
        }
        
        MessageManager.MESSAGES.removeIf(message -> {
            if (message.getIdLong() == 0L) {
                return true;
            }
            
            if (message.getTimeCreated().toInstant().isAfter(deleteTime)) {
                return false;
            }
            
            message.delete().queue(
                    success -> DiscordMusic.getInstance().getLogger().debug("Successfully deleted Message {}", message.getIdLong()),
                    failure -> DiscordMusic.getInstance().getLogger().error("Encountered an error deleting message {}", message.getIdLong(), failure)
            );
            
            return true;
        });
    }
}