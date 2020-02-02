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
import io.github.lxgaming.discordmusic.configuration.category.ServiceCategory;
import io.github.lxgaming.discordmusic.service.Service;
import io.github.lxgaming.discordmusic.util.Toolbox;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ServiceManager {
    
    public static final ScheduledThreadPoolExecutor SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(0, Toolbox.newThreadFactory("Service Thread #%d"));
    
    public static void prepare() {
        ServiceCategory serviceCategory = DiscordMusic.getInstance().getConfig().map(Config::getServiceCategory).orElseThrow(NullPointerException::new);
        if (serviceCategory.getCorePoolSize() < 0) {
            DiscordMusic.getInstance().getLogger().warn("CorePoolSize is out of bounds. Resetting to {}", ServiceCategory.DEFAULT_CORE_POOL_SIZE);
            serviceCategory.setCorePoolSize(ServiceCategory.DEFAULT_CORE_POOL_SIZE);
        }
        
        if (serviceCategory.getMaximumPoolSize() < serviceCategory.getCorePoolSize()) {
            DiscordMusic.getInstance().getLogger().warn("MaximumPoolSize is out of bounds. Resetting to {}", ServiceCategory.DEFAULT_MAXIMUM_POOL_SIZE);
            serviceCategory.setMaximumPoolSize(ServiceCategory.DEFAULT_MAXIMUM_POOL_SIZE);
        }
        
        if (serviceCategory.getKeepAliveTime() <= 0) {
            DiscordMusic.getInstance().getLogger().warn("KeepAliveTime is out of bounds. Resetting to {}", ServiceCategory.DEFAULT_KEEP_ALIVE_TIME);
            serviceCategory.setKeepAliveTime(ServiceCategory.DEFAULT_KEEP_ALIVE_TIME);
        }
        
        SCHEDULED_EXECUTOR_SERVICE.setCorePoolSize(serviceCategory.getCorePoolSize());
        SCHEDULED_EXECUTOR_SERVICE.setMaximumPoolSize(serviceCategory.getMaximumPoolSize());
        SCHEDULED_EXECUTOR_SERVICE.setKeepAliveTime(serviceCategory.getKeepAliveTime(), TimeUnit.MILLISECONDS);
    }
    
    public static void schedule(Service service) {
        try {
            if (!service.prepare()) {
                DiscordMusic.getInstance().getLogger().warn("{} failed to prepare", Toolbox.getClassSimpleName(service.getClass()));
                return;
            }
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error while preparing {}", Toolbox.getClassSimpleName(service.getClass()), ex);
            return;
        }
        
        ScheduledFuture<?> scheduledFuture = schedule(service, service.getDelay(), service.getInterval());
        service.setScheduledFuture(scheduledFuture);
    }
    
    public static ScheduledFuture<?> schedule(Runnable runnable) {
        return schedule(runnable, 0L, 0L);
    }
    
    public static ScheduledFuture<?> schedule(Runnable runnable, long delay, long interval) {
        return schedule(runnable, delay, interval, TimeUnit.MILLISECONDS);
    }
    
    public static ScheduledFuture<?> schedule(Runnable runnable, long delay, long interval, TimeUnit unit) {
        try {
            if (interval <= 0L) {
                return SCHEDULED_EXECUTOR_SERVICE.schedule(runnable, Math.max(delay, 0L), unit);
            }
            
            return SCHEDULED_EXECUTOR_SERVICE.scheduleWithFixedDelay(runnable, Math.max(delay, 0L), Math.max(interval, 0L), unit);
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error while scheduling service", ex);
            return null;
        }
    }
    
    public static void shutdown() {
        try {
            SCHEDULED_EXECUTOR_SERVICE.shutdown();
            if (!SCHEDULED_EXECUTOR_SERVICE.awaitTermination(5000L, TimeUnit.MILLISECONDS)) {
                throw new InterruptedException();
            }
            
            DiscordMusic.getInstance().getLogger().info("Successfully terminated threads, continuing with shutdown process...");
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Failed to terminate threads, continuing with shutdown process...");
        }
    }
}