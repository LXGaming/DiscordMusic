/*
 * Copyright 2019 Alex Thomson
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

package io.github.lxgaming.discordmusic.service;

import io.github.lxgaming.discordmusic.DiscordMusic;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class Service implements Runnable {
    
    private long delay;
    private long interval;
    private ScheduledFuture<?> scheduledFuture;
    
    public final void run() {
        try {
            execute();
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error while executing {}", getClass().getSimpleName(), ex);
            getScheduledFuture().cancel(false);
        }
    }
    
    public abstract boolean prepare();
    
    public abstract void execute() throws Exception;
    
    public boolean isRunning() {
        return getScheduledFuture() != null && (!getScheduledFuture().isDone() || getScheduledFuture().getDelay(TimeUnit.MILLISECONDS) > 0L);
    }
    
    public final long getDelay() {
        return delay;
    }
    
    protected final void delay(long delay) {
        this.delay = delay;
    }
    
    public final long getInterval() {
        return interval;
    }
    
    protected final void interval(long interval) {
        this.interval = interval;
    }
    
    public final ScheduledFuture<?> getScheduledFuture() {
        return scheduledFuture;
    }
    
    public final void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }
}