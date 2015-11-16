indie-study-june
================

My independent study with Dr. Shenk for my junior and senior years (2014-16) at AAE
Requires google-gson-2.2

## Configuration
The configuration file for the simulation should be created in a document in the directory of the simulation, named `.config`. The file should contain a single map, which should have the same structure to the below example.

        {
          "World Seed": "",
          "World Width": 100,
          "World Height": 100,
          "Update Interval": 20,
          "Entity Types": [
            {
              "Name": "Test Entity",
              "Behaviors": [
                "Exist"
              ],
              "Extras": {
                "number-10": 10,
                "letter-a": "a"
              }
            }
          ],
          "Behaviors": [
            {
              "Name": "Exist",
              "Snippet": "snippets/Exist.snip"
            }
          ]
        }

### World Seed
The seed for the random number generator that the simulation uses, in the form of a string. If the the string is empty, the simulation uses the result of `System.currentTimeMillis()` to determine a seed. If it is a valid Java long, then it will be parsed as such. Otherwise, the hashcode of the string is used instead.

### World Height
The width of the simulated world, in pixels.

### World Height
The height of the simulated world, in pixels.

### Update Interval
The number of milliseconds that should pass in between attempts to update the simulation logic.

### Entity Types
The array containing the types of entities that exist in the simulation.
Each entity type is a map with the following fields.

##### Name
The name of this type of entity. As this name serves as an identifier, it must be unique with respect to other entity names.

##### Behaviors
The array containing the names of the behaviors that this type of entity should exhibit. These should match names provided
in the Behaviors array.

##### Extras
The map of extra data that should be stored in the data map of each new instance of this type of entity. The only requirement for
entries in this map is that keys must be strings. The actual data can be of any type.

### Behaviors
The array containing the definitions for the behaviors present in the simulation. 

Each behavior is a map of the following structure.

#### Name
The name of the behavior. Each name should be unique, as again it is used as an identifier.

#### Snippet
The path to the file containing the Java definition for this behavior.
These snippet files are what allow the flexibility of the simulation. Its contents are compiled prior to running the simulation by the program, and are then incorporated as part of the runtime code. These snippets provide a great deal of customization for the behaviors of entities.

Any Behavior snippet is required to implement `net.popsim.src.sim.Behavior.Snippet`. This interface defines three methods that the snippet must implement:

`void init(World world, Entity entity)`

This method gives a behavior to do any initialization it needs to when an entity is added to the world. This may include storing information in the entity's extras, scheduling behavior updates, and scheduling behavior renders. 

`void behave(World world, Entity entity)`

This method is where a behavior should exert its influence on an entity. It is called during the tick that the entity has scheduled to update this behavior. The entity parameter will contain the entity that is currently "behaving".

An important note is that all entity updates are done in parallel, and therefore some entities in the world may have been updated, and some may not have at the time of calling. Moreover, any entities that have updated may have altered the "current" entity's position. Therefore, any references to an entity's position should be by the `getLastPosition()` method, which returns the position prior to current tick. 

`void render(World world, Entity entity, GraphicsContext gfx)`

Like the update method above, this method is called when the entity has scheduled to render this behavior. Generally this is done every frame, as paint calls will not persist past the current frame. Unlike the update, however, all render calls are done sequentially, as they must be called from the JavaFX Application Thread. 

