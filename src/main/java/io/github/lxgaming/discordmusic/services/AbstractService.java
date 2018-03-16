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

package io.github.lxgaming.discordmusic.services;

import io.github.lxgaming.discordmusic.DiscordMusic;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractService implements Runnable {
    
    private long delay;
    private long period;
    private transient ScheduledFuture scheduledFuture;
    
    public final void run() {
        try {
            execute();
        } catch (Exception ex) {
            DiscordMusic.getInstance().getLogger().error("Encountered an error processing {}::run", getClass().getSimpleName(), ex);
            getScheduledFuture().cancel(false);
        }
    }
    
    public abstract void execute();
    
    public abstract boolean isPeriodical();
    
    public boolean isRunning() {
        return getScheduledFuture() != null && (!getScheduledFuture().isDone() || getScheduledFuture().getDelay(TimeUnit.MILLISECONDS) > 0L);
    }
    
    public long getDelay() {
        return delay;
    }
    
    protected void setDelay(long delay) {
        this.delay = delay;
    }
    
    public long getPeriod() {
        return period;
    }
    
    protected void setPeriod(long period) {
        this.period = period;
    }
    
    public ScheduledFuture getScheduledFuture() {
        return scheduledFuture;
    }
    
    public void setScheduledFuture(ScheduledFuture scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }
}