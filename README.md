Minecraft Map Auto Trim
=======================

Delete unused chunks automatically.


Features
--------

* Detect chunks which contain no building and delete them.
* Can dilate remain region for a certain number of chunks.


Usage
-----

    java -jar mmat.jar -w <world path> [-d <dilation size>] [-p <user defined preserve block id list (spliter: comma)>]


Default Protect Block ID list
-----------------------------

    5,19,20,22,23,25,26,27,28,29,33,34,35,36,41,42,43,44,45,46,47,53,55,57,59,60,63,64,65,66,67,68,69,70,71,72,75,76,77,80,84,85,87,88,89,91,92,93,94,96
