-------------------------------
mIDas - Block ID Changer V0.3.2
-------------------------------
by Pfaeff


IMPORTANT NOTE:
===============

I am not responsible for any damage or data loss that may occur using this tool.


What is mIDas?
==============

mIDas is a very simple tool for Minecraft that allows you to change the IDs of certain blocks 
and items in your world files. You can for example replace all blocks of dirt with blocks of cobblestone 
if you want to. This tool is especially useful for modders and mod users, because it allows to 
exchange IDs during the development process. It also changes the IDs inside of chests and so on, 
but not those inside your inventory or those lying around. But this is already planned for future 
versions.


Which minecraft versions are supported?
=======================================

This tool currently works with all minecraft versions that use the MCRegion world format 
(i.e. version Beta 1.3 and higher). It has been tested with version Beta 1.4_01. It should
work with future versions, if the level format isn't changed drastically.


Installation:
=============

Doubleclick mIDas.jar to start.
If that doesn't work, use the console command "java -jar mIDas.jar".


Usage:
======

You need to select a savegame first (you can add external savgames using the "add savegame" button).
You can then add the IDs that you want to exchange to the list. Choose a target ID and click start.
Depending on the world size, this may take some time.
Note that this process can not be undone. If you exchange ID 15 with 0 for example and then change 
0 back to 15, all blocks that were 0 before now get changed, too. 
There is an option for backing up your region folder, but it is recommended to do a manual backup.


Sourcecode:
===========

The sourcecode can be found on google code: http://code.google.com/p/midas/


Additional notes:
=================

- I only tested this tool under Windows, so there might be problems on other systems. 
  Though it might work with some restrictions.
- I know the gui (and its code) is a mess. It will either be rewritten or cleaned up.  


Bugs/Suggestions
================

Found a bug or have a suggestion to make? Either mail it to pfaeff@googlemail.com 
or use the bug tracker on google code: http://code.google.com/p/midas/
