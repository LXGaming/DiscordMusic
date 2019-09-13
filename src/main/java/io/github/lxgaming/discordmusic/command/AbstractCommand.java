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

package io.github.lxgaming.discordmusic.command;

import io.github.lxgaming.discordmusic.manager.CommandManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.core.entities.Message;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractCommand {
    
    private final Set<String> aliases = Toolbox.newLinkedHashSet();
    private final Set<AbstractCommand> children = Toolbox.newLinkedHashSet();
    private String description;
    private String permission;
    private String usage;
    
    public abstract void execute(Message message, List<String> arguments);
    
    protected final void addAlias(String alias) {
        CommandManager.registerAlias(this, alias);
    }
    
    protected final void addChild(Class<? extends AbstractCommand> commandClass) {
        CommandManager.registerCommand(this, commandClass);
    }
    
    public final Optional<String> getPrimaryAlias() {
        for (String alias : getAliases()) {
            if (StringUtils.isNotBlank(alias)) {
                return Optional.of(alias);
            }
        }
        
        return Optional.empty();
    }
    
    public final Set<String> getAliases() {
        return aliases;
    }
    
    public final Set<AbstractCommand> getChildren() {
        return children;
    }
    
    public final String getDescription() {
        return description;
    }
    
    protected final void setDescription(String description) {
        this.description = description;
    }
    
    public final String getPermission() {
        return permission;
    }
    
    protected final void setPermission(String permission) {
        this.permission = permission;
    }
    
    public final String getUsage() {
        return usage;
    }
    
    protected final void setUsage(String usage) {
        this.usage = usage;
    }
}