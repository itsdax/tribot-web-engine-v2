# OSRS Web Walker

## Summary

This client is written for [Tribot](https://tribot.org/) and used to navigate throughout OSRS.
This is V2 of Tribot's webwalker engine. This is currently in **BETA**.
All paths are generated remotely on a server. 
This repository contains the client side logic for consuming the paths returned by the server.
There should (rarely) be missing mapped areas. Please submit a ticket and this will be mapped whenever possible.

For users coming from V1, this version has improved obstacle and pathing detection locally.
The limitation is that this version does not have all the custom handlers of V1, which has been polished and patched
over 5 years. I encourage users to feel free and submit code to support any custom area handlers if they happen to
implement them. Please check out [Adding Area and Custom Handlers](#Adding-Area-and-Custom-Handlers)

## Features
- **Speed**: Extremely fast computation with a worst case of 200ms (not including latency to backends)
- **Server Sided**: CPU + memory heavy computations are all done remotely.
- **Short-cuts**: Uses shortcuts/obstacles whenever possible while accounting for skill/quest/item requirements
- **Danger Path Weighting**: Avoids dangerous areas when low combat level (i.e: dark wizard circle south of Varrock)
- **Teleports**: Uses teleports available for your character

Visit [Explv](https://explv.github.io/) to try out Dax Path generation and see if it supports the area you need.
<p align="center">
  <img src="https://i.imgur.com/Haf7BNb.gif"/>
</p>

## Usage

```java
package scripts;

import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;
import scripts.dax.walker.DaxWalker;
import scripts.dax.walker.WebWalker;
import scripts.dax.walker.data.RSBank;
import scripts.dax.walker.engine.Navigator;
import scripts.dax.walker.server.DaxWalkerServerClient;

import java.awt.*;

@ScriptManifest(name = "Walker Test", authors = {"dax"}, category = "Tools")
public class WalkerTest extends Script implements Painting {

    // The reason why we're holding this in an instance variable is so we can enable debug paint
    private DaxWalker walker;

    @Override
    public void run() {
        // Instantiate a server client with your API keys
        DaxWalkerServerClient daxWalkerServerClient = new DaxWalkerServerClient("PUBLIC-KEY", "SECRET-KEY");

        // Create instance of DaxWalker
        walker = new DaxWalker(daxWalkerServerClient, new Navigator());

        // IMPORTANT: In order to use the convenience class 'WebWalker', you MUST initialize the singleton
        WebWalker.setDaxWalker(walker);

        // Walk to a location
        WebWalker.walkTo(new RSTile(1, 2, 3));
        
        // Walks to closest bank
        WebWalker.walkToBank();
        
        // Walks to Varrock east bank
        WebWalker.walkToBank(RSBank.VARROCK_EAST);
    }

    @Override
    public void onPaint(Graphics graphics) {
        // Allows for DEBUG paint. This is highly optimized, but will still consume a bit of CPU.
        if (walker != null) walker.onPaint(graphics);
    }
}

```

## API Keys
Please visit https://admin.dax.cloud/ for more information. To use your Api Keys, please configure your Dax API Key provider.

[![Api Keys](https://i.imgur.com/Qwc0115.png)](https://admin.dax.cloud)

## Adding Areas and Custom Handlers

Please refer to [Contributor's Guide](./Contributors.md)

