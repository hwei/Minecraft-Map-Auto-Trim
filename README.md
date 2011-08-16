ScorePlugin
===========

Score plugin for bukkit.


Description
-----------

* Viewers can give a score to a work. The range of score is 0.0 - 10.0.
* Author will receive reward according to the final score.
* Give score will cost viewer money but he will win reward if the score is near to the final score.
* Final score is average score of viewers generally. But admin can also set it to a forced score.


Requirements
------------
Required:

* Minecraft server that runs CraftBukkit
* An economy plugin supported by Register, for example iConomy

Optional:

* PermissionBukkit (There is an internal permissions system for Score plugin itself.)


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
    /scr addadmin <name> Add an admin. (For servers without PermissionsBukkit)
    /scr removeadmin <name> Remove an admin. (For servers without PermissionsBukkit)


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
 
Configuation
-----------

config.yml example

    # How much money to take from viewer when give a score.
    price: 25.0
    # How much money the viewer will win if he has given the exact score to the final score.
    viewer_max_reward: 200.0
    # How much money the author will win if he has got score of 10.0.
    auther_max_reward: 2000.0
    # If the difference form viewer's score to final score is greater than this, he will win no money.
    viewer_score_threshold: 1.5
    # If the score of auther is less than this, he will win no money.
    auther_score_threshold: 6.0
    # If do not have PermissionsBukkit plugin, set score admins here.
    admins:
    - HWei_just_example_please_remove_this
    - jagt_just_example_please_remove_this

