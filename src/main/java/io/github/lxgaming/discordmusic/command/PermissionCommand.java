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

import io.github.lxgaming.discordmusic.command.permission.AddPermissionCommand;
import io.github.lxgaming.discordmusic.command.permission.ListPermissionCommand;
import io.github.lxgaming.discordmusic.command.permission.RemovePermissionCommand;
import io.github.lxgaming.discordmusic.manager.CommandManager;
import io.github.lxgaming.discordmusic.util.Toolbox;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class PermissionCommand extends AbstractCommand {
    
    public PermissionCommand() {
        addAlias("permission");
        addChild(AddPermissionCommand.class);
        addChild(ListPermissionCommand.class);
        addChild(RemovePermissionCommand.class);
        setDescription("Base permission command");
        setPermission("permission.base");
    }
    
    @Override
    public void execute(Message message, List<String> arguments) {
        CommandManager.getCommand(HelpCommand.class).ifPresent(command -> command.execute(message, getPrimaryAlias().map(Toolbox::newArrayList).orElseGet(Toolbox::newArrayList)));
    }
}