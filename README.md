Midas Silver - Block ID and Tile Entity Changer
===============================================
updated by agaricus, based on [mIDas GOLD](https://code.google.com/p/midas-gold/) by Laurence Reading, originally developed as [mIDas](https://code.google.com/p/midas/) by Pfaeff


IMPORTANT NOTE:
---------------

I am not responsible for any damage or data loss that may occur using this tool.

Downloads
---------

Latest builds are available from Buildhive: 
[![Build Status](https://buildhive.cloudbees.com/job/agaricusb/job/midas-silver/badge/icon)](https://buildhive.cloudbees.com/job/agaricusb/job/midas-silver/)

What is mIDas?
--------------

Midas Silver is a very simple tool for Minecraft that allows you to change the IDs of certain blocks 
and items in your world files. You can for example replace all blocks of dirt with blocks of cobblestone 
if you want to. This tool is especially useful for modders and mod users, because it allows to 
exchange IDs during the development process. It also changes the IDs inside of chests and in your inventory,
and has several other optional conversion features.


Which Minecraft versions are supported?
---------------------------------------

This tool currently works with all Minecraft versions that use the Anvil world format (1.2
and higher). It has been tested with version 1.5.1. It should work with future versions, if the level format isn't changed drastically.
For older world formats, see mIDas Gold or the original mIDas.


Compilation and Installation
-----------------------------

To build Midas Silver:

1. Install [Apache Maven](http://maven.apache.org/)
2. Run `mvn package`

The standalone jar will be generated in the target "target".

To use, run the console command `java -jar target/midas-silver-1.0-SNAPSHOT.jar`.


Usage
-----
Run the jar with no arguments for help.

The --input-save-game and --patch-file arguments are required.

You need to select a savegame first (you can add external savgames using the "add savegame" button).

Depending on the world size, this may take some time.
Note that this process can not be undone. If you exchange ID 15 with 0 for example and then change 
0 back to 15, all blocks that were 0 before now get changed, too. 
There is an option for backing up your region folder, but it is recommended to do a manual backup.

Patch File Format
-----------------
The file given to --patch-file should be a text file, with each line of the format:

oldid -> newid

where either id can be with or without metadata (blockid:metadata or just blockid).
Block and item IDs are supported.

Comments begin with "#" and are ignored.

Conversion Plugins
------------------
Several "plugins" for handling ID conversion are integrated, and can be enabled/disabled as desired.

ConvertBlocks: converts blocks in the world. Can be disabled with --no-convert-blocks

ConvertItems: converts items in "Items" NBT. Can be disabled with --no-convert-items.

ConvertPlayerInventories: converts items in player Inventory NBT. Can be disabled with --no-convert-player-inventories.

BuildCraftPipesPlugin: converts pipeID NBT with block/item ID conversion. Can be disabled with --no-convert-buildcraft-pipes.

_New in Midas Silver_:

ProjectBenchPlugin: converts [RedPower2](http://www.minecraftforum.net/topic/365357-146-eloraams-mods-redpower-2-prerelease-6/) Project Table tile entities to bau5 [Project Bench](http://www.minecraftforum.net/topic/1550010-151sspsmp-project-bench-v173forge5000-downloads/) tile entities. You can convert the block ID as well in the patch file.


Sourcecode
----------

The sourcecode can be found on GitHub: https://github.com/agaricusb/midas-silver

Original source on Google Code: http://code.google.com/p/midas/ and https://code.google.com/p/midas-gold/


Additional notes
----------------

- I only tested this tool under OS X , so there might be problems on other systems. 
  Though it might work with some restrictions.
- There is no GUI in this version, only a CLI (use mIDas Gold if you want a GUI).


Bugs/Suggestions
----------------

Found a bug or have a suggestion to make?
You can use the bug tracker on GitHub: https://github.com/agaricusb/midas-silver/issues
Feel free to fork, branch, etc. Midas Silver was developed as a fork of mIDas Gold for my
own needs; it is open source (GPLv3) so can be changed by anyone for any purpose, enjoy :)
