# Windchimes

Wind chimes for Minecraft 26.3 (Fabric). Hang one under a roof, a tree, or a fence, and it plays soft random notes in the breeze.

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.3.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.3.
3. Download `windchimes-x.y.z.jar` from the [Releases](https://github.com/Studleee/windchimes/releases) page.
4. Put both jars in your `.minecraft/mods` folder and launch the Fabric profile.

The mod is needed on both the client and the server.

## Features

- **Five voices:** bamboo (wooden tok), iron (bright metal), copper (bell), gold (chime), and amethyst (crystal).
- **Long chimes:** each one also comes in a long version that hangs two blocks tall, with rods twice as long, slower swings, and deeper notes (plus a quiet lower undertone for body). It needs an empty block below it.
- **They move:** the rods hang from strings and sway. They lean downwind, rock in the gusts (long rods swing slower than short ones), and jump when struck. A muffled chime hangs still.
- **Wind:** the whole world shares one breeze that slowly changes direction. Gusts roll across the land, so a row of chimes rings one after another. In strong wind, faint white wisps blow past the chimes.
- **Weather matters:** chimes are gentle on calm days, busier in rain, and wild in thunderstorms. They also swing and ring more high up (Y 100 and up).
- **Rings like a real chime:** each of the four rods has its own note (the longest rod is the lowest), and the wind knocks the striker into the rods at uneven moments, often clattering into two or three in a row so the notes ring over each other. Calm air brings a gentle tink now and then; gusts bring quick overlapping runs.
- **Always in tune:** every note comes from a pentatonic scale, and each chime picks its own four notes, so several chimes together still sound nice.
- **Right-click** to ring one yourself.
- **Sneak + right-click** to muffle it or unmuffle it.
- A chime must hang from something above it. It falls off if that block is removed or if a piston pushes it.

**Crafting**, for every chime:

```
 W       W = any wooden slab
SSS      S = string
MMM      M = bamboo / iron nugget / copper nugget / gold nugget / amethyst shard
```

**Long chimes:** the normal chime with three more of its material underneath.

```
 C       C = the normal chime (e.g. Iron Wind Chime)
MMM      M = bamboo / iron nugget / copper nugget / gold nugget / amethyst shard
```

Code for the chimes: `block/WindChimeBlock.java` (behavior), `block/ChimeVoice.java` (sounds and notes), `Wind.java` (the breeze), and `client/WindChimeRenderer.java` (the swinging rods).

## Quick start

Double-click `run.bat`, or run it from a terminal in this folder. Minecraft opens with your mod loaded, and everything shows up in the **Windchimes** creative tab. The first launch downloads Minecraft and takes a few minutes.

## Adding content

Run these from a terminal in this folder (in Cursor: **Terminal > New Terminal**).

| You want | Command |
| --- | --- |
| A plain item | `new-item ruby "Ruby"` |
| A food | `new-item apple_pie "Apple Pie" -Food` |
| A block | `new-block ruby_block "Block of Ruby"` |
| A wooden block | `new-block maple_planks "Maple Planks" -Tool axe` |

Each command:

1. adds a line to `ModItems.java` or `ModBlocks.java`
2. creates the model files Minecraft needs
3. creates a colored placeholder texture
4. adds the in-game name

Then paint over the placeholder texture and run `run.bat` again.

Names must be lowercase, like `ruby_sword`. The quoted part is what players see.

## Naming your mod

When you pick a name, run:

```
rename-mod "Dragon Tools"
```

That renames the mod, its id (`dragontools`), the Java package, and every folder in one go. Run it early, before you share your mod.

## Where things live

```
src/main/java/com/windchimes/
  Windchimes.java            starts the mod
  Wind.java                  the shared breeze: direction, strength, gusts
  block/WindChimeBlock.java  how chimes hang, ring, and muffle
  block/WindChimeBlockEntity.java  remembers the last strike so the rods can jump
  block/ChimeVoice.java      the voices: sound, volume, notes, how easily they swing
  registry/Register.java     helper shortcuts (item, food, block)
  registry/ModItems.java     list of your items
  registry/ModBlocks.java    list of your blocks (all the chimes)
  registry/ModBlockEntities.java, ModParticles.java
  registry/ModCreativeTab.java  your creative tab (fills itself)

src/client/java/com/windchimes/client/
  WindChimeRenderer.java     draws the swinging rods and strings
  WindParticle.java          the wind wisps

src/main/resources/
  assets/windchimes/models/block/wind_chime.json  the full chime shape (used for the item)
  assets/windchimes/models/block/wind_chime_cap.json  just the top (the placed block; the rods are drawn by the renderer)
  assets/windchimes/models/block/*_wind_chime.json  which textures each chime uses
  assets/windchimes/textures/particle/wind_*.png  wisp frames
  assets/windchimes/lang/en_us.json  in-game names
  data/windchimes/recipe/    crafting recipes
  data/windchimes/loot_table/  what blocks drop
```

## Tweaking content

Everything is in `ModItems.java` and `ModBlocks.java`.

**Items**, using `Register.item("name", new Item.Properties()...)`:

- `.stacksTo(16)`: stack limit
- `.rarity(Rarity.EPIC)`: colored name
- `.fireResistant()`: survives lava, like netherite
- `.durability(250)`: wears out with use

**Food**, using `Register.food("name", nutrition, saturation)`. A carrot is `3, 0.6F` and steak is `8, 0.8F`.

**Blocks**, using `BlockBehaviour.Properties.of()...`:

- `.strength(1.5F)`: mining time (dirt 0.5, stone 1.5, obsidian 50)
- `.lightLevel(state -> 15)`: glows (0 to 15)
- `.sound(SoundType.GLASS)`: break and step sounds
- `.noOcclusion()`: for see-through blocks like glass
- `.friction(0.98F)`: slippery like ice

## Custom behavior

To make a block do something, write a class that extends `Block` (like `WindChimeBlock`) and register it with a factory:

```java
public static final Block MY_BLOCK = Register.block("my_block", MyBlock::new, BlockBehaviour.Properties.of());
```

**Adding a chime voice:** add a line to `ChimeVoice.java`, then add a chime to `ModBlocks.java`:

```java
public static final Block GLASS_WIND_CHIME = windChime("glass", ChimeVoice.GLASS, SoundType.GLASS, "glass", "oak_planks");
```

The last two are the vanilla textures for the rods and the top. Then add the same chime to the list at the top of `tools/gen-chime-assets.ps1` and run it. It writes the models, blockstate, item, loot table, recipe, and in-game name.

## Recipes

Copy one of the files in `data/windchimes/recipe/` and change the items. Use `minecraft:` for vanilla items and `windchimes:` for yours.

## Textures

Textures are 16x16 PNGs. Good free tools:

- [Pixilart](https://www.pixilart.com/draw), in the browser
- [Paint.NET](https://www.getpaint.net/)
- [Blockbench](https://www.blockbench.net/), for textures and custom 3D models

## Sharing your mod

Run `build.bat`. Your mod is `build/libs/windchimes-1.0.0.jar`. Players need Fabric Loader and Fabric API for Minecraft 26.3, plus your jar in their `mods` folder.

## Learn more

- [Fabric documentation](https://docs.fabricmc.net/develop/): official guides for items, blocks, entities, and more
- [Fabric Discord](https://discord.gg/v6v4pMv): ask questions
