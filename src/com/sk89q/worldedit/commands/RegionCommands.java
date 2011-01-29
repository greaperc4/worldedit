// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.commands;

import java.util.Set;
import com.sk89q.util.commands.Command;
import com.sk89q.util.commands.CommandContext;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.filters.GaussianKernel;
import com.sk89q.worldedit.filters.HeightMapFilter;
import com.sk89q.worldedit.patterns.*;
import com.sk89q.worldedit.regions.Region;

/**
 * Region related commands.
 * 
 * @author sk89q
 */
public class RegionCommands {
    @Command(
        aliases = {"//set"},
        usage = "<block>",
        desc = "Set all the blocks inside the selection to a block",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.region.set"})
    public static void set(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        
        int affected;
        
        if (pattern instanceof SingleBlockPattern) {
            affected = editSession.setBlocks(session.getRegion(),
                    ((SingleBlockPattern)pattern).getBlock());
        } else {
            affected = editSession.setBlocks(session.getRegion(), pattern);
        }
        
        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = {"//replace"},
        usage = "[from-block] <to-block>",
        desc = "Replace all blocks in the selection with another",
        min = 1,
        max = 2
    )
    @CommandPermissions({"worldedit.region.replace"})
    public static void replace(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        Set<Integer> from;
        Pattern to;
        if (args.argsLength() == 1) {
            from = null;
            to = we.getBlockPattern(player, args.getString(0));
        } else {
            from = we.getBlockIDs(player, args.getString(0), true);
            to = we.getBlockPattern(player, args.getString(1));
        }

        int affected = 0;
        if (to instanceof SingleBlockPattern) {
            affected = editSession.replaceBlocks(session.getRegion(), from,
                    ((SingleBlockPattern)to).getBlock());
        } else {
            affected = editSession.replaceBlocks(session.getRegion(), from, to);
        }
        
        player.print(affected + " block(s) have been replaced.");
    }
    
    @Command(
        aliases = {"//overlay"},
        usage = "<block>",
        desc = "Set a block on top of blocks in the region",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.region.overlay"})
    public static void overlay(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        BaseBlock block = we.getBlock(player, args.getString(0));

        Region region = session.getRegion();
        int affected = editSession.overlayCuboidBlocks(region, block);
        player.print(affected + " block(s) have been overlayed.");
    }

    @Command(
        aliases = {"//walls"},
        usage = "<block>",
        desc = "Build the four sides of the selection",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.region.walls"})
    public static void walls(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        BaseBlock block = we.getBlock(player, args.getString(0));
        int affected = editSession.makeCuboidWalls(session.getRegion(), block);
        
        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = {"//faces", "//outline"},
        usage = "<block>",
        desc = "Build the walls, ceiling, and roof of a selection",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.region.faces"})
    public static void faces(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        BaseBlock block = we.getBlock(player, args.getString(0));
        int affected = editSession.makeCuboidFaces(session.getRegion(), block);
        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = {"//smooth"},
        usage = "[iterations]",
        desc = "Smooth the elevation in the selection",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.region.smooth"})
    public static void smooth(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        int iterations = 1;
        if (args.argsLength() > 0) {
            iterations = args.getInteger(0);
        }

        HeightMap heightMap = new HeightMap(editSession, session.getRegion());
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        int affected = heightMap.applyFilter(filter, iterations);
        player.print("Terrain's height map smoothed. " + affected + " block(s) changed.");
    
    }

    @Command(
        aliases = {"//move"},
        usage = "[count] [direction] [leave-id] ",
        desc = "Move the contents of the selection",
        min = 0,
        max = 3
    )
    @CommandPermissions({"worldedit.region.move"})
    public static void move(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        int count = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
        Vector dir = we.getDirection(player,
                args.argsLength() > 1 ? args.getString(1).toLowerCase() : "me");
        BaseBlock replace;

        // Replacement block argument
        if (args.argsLength() > 2) {
            replace = we.getBlock(player, args.getString(2));
        } else {
            replace = new BaseBlock(0);
        }

        int affected = editSession.moveCuboidRegion(session.getRegion(),
                dir, count, true, replace);
        player.print(affected + " blocks moved.");
    }
    

    @Command(
        aliases = {"//stack"},
        usage = "[count] [direction] ",
        desc = "Repeat the contents of the selection",
        min = 0,
        max = 2
    )
    @CommandPermissions({"worldedit.region.stack"})
    public static void stack(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        int count = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
        Vector dir = we.getDirection(player,
                args.argsLength() > 1 ? args.getString(1).toLowerCase() : "me");

        int affected = editSession.stackCuboidRegion(session.getRegion(),
                dir, count, true);
        player.print(affected + " blocks changed. Undo with //undo");
    }
}
