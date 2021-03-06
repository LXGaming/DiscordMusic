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

package io.github.lxgaming.discordmusic.command;

import io.github.lxgaming.discordmusic.command.permission.ListPermissionCommand;
import io.github.lxgaming.discordmusic.command.permission.SetPermissionCommand;
import io.github.lxgaming.discordmusic.command.permission.UnsetPermissionCommand;
import io.github.lxgaming.discordmusic.manager.CommandManager;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class PermissionCommand extends Command {
    
    @Override
    public boolean prepare() {
        addAlias("permission");
        addChild(ListPermissionCommand.class);
        addChild(SetPermissionCommand.class);
        addChild(UnsetPermissionCommand.class);
        description("Base permission command");
        permission("permission.base");
        return true;
    }
    
    @Override
    public void execute(Message message, List<String> arguments) throws Exception {
        Command command = CommandManager.getCommand(HelpCommand.class);
        if (command != null) {
            command.execute(message, getPath());
        }
    }
}