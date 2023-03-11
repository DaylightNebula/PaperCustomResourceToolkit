The goal of this toolkit is to make a simple solution for custom blocks, entities, items and guis.

Goals:
 - Toggleable automatic resource pack generation on startup
 - Toggleable and configurable local server host for the resource pack (should only be used for testing, this should be disabled for public facing servers)
 - Custom blocks, entities, items and guis that can be easily and quickly made via .json files
 - BBModels can be used for custom models for all custom objects
 - PNG textures can be used for custom items textures

Todolist:
- [ ] Local Server
  - [ ] Only open on resource pack finalized event
  - [ ] Host via https
  - [ ] Settings to kick if pack is not accepted
  - [ ] Resend resource pack packet if loaded is sent to quickly after accepted
- [ ] Resource pack generator
  - [ ] Universal way of storing and saving the models before they are packed
    - [ ] Other plugins can easily add folders to load assets from
  - [ ] Pack creation should start from onLoad but not finalized until the first tick
    - [ ] Should be run async
    - [ ] Local server should not be started until this finishes
    - [ ] Broadcast resource pack finalized event
  - [ ] PNG textures converted to models
  - [ ] Allow for 
- [ ] Custom items (maybe able to steal from rpg toolkit)
  - [ ] Load from json files
  - [ ] Optionally use custom texture or model
  - [ ] Extension functions for item stack
    - [ ] Is Custom Item
    - [ ] Get Custom Item (return null if not custom item)
  - [ ] Settings
    - [ ] All the settings (list in rpg toolkit)
    - [ ] Custom Durability (0 means no durability and is default)
    - [ ] Max stack count
  - [ ] Events
    - [ ] Custom Item Interact
    - [ ] Custom Item Attack
    - [ ] Custom Item Break
    - [ ] Custom Item Pickup (include give command)
- [ ] Custom blocks
  - [ ] Load from json files
  - [ ] Custom item created automatically to be the item representation of this
    - [ ] Only allow for max stack count to be edited via json
  - [ ] Implemented in code is optional and vice versa
  - [ ] Start, stop, update, you know the drill
  - [ ] Optionally use custom models
  - [ ] Settings
    - [ ] Can be mined
    - [ ] Hardness
    - [ ] Name
    - [ ] Drops
- [ ] Custom entities
  - [ ] Allow json files to specify when certain animations should be used
  - [ ] json files specific tasks
  - [ ] Could just steal old system from rpg toolkit and modify it to use animations
  - [ ] Task System
    - [ ] Tasks sorted by priority that can be changed
    - [ ] A task change can ONLY occur when a task marks itself as done or is marked "can be stopped prematurely"
    - [ ] Start, stop, and update, you know the drill at this point
    - [ ] Animations can be specified for the task to use or to be played while the task is running
- [ ] Custom GUIs
  - [ ] Allow to be easily created via json files
  - [ ] Easy way to hide slots
  - [ ] Elements (textures for which may be optionally specified)
    - [ ] Input slot (fire event, a user can play an item here)
    - [ ] Output slot (fire event, a user can only take items for here)
    - [ ] Button (fire event, optionally toggle to other texture)
    - [ ] Checkbox (cycle between textures for other options, fire event)
    - [ ] Labels (can be used to display text and have a custom background)
    - [ ] Text buttons (could just add to button, check when this is being implemented)