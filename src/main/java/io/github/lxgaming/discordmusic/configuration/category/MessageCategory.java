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

import io.github.lxgaming.discordmusic.data.Color;
import io.github.lxgaming.discordmusic.util.Toolbox;

import java.util.Map;

public class MessageCategory {
    
    private long actionTimeout = 900000L;
    private Map<Color, String> colors = Toolbox.newHashMap();
    private long deleteInternal = 60000L;
    private boolean deleteInvoking = true;
    private boolean deleteMessages = true;
    
    public long getActionTimeout() {
        return actionTimeout;
    }
    
    public Map<Color, String> getColors() {
        return colors;
    }
    
    public long getDeleteInternal() {
        return deleteInternal;
    }
    
    public boolean isDeleteInvoking() {
        return deleteInvoking;
    }
    
    public boolean isDeleteMessages() {
        return deleteMessages;
    }
}