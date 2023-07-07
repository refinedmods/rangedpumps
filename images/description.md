### Ranged Pumps is a simple mod that adds a pump that pumps liquids in a range.

![A ranged pump being used to pump lava in the Nether](overview.png)

![Crafting recipe for a ranged pump](crafting_recipe.png)

## Placement

Place the pump down, and it'll only work *without* a redstone signal.

## Redstone signal

If you give the pump a redstone signal, it will stop working until you disable the redstone signal.

## Energy requirements

By default, the pump requires Forge Energy, but can be configured to not use energy in the config.

## Internal tank

The size of the pump's internal tank is 32 buckets by default. You can increase or decrease this in the config.

The pump will stop running if the internal tank is full.

## Status updates

Right click on the pump to know what it is doing and to know if it is running.

## Scanning and range

![Scanning behavior of a ranged pump](/assets/ranged-pumps/scanning.png)

It'll start pumping liquids in rectangular spiral shape 1 block under the pump.

The default range is 64 blocks. That'll mean it scan 64 blocks in any direction, nearest-first.

On a vertical (Y) level, it will scan liquids from the pump to bedrock.

## Liquids to stone

The pump will also replace any liquids to stone by default. This can be turned off in the config.

## Auto-pushing liquids

The pump will auto-push liquids to neighboring tanks. You can also get the liquids out with pipes.

## Chunkloading

The mod doesn't chunkload pumps automatically. You'll need another mod to get this behavior.
