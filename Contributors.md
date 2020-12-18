# Contributor's Guide

Content here is partially complete.
Please reach out to me directly for more information if necessary so that it can be added here.
This page is intended to be the source of truth for adding anything to walker engine.

There are 4 main concerns for contributing to DaxWalker which are listed below.
These 4 cases should cover 99% of all questions when looking into contributing for DaxWalker but please
feel free to contact me if anything or examples are missing.

To understand the flow of the walker, take a look at `handlePath` in 
[PathWalker](src/scripts/dax/walker/engine/PathWalker.java) and along with the higher level function `walk`.
DaxWalker breaks down each loop by analyzing the path in
[PathAnalyzer](src/scripts/dax/walker/engine/compute/PathAnalyzer.java). It walks to the furthest tile it can and will
resort to custom/default handlers for anything that isn't supported. If there are enough errors, for missing Scenario
Handler, DAxwalker will throw an Exception to make sure we're not stuck for long periods of time.

## Server does not have an Area mapped
This is a server issue. Please submit a ticket on this repo and this will be mapped whenever possible.
This cannot be solved in the client.

## Generic Obstacle Handler
This is the most common handler the walker engine will use when handling obstacles.
The role of this handler is to support anything that is commonly occurring such as basic ladders, staircases, doors, 
etc.
This handler is not intended to be a one-off handler for ANY scenario. Anything that is one-off should be handled via
a [Scenario Handler](#Scenario-handler)

[Source](src/scripts/dax/walker/engine/handlers/GenericObstacleHandler.java)


## Scenario Handler
THis is the one-off handler used for anything that isn't reoccurring. For example, gnome gliders, ships, adding rope to
tunnel of kalphite entrance, etc.

All Scenario Handlers implement [ScenarioHandler](src/scripts/dax/walker/engine/handlers/ScenarioHandler.java) and
are activated when they're added to `AVAILABLE_HANDLERS` in
[ScenarioHandlers](src/scripts/dax/walker/engine/handlers/ScenarioHandlers.java)


## Teleports
These are different from the Handlers above because those are handlers that take action in the MIDDLE of walking a
path.

Teleports happen at the BEGINNING of a path. All teleports are defined in
[Teleport](src/scripts/dax/walker/engine/interaction/Teleport.java).
Please refer to the comments in the file to understand how to define a Teleport.

