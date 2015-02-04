jCubicWorld
=============

This project was started by me as an educational training-project with libgdx.
I was porting my unity3d voxel engine to libgdx, but it was that much fun that i started to write kind of a game.
I dunno if i ever finish this, may i will. may i'll quit at some point :P

This project is currently under heavy development and not ready for playing!

What is jCubicWorld?
==============

jCubicWorld started as a voxel rendering fun-project.
I was porting my Unity VoxelEngine to Java and i wanted to try out libgdx.
Working with libgdx was that much fun so i decided to make a whole game out of it.

My goal is to build a game similar to minecraft but specialized on user-created content.
I want to provide a modding api for unlimited access to all of the game mechanics.

Eclipse Plugins
===============

Cubicworld uses the following Eclipse Plugins (+Repos):

Needed
---------
gradle - http://dist.springsource.com/snapshot/TOOLS/gradle/nightly

Eclipse 4.4 Recommended!

Recommended
---------
PMD - http://sourceforge.net/projects/pmd/files/pmd-eclipse/update-site/
FindBugs - http://findbugs.cs.umd.edu/eclipse/
Mylyn - http://download.eclipse.org/mylyn/releases/luna

Eclipse 4.x Recommended!

Conventions
==============

There are a few conventions for adding code to this project:

General
----------------
Whenever you use a Vector3 obtained by a getter method wrap it into a new vector like this new Vector3(obj.getPos());
Remeber, in java there's call by reference so if you get a vector by a getter you will get the reference of the Vector used in the object, not a new instance!

Always create a new vector if you want to modify it and use it in another space!
You can use a position returned by getPosition() to set new values to the vector3 instance, but you shouldn't.
Use the setPosition() method instead!

Please use firstLowerCamelCase for your variable names.
For class, interface, abstract classes or enumerations, please use FirstUpperCamelCase.

Abstract classes have a prefix "A". Interface have "I".
Example: AEntity (Abstract class) and IPacketModel (Interface)

Generally classes in this project are not thread-safe, only if the documentation explicitly states it.

Entities
------------------
Entity positions ALWAYS point to the bottom center position of it's collision box.
If you access entity position or rotation use the getter and setter method!
Otherwise it is not guaranteed that the values will be updated correctly.

Also, if any value of an entity depends on position or rotation, just overwrite AEntity's setter methods and take care of the dependence.

Formatting
------------------
You can find my eclipse code style configuration for auto-formatting in this repo under eclipse_settings/formatter_settings.xml!

Libraries
==============
jCubicWorld uses the following libraries:

- libGDX - http://libgdx.badlogicgames.com/ - Apache 2.0 license
- jUnit - http://junit.org/ - EPL 1.0
- EasyMock - http://easymock.org/ - MIT License
- Hamcrest-Junit - https://github.com/hamcrest/hamcrest-junit - EPL 1.0
- Sqlite-JDBC - https://bitbucket.org/xerial/sqlite-jdbc - Apache 2.0 license
- MapDB - http://www.mapdb.org/ - Apache 2.0 license
- dain's java snappy implementation - https://github.com/dain/snappy - Apache 2.0 license
Current Features
==============

- Multiuser (Entity system)
- Voxel world rendering
- World editing (+ Multiplayer sync)
- Basic physics system (Supports collision boxes and raycasting)
- 3d Models as voxels (World block model rendering)
- Input system
- Pathfinder (A*)
- GUI System
- Inventory/Item System (WIP)
- Plugin API (WIP)
- Enemy AI System (WIP)

Planned Features
==============

- Better physics implementation
- Fluid simulation (Maybe like the one of minecraft?)
- Minecraft like world generation (Caves and so on)
- Weapons (Swords, Bows, Guns)
- Tools (Pickaxe, Shovel, ...)
- Trading System with either direct trading (Player to Player) or shops.


Screenshots
------------------

![Furnaces, the middle one is working](http://kennux.net/wordpress/wp-content/uploads/2015/02/jcubicworld_furnaces-1024x789.png)
![Player inventory](http://kennux.net/wordpress/wp-content/uploads/2015/02/jcubicworld_inventory-1024x792.png)
![Test entity implementation with A* Pathfinding](http://kennux.net/wordpress/wp-content/uploads/2014/11/screenshot1416230295.png)