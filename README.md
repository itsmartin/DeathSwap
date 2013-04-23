DeathSwap
=========

Implementation of SethBling's DeathSwap game as a Bukkit plugin.

Background
==========

* [SethBling's introductory video](http://www.youtube.com/watch?v=r5rEaHPt6mw)
* [Schematic for the vanilla game](http://sethbling.com/swapbox)

Installation
============

* Download the plugin jar file and install it in your server's /plugins folder.

Features
========

* Faithfully replicates the gameplay of the vanilla DeathSwap game by SethBling
* No requirement to have a spectator
* Any players not participating will be invisible to players (and if [SportBukkit](http://github.com/ProjectAres/SportBukkit) is used, they will also not block arrows or affect mob spawning)
* Matches can be of abitrary size (not limited to two players). If there are more than two players, they will cycle.

Usage
=====

A list of commands is available by typing **/help deathswap**.

* All players who want to take part in the game should enter **/join** to join the match. If they decide to leave, they can type **/leave**.
* When players are ready to begin, they type **/ready**.
* As soon as all joined players have marked themselves as ready, the match countdown will begin. There may be a brief period of lag at this point as the server pre-generates the chunks where players will teleport to.
* At the end of the countdown, players will be healed and fed and teleported to their start locations. The world time is set to dawn.
* The match continues exactly as it does in SethBling's vanilla game.

Additional commands
===================

* **/dm reset** - resets the plugin, ending any match in progress and teleporting players back to spawn.
* **/dm uhc** - toggles Ultra Hardcore mode (health regeneration disabled).

Download
========

* Latest beta release: [DeathSwap v0.2](http://www.mediafire.com/?w1i4r1rb0g8701g)

Release notes
=============

The current plugin is still in beta form. It appears fairly stable, but there may be some remaining bugs. Please report any bugs
via the [issue tracker](https://github.com/itsmartin/DeathSwap/issues).
