<div align="center">
    <img src="https://i.imgur.com/MDpQ6nQ.png" style="width: 40%; border-radius:10%" alt="Resource Icon">
    <h1>Sponge Cakes</h1>

[![](https://img.shields.io/badge/Minecraft_Version-1.21-blue)](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21)
[![](https://img.shields.io/discord/1242867116835733534.svg?color=%237289da&label=Discord&logo=discord&logoColor=%237289da)](https://discord.gg/eyRquMQ55v)

</div>

![BStats Graph](https://bstats.org/signatures/bukkit/SpongeCakes.svg)

# What is SpongeCakes?

Sponge cakes is a plugin that implements new sponge related features. Such as a condensed sponge that
sucks up far more water, as well as two new cakes! The sponge cake, and condensed sponge cake. When eaten, they allow
the user to soak up water just by walking near it!

# Features

## Condensed Sponge

A condensed sponge is a stronger version of the normal sponge. It soaks up far more
water than a normal sponge, but breaks immediately after use.

Players by default must have the `spongecakes.craft.condensed` permission to be
able to craft the condensed sponge. You can set the permission to an empty string
to allow all players to craft it.

### Crafting Recipe

A condensed sponge requires 9 sponge to craft. This recipe can be customized in the config.

![Condensed Sponge Crafting Recipe](https://i.imgur.com/43I9EA4.png)

## Sponge Cake

A sponge cake is a unique cake item that when placed and consumed allows the player
to absorb any water nearby. By default, this is in a 5 block radius. The effect can
be removed by drinking a milk bucket.

Players by default must have the `spongecakes.craft.spongecake` permission to be
able to craft the sponge cake. You can set the permission to an empty string
to allow all players to craft it.

### Crafting Recipe

A sponge cake is crafted using the normal cake recipe, but substituting wheat for
sponge. This recipe can be customized in the config.

![Sponge Cake Crafting Recipe](https://i.imgur.com/fbuL1Wp.png)

## Condensed Sponge Cake

A condensed sponge cake is similar to a sponge cake, however it has a longer effect
time by default and a larger radius. The effect can still be removed by drinking
a milk bucket.

Players by default must have the `spongecakes.craft.condensedspongecake` permission to be
able to craft the condensed sponge cake. You can set the permission to an empty string
to allow all players to craft it.

### Crafting Recipe

A sponge cake is crafted using the normal cake recipe, but substituting wheat for
condensed sponge. This recipe can be customized in the config.

![Condensed Sponge Cake Crafting Recipe](https://i.imgur.com/aQZjnQS.png)

# Commands

| Command      | Description                           | Permission         |
|--------------|---------------------------------------|--------------------|
| /spongecakes | Reload the configuration and recipes. | spongecakes.reload |

#

<details>
<summary style="font-size: 30px; font-weight: bold;">Default Configuration</summary>

```yaml
Messages:
  Craft-No-Permission: "&cYou cannot craft this item!"
  Condensed-Error-Placing: "&cYou must place a condensed sponge in water to use it!"
  # This is the display that is shown when a player
  # has eaten a slice of sponge cake
  Time-Display:
    Time-Left: "&e%time% &aleft of sponge absorb time!"
    Condensed-Time-Left: "&e%time% &aleft of condensed sponge absorb time!"
  Command:
    No-Permission: "&cYou cannot use this command"
    Reloaded: "&aSuccessfully reloaded configuration!"

# When a condensed sponge is used it immediately breaks
Condensed-Sponge:
  # The permission a player must have to craft this
  # If the permission is empty, anyone can craft it
  Permission: "spongecakes.craft.condensed"
  # The maximum amount of blocks the condensed sponge can absorb
  Max-Absorbed: 576
  # The condensed sponge item
  Item:
    Material: "SPONGE"
    Glow: true
    Name: "&eCondensed Sponge"
    Lore:
      - "&7A condensed version of a &esponge&7."
      - "&7When placed it absorbs up to &e%max% &7blocks."
  Recipe:
    # Whether the recipe should be enabled
    Enabled: true
    # Recipe type, valid options are SHAPED or SHAPELESS
    Type: "SHAPED"
    # Represents a crafting table
    #
    # This is the shape of the recipe
    # only required if it's a SHAPED recipe.
    # MUST have the empty spaces, 3 strings in a 3x3x3 size!
    # The shape key can only be a single character, such as S!
    Shape:
      - "SSS"
      - "SSS"
      - "SSS"
    # Ingredients for the recipe.
    # If recipe is SHAPED the key such as "S"
    # must match the character above in the recipe.
    Ingredients:
      S:
        Material: "SPONGE"

Sponge-Cake:
  Permission: "spongecakes.craft.spongecake"
  # How long the player should absorb water after eating a slice
  # Time stacks when eating a slice, so eating 2 would be 20 seconds
  Absorb-Time-Per-Slice: "10s"
  # The radius of blocks to absorb, if water or waterlogged
  Absorb-Radius: 5
  Item:
    Material: "CAKE"
    Name: "&eSponge Cake"
    Lore:
      - "&eA Sponge Cake!"
      - ""
      - "&7When a slice is eaten, you gain the ability to"
      - "&7absorb nearby water in a &e%base-radius% &7block radius!"
  Recipe:
    Enabled: true
    Type: "SHAPED"
    Shape:
      - "MMM"
      - "SES"
      - "AAA"
    Ingredients:
      M:
        Material: "MILK_BUCKET"
      S:
        Material: "SUGAR"
      E:
        Material: "EGG"
      A:
        Material: "SPONGE"

Condensed-Sponge-Cake:
  Permission: "spongecakes.craft.condensedspongecake"
  Absorb-Time-Per-Slice: "30s"
  Absorb-Radius: 10
  Item:
    Material: "CAKE"
    Glow: true
    Name: "&eCondensed Sponge Cake"
    Lore:
      - "&eA Condensed Sponge Cake!"
      - ""
      - "&7When a slice is eaten, you gain the ability to"
      - "&7absorb nearby water in a &e%condensed-radius% &7block radius!"
  Recipe:
    Enabled: true
    Type: "SHAPED"
    Shape:
      - "MMM"
      - "SES"
      - "AAA"
    Ingredients:
      M:
        Material: "MILK_BUCKET"
      S:
        Material: "SUGAR"
      E:
        Material: "EGG"
      A:
        Material: "CONDENSED_SPONGE"
```

</details>

## Questions? Suggestions? Issues? Join my [Discord](https://discord.gg/eyRquMQ55v)!
