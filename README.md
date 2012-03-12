Minecraft Map Auto Trim
=======================
Help you delete unnecessary trunks.

Usage
-----

    java -jar mmat.jar -w <world path> [-d <dilation size>] [-p <id list>] [-r <rect minX,maxX,minZ,maxZ>]

Where:

    <world path>    Path to the world folders
    <dilation size>    Dilate preserved area to perserve more chunks around the chunks with "perserve block"
    <id list>    Define a list of "perserve block".If a chunk contains any "perserve block", it will be preserved.
    <rect>    If specified, all the chunks outside this rectangle will be forced to delete.

Examples:

    java -jar mmat.jar -w ~/minecraft/world
    java -jar mmat.jar -w ~/minecraft/world -d 3 -p 63,68 -r -1000,1000,-1000,1000

Forum Thread
------------
http://forums.bukkit.org/threads/tool-admin-minecraft-map-auto-trim-v0-2.37846/
