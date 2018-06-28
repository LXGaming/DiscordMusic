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

package io.github.lxgaming.discordmusic.managers;

import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.services.AbstractService;
import io.github.lxgaming.discordmusic.util.Toolbox;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServiceManager {
    
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(10, Toolbox.buildThreadFactory("Service Thread #%d"));
    
    public static void buildServices() {
        DiscordMusic.getInstance().getConfig().map(Config::getMessageService).ifPresent(ServiceManager::schedule);
    }
    
    private static void schedule(AbstractService abstractService) {
        try {
            long delay = Math.max(abstractService.getDelay(), 0L);
            long period = Math.max(abstractService.getPeriod(), 0L);
            if (!abstractService.isPeriodical() || period <= 0L) {
                abstractService.setScheduledFuture(getScheduledExecutorService().schedule(abstractService, delay, TimeUnit.MILLISECONDS));
                return;
            }
            
            abstractService.setScheduledFuture(getScheduledExecutorService().scheduleWithFixedDelay(abstractService, delay, period, TimeUnit.MILLISECONDS));
        } catch (RuntimeException ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error processing {}::schedule", "ServerManager", ex);
        }
    }
    
    public static ScheduledExecutorService getScheduledExecutorService() {
        return SCHEDULED_EXECUTOR_SERVICE;
    }
}