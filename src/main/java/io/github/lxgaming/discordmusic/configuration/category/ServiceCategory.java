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

package io.github.lxgaming.discordmusic.configuration.category;

import com.google.gson.annotations.SerializedName;

public class ServiceCategory {
    
    public static final int DEFAULT_CORE_POOL_SIZE = 5;
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    public static final long DEFAULT_KEEP_ALIVE_TIME = 60000L;
    
    @SerializedName("corePoolSize")
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    
    @SerializedName("maximumPoolSize")
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;
    
    @SerializedName("keepAliveTime")
    private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    
    public int getCorePoolSize() {
        return corePoolSize;
    }
    
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }
    
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }
    
    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }
    
    public long getKeepAliveTime() {
        return keepAliveTime;
    }
    
    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
}