ScorePlugin
===========

Description
-----------

Score plugin for bukkit.

* Let viewers give a score of your work. You will receive reward according to the final score.
* Give score will cost viewer money but he will win reward if the score is near to the final score.
* Final score is average score of viewers generally. But admin can also set it to a forced score.


Requirements
------------
Required:

* Minecraft server that runs CraftBukkit
* An economy plugin supported by Register, for example iConomy

Optional:

* PermissionBukkit


Usage
-----

1. Create a sign near your work: 1st line is `[Score]`, 2ed line is the name of your work.
2. Punch this sign, your name will display in 4th line.
3. Ask admin to use `\scr open` to open this score sign for you.
4. Inform other players of your work, and let them to give it a score.
5. Ask admin to use `\scr close` to close this score sign to get a final score and distribute rewards.




Commands
--------

### For users

    /scr info View score info.
    /scr <score> Give a score.

### For Admins

    /scr open Open a score sign.
    /scr admin View info for admins.
    /scr set <score> Set a forced score.
    /scr unset Unset forced score.
    /scr clear Clear all scores from viewers.
    /scr maxreward <amount> Set max reward for author.
    /scr close Close a score sign and distribute rewards.
    /scr addadmin <name> Add an admin.
    /scr removeadmin <name> Remove an admin.


Permissions
-----------

    permissions:
      score.*:
        description: Gives access to all Score commands
        children:
          score.admin: true
      score.admin:
        description: Allows you to manage Score.
        default: op
        children:
          score.use: true
      score.use:
        description: Allows you to view info and give score.
        default: true
 