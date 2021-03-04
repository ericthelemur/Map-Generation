# Map-Generation
My 2nd attempt at hex-based map generation, with named cities and nations, and rivers and roads

![Main](https://user-images.githubusercontent.com/8903016/109990302-2c80a600-7d01-11eb-9ef5-074a3a5c928b.png)

In this project, I wanted to focus on and further the map generation in my [previous project](https://github.com/ericthelemur/Hex-Wars), by including rivers, territory and names. 

## Generation
The generation can be split into 2 primary sections: terrain and civilisation. The terrain elements include the heightmap, rainfall, biomes, rivers, etc. and the civilisation elements include capital, city and road placement, and territory selection.

1. Terrain
    1. Elevation Map
    2. Rainfall Map
    3. Rivers
    4. Biomes
2. Civilisation
    1. City Placement
    2. Nation Territory
    3. Road Routes
3. Graphical
    1. Place Trees
    2. Render

### Terrain
The elevation generation uses a layer of ridged Perlin Noise domain warped by 2 separate standard Perlin Noise functions. I then multiply the value by 1-the distance to the centre of the map. This creates a fall-off towards the edge, more like that of an island and keeps the island edges natural.  

![elevation](https://user-images.githubusercontent.com/8903016/109990380-3e624900-7d01-11eb-964e-ba57afa4e4a8.png)


The rainfall generation uses a billowed layer of noise, with a slight temperature gradient applied over it. These 2 values are then used to calculate the biome of each hexagon, by approximating a Whittaker diagram, which is a representation of Earth’s biomes distributed by precipitation amount and temperature. In this case, I am using elevation as an approximation of temperature.
For rivers, the drainage basins are first constructed, these are constructed using Priority-Flood Depression Filling, which is intended to fill holes in a heightmap, but can also calculate the flow direction of each hex. Then, the route to the edge of the map is traced from each hex, increasing the water flux (river size) of each hex on the route. Displayed rivers are any hex with a certain threshold of water flux.

![water flow](https://user-images.githubusercontent.com/8903016/109990398-428e6680-7d01-11eb-97d1-c73169fff74e.png)

### Civilisation
Each hex is given a weighting to be picked for a city location and cities are placed on hexes with the highest weightings, unless the chosen location is too close to an already existing city. These weightings are influenced by water flow and amount of surrounding land. This biases towards cities at the mouths of rivers, which is where many historical successful cities lay. The first few cities placed become the capitals of their own nation. Once all cities are placed, the nations’ territory is created with a Dijkstra-like flood fill from each capital, weighted to make borders much more likely along mountain ridges, rivers and oceans. 

The city names are generated with a standard Markov Chain approach, with a 50/50 chance of being based of a set of Welsh place names or German place names. I picked these locations as the information was widely available, and gave a nice “other-world-ly-ness” feel. It also meant the Markov Chain could get away with scrambling English place names into nonsense, since few English speaking people are familiar enough with either language to notice mistake.

![fill edges](https://user-images.githubusercontent.com/8903016/109990412-4621ed80-7d01-11eb-9dd8-73bbac5e3460.png)

Roads are created between all capitals, between towns and their capital, and between nearby towns. This creates a convincing web of roads, while not having to process a pathfinding attempt for every pair of >40 towns. Roads between larger cities are searched first, and existing roads have decreased cost for future road routing. Futhermore, if the pathfinding reaches an existing road to the destination, the pathfinder finishes the route along this road. This ensures routes between major cities are mostly directly and completely connected, whereas smaller cities are only connect to the nearby road or 2, since they would have less need of a large direct road.

## Debug Options
Within this program, further information about each map can be found in the debug menu, which can display multiple views of different sets of data. These include the chance of a city being placed at a given location, the flow of water per location, and the elevation and rainfall maps.
![debug](https://user-images.githubusercontent.com/8903016/109990462-520daf80-7d01-11eb-836e-01892ffc60b8.png)


## Graphics
As well as wanting to create a more accurate map, I also wanted to improve it’s rendering. To this end, roads and rivers are drawn as Bezier curves through each hex on it’s path, and the size of both scale with the size of the cities/towns it connects. In forested areas, I also generated areas of trees to represent this. I would have liked to have done a similar thing (but obviously much larger) with mountains, so there are actual peaks.

![closeup](https://user-images.githubusercontent.com/8903016/109990477-55a13680-7d01-11eb-85b0-0f2bca85a13d.png)


