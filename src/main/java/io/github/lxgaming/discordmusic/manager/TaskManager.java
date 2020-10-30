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

import io.github.lxgaming.common.task.Task;
import io.github.lxgaming.discordmusic.DiscordMusic;
import io.github.lxgaming.discordmusic.configuration.Config;
import io.github.lxgaming.discordmusic.configuration.category.TaskCategory;
import io.github.lxgaming.discordmusic.util.Toolbox;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class TaskManager {
    
    public static final ScheduledThreadPoolExecutor SCHEDULED_EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(0, Toolbox.newThreadFactory("Service Thread #%d"));
    
    public static void prepare() {
        TaskCategory taskCategory = DiscordMusic.getInstance().getConfig().map(Config::getTaskCategory).orElseThrow(NullPointerException::new);
        if (taskCategory.getCorePoolSize() < 0) {
            DiscordMusic.getInstance().getLogger().warn("CorePoolSize is out of bounds. Resetting to {}", TaskCategory.DEFAULT_CORE_POOL_SIZE);
            taskCategory.setCorePoolSize(TaskCategory.DEFAULT_CORE_POOL_SIZE);
        }
        
        if (taskCategory.getShutdownTimeout() < 0) {
            DiscordMusic.getInstance().getLogger().warn("ShutdownTimeout is out of bounds. Resetting to {}", TaskCategory.DEFAULT_SHUTDOWN_TIMEOUT);
            taskCategory.setShutdownTimeout(TaskCategory.DEFAULT_SHUTDOWN_TIMEOUT);
        }
        
        SCHEDULED_EXECUTOR_SERVICE.setCorePoolSize(taskCategory.getCorePoolSize());
    }
    
    public static void schedule(Task task) {
        try {
            if (!task.prepare()) {
                DiscordMusic.getInstance().getLogger().warn("{} failed to prepare", Toolbox.getClassSimpleName(task.getClass()));
                return;
            }
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error while preparing {}", Toolbox.getClassSimpleName(task.getClass()), ex);
            return;
        }
        
        try {
            task.schedule(SCHEDULED_EXECUTOR_SERVICE);
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error while scheduling {}", Toolbox.getClassSimpleName(task.getClass()), ex);
        }
    }
    
    public static ScheduledFuture<?> schedule(Runnable runnable) {
        return SCHEDULED_EXECUTOR_SERVICE.schedule(runnable, 0L, TimeUnit.MILLISECONDS);
    }
    
    public static void shutdown() {
        try {
            long timeout = DiscordMusic.getInstance().getConfig()
                    .map(Config::getTaskCategory)
                    .map(TaskCategory::getShutdownTimeout)
                    .orElse(TaskCategory.DEFAULT_SHUTDOWN_TIMEOUT);
            
            SCHEDULED_EXECUTOR_SERVICE.shutdown();
            if (!SCHEDULED_EXECUTOR_SERVICE.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                throw new InterruptedException();
            }
            
            DiscordMusic.getInstance().getLogger().info("Successfully terminated threads, continuing with shutdown process...");
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Failed to terminate threads, continuing with shutdown process...");
        }
    }
}