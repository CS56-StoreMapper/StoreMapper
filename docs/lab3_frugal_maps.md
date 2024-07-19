# Lab 3: Frugal Maps

```
The questions below are due on Friday February 28, 2020; 040000 PM.
```
```
You are not logged in.
```
```
If you are a current student, please Log In for full access to the web site.
Note that this link will take you to an external site (https://shimmer.csail.mit.edu) to authenticate, and
then you will be redirected back to this page.
```
## Table of Contents

```
1) Preparation
2) Introduction
2.1) The Data
2.2) Testing
3) Shortest Paths
3.1) Design Considerations
3.1.1) Road Types
3.1.2) Connectedness
3.1.3) Distance Measure
3.1.4) Starting and Ending Points
3.2) Auxiliary Data Structures
3.3) Visualization
4) Improving Runtime With Heuristics
4.1) Heuristics and Optimality
4.2) A Heuristic For This Problem
5) Need for Speed (Limits)
5.1) Heuristics
5.2) Visualization
6) Code Submission
7) Checkoff
7.1) Grade
8) (Optional) Extra Pieces
```
## 1) Preparation

This lab assumes you have Py thon 3.6 or later installed on your machine.

The following file contains code and other resources as a starting point for this lab: lab3.zip

Most of your changes should be made to lab.py, which you will submit at the end of this lab. Importantly, you should not
add any imports to the file.

You can also see and participate in online discussion about this lab in the "Lab 3" Category in the forum.

This lab is worth a total of 4 points. Your score for the lab is based on:


```
correctly answering the questions throughout this page (1 point)
passing the test.py tests on the server (1 point, see below), and
a brief "checkoff" conversation with a staff member to discuss your work, and a review of your code's clarity/style (
points).
```
Note that passing all of the tests on the server will require that your code runs reasonably efficiently, and that it makes
reasonably efficient use of memory.

**The questions in section 2 on this page are due before lecture, at 130pm on Monday, 24 Feb. The remaining
questions on this page (including your code submission) are due at 4 pm on Friday, 28 Feb. Checkoffs are due at
10 pm on Wednesday, 4 Mar.**

## 2) Introduction

In this lab, you will be working with freely-available^1 real-world mapping data to solve the realistic large problem of finding
the shortest (or fastest) path between two points on a map. You will implement the backend for a route-finding application,
which we will be able to use to plan paths around Cambridge.

### 2.1) The Data

The data we'll use for this lab comes from OpenStreetMap, and for the bulk to this lab, we are working with data about
Cambridge and the surrounding area (about 650 MB of data). While the raw data (downloaded from here) was specified in
the OSM XML Format, we have done a little bit of pre-processing to convert the data to a format that is slightly easier to
work with.

As in the original format, we have divided the data into two separate pieces: a list of "nodes" (representing individual
locations), and a list of "ways" (representing roads or other kinds of connections between nodes). We have stored these
data in large files that contain many pickled Py thon objects, one for each node/way (the function we used to do this is made
available to you in the util.py file as osm_to_serial_pickles, in case you want to try it on your own data).

The Cambridge data set stores these in two files, which are available in the code distribution:
resources/cambridge.nodes and resources/cambridge.ways.

We have also provided a helper function in util.py called read_osm_data (which has been imported into lab.py for
you). Calling read_osm_data with a filename will produce an object over which you can loop to examine each node/way in
turn. For example, you could use the following code to print all of the nodes in this database:

```
for node in read_osm_data('resources/cambridge.nodes'):
print(node)
```
One important thing worth noting is that this object can only be looped over once (looping over the same data again would
require calling read_osm_data again).

Each node (representing a location) is represented as a dictionary containing the following keys:

```
'id' maps to an integer ID number for the node
'lat' maps to the node's latitude (in degrees)
'lon' maps to the node's longitude (in degrees)
'tags' maps to a dictionary containing additional information about the node, including information about the type of
object represented by the node (traffic lights, speed limit signs, etc).
```

Each way (representing an ordered sequence of connected nodes) is represented as a dictionary with the following keys:

```
'id' maps to an integer ID number for the way
'nodes' maps to a list of integers representing the nodes that comprise the way (in order)
'tags' maps to a dictionary containing additional information about the way (e.g., is this a one-way street? is it a
highway or a pedestrian path? etc.)
```
Try printing out the nodes and ways to get a sense for the kind of information that is available there, and then use Py thon to
answer the following questions about the database.

```
How many total nodes are in the database?
(Hint: it may take a long time and use a large amount of memory to make a big list containing all the nodes; try
looping over the result from read_osm_data instead).
```
```
This question is due on Monday February 24, 2020 at 013000 PM.
```
```
Some of the nodes have a name associated with them (by virtue of having a 'name' entry in their 'tags'
dictionary). How many of the nodes have a name?
```
```
This question is due on Monday February 24, 2020 at 013000 PM.
```
```
What is the ID number of the node named '77 Massachusetts Ave'?
```
```
This question is due on Monday February 24, 2020 at 013000 PM.
```
```
How many total ways are in the database?
```
```
This question is due on Monday February 24, 2020 at 013000 PM.
```
```
How many of these are one-way streets? (Hint: look at the 'oneway' key in the 'tags' dictionary if it exists to
make a decision; if that key doesn't exist, assume the road is two-way)
```
```
This question is due on Monday February 24, 2020 at 013000 PM.
```
This might give you a sense of the scale of the database we're working with; there's a lot of information here!

### 2.2) Testing


We have provided three datasets for you to work with as you are testing your code, and also a visualization (see section 3.3).

As you are debugging, you may wish to primarily make use of the mit data set (which contains some manually-constructed
nodes and ways), which is relatively small compared to the others (to the extent that it should be possible to manually
compute some relevant results after printing out the nodes and ways contained therein).

midwest is quite a bit larger, so it may not be possible to manually compute results from it, but it should load relatively
quickly, so it can be used for testing on a slightly larger scale (using the visualization, for example).

cambridge is _really_ big, and so working with it can take a while. As such, you may not want to do too much testing with it
until you are reasonably sure that things are working. However, once things _are_ working, it can be neat to plan paths through
Cambridge and the surrounding area using the visualization.

## 3) Shortest Paths

Now that we've had a chance to get used to the format of the data set, we'll embark on our first of two tasks for the lab:
finding the shortest path between two locations. Ultimately, we will implement this as a function find_short_path in
lab.py. This function takes three arguments:

```
aux_structures: some structures of your creation (see below)
loc1: a tuple of (latitude, longitude) of our starting location
loc2: a tuple of (latitude, longitude) of our ending location
```
We are interested in returning the shortest path (in miles) between those two locations.

Ultimately, find_short_path should return a list of (latitude, longitude) tuples (each corresponding to a single
node) representing a path between the two given locations, where each pair of adjacent nodes must be connected by a way.
If no such path exists, you should return None.

You can make use of the ideas introduced in lecture on 24 Feb, or look here for a brief overview of one appropriate
algorithm.

### 3.1) Design Considerations

This procedure is fairly complicated, and so are the data we're given, so we'll clarify a few things and make a few small
simplifying assumptions, as indicated below.

#### 3.1.1) Road Types

The datasets we're working with contain information not only about roadways, but also about bicycle paths, pedestrian
paths, buildings, etc. Since we are (for now, at least) planning paths for cars only, we will only consider a way to be valid for
our purposes if it is a roadway (we're responsible citizens of the world, so we won't drive on a bike path or a pedestrian-only
walkway).

Some ways in the dataset have a tag called 'highway', indicating that the path represents a path people can use to travel
(as opposed to the outline of a building, a river, the outline of a park, etc). We will use this tag to decide what kinds of ways
to include in our results.

In particular, we'll only consider a way as part of our path-planning process if:

```
it has a 'highway' tag, and
its 'highway' tag is in the ALLOWED_HIGHWAY_TYPES set that is defined at the top of lab.py.
```

(Ways that don't have these properties should be ignored completely)

#### 3.1.2) Connectedness

We will assume that we can travel from a node to another node if and only if there is a way that connects them. For example,
if we have the following two ways in the database:

```
w1 = {'id': 1 , 'nodes': [ 1 , 2 , 3 ], 'tags': {}}
w2 = {'id': 2 , 'nodes': [ 5 , 6 , 7 ], 'tags': {'oneway': 'yes'}}
```
then moving from node 1 to node 2 , from 2 to 3 , from 3 to 2 , or from 2 to 1 are all OK because w1 represents a
bidirectional street. But, while moving from 5 to 6 or from 6 to 7 are OK, moving from 7 to 6 or from 6 to 5 are **not** OK
because w2 represents a one-way street (we're responsible citizens of the world, so we respect one-way restrictions).

Note that moving directly from node 1 to node 3 is **not** possible given the above, unless there is another way that directly
connects those two nodes.

As we're planning paths between nodes, we'll want to make sure that we only consider a node as a possibility if it exists as
part of a way (we can ignore all other nodes in the database).

#### 3.1.3) Distance Measure

Throughout this lab, we will use an approximation for distance (in miles) that takes into account the approximate curvature
of the earth. This approximation has been defined as a function great_circle_distance in util.py (and it has been
imported into lab.py for your use).

**You should not use any other distance measure for this lab** , as the test cases expect you to use this measure of
distance.

```
What is the distance (in miles) between the following two locations, specified in terms of latitude and longitude?
Location 1: (42.363745, -71.100999)
Location 2: (42.361283, -71.239677)
```
```
Enter your answer as accurately as Py thon provides it to you; don't round!
```
```
This question is due on Friday February 28, 2020 at 040000 PM.
```
```
In the midwest dataset, what is the distance (in miles) between the nodes with the following ID numbers?
ID 1: 233941454
ID 2: 233947199
```
```
This question is due on Friday February 28, 2020 at 040000 PM.
```

```
In the midwest dataset, there is a way with ID number 21705939. If we were to follow that way from its
beginning node to its ending node (and through all the intermediate nodes), how many miles would we have
traveled?
```
```
This question is due on Friday February 28, 2020 at 040000 PM.
```
#### 3.1.4) Starting and Ending Points

It is entirely possible that the locations passed to find_short_path will not correspond exactly to a node in the dataset. As
such, we will instead plan our shortest paths as follows, where a "relevant" way refers to any way that we will actually
consider in our path planning (i.e., one that has not been ignored by the rules in section 3.1.1):

```
finding the nearest node to loc1 that is part of a relevant way (call this node )
finding the nearest node to loc2 that is part of a relevant way (call this node )
finding the shortest path from to (in terms of miles)
```
After finding and , we will ignore loc1 and loc2 completely during the search process. They should not show up in
the path, and they should not be considered when computing the total distance represented by the path.

```
In the midwest dataset, what is the ID number of the nearest relevant node to location (41.4452463,
-89.3161394), i.e., the nearest node to that location that is part of a way we'll actually consider in our path-
planning?
```
```
This question is due on Friday February 28, 2020 at 040000 PM.
```
### 3.2) Auxiliary Data Structures

As we saw in lab 2, the choice of internal data structures is going to be important (in fact, this is even more true in this lab,
which makes use of a _lot_ more data than lab 2).

As such, you are free to create whatever data structures you like by filling in the build_auxiliary_structures function
in lab.py. The object returned by this function will be passed in as the first argument to your path finding functions.

The ultimate goal of these auxiliary structures is to be able to quickly answer questions about the data that we'll need to
answer repeatedly (without looping over the whole dataset). We set things up this way so that we can build these structures
once, and then use them multiple times to compute various results. As such, it is OK for this function to be a bit slow, so
long as it saves time during the actual search process.

However, it may not be possible to store all of the nodes or all of the ways in memory (so if there are nodes and/or ways that
you know will be irrelevant, you should not store them in memory).

### 3.3) Visualization

As with the last lab, we have provided a web-based interface that acts as a visualization for your code.

#### n 1

#### n 2

#### n 1 n 2

#### n 1 n 2


Here, we use leaflet to display map data from the Wikimedia foundation (more information about their Maps service is
available here).

You can start the server by running server.py but providing the filename of one of the datasets as an argument, for
example:

```
python3 server.py midwest
```
or

```
python3 server.py cambridge
```
(if you are not using a terminal to run your code, you can replace sys.argv[1] in server.py with a hard-coded name of a
dataset, like 'midwest', which you can then change later to load in different datasets).

This process will first build up the necessary auxiliary structures for pathfinding by calling your
build_auxiliary_structures function, and then it will start a server. After the server has successfully started, you can
interact with this application by navigating to [http://localhost:6009/](http://localhost:6009/) in your web browser. In that view, you can double-
click on two locations to find and display a path between them.

Alternatively, you can manually call your path-finding procedure to generate a path, and then pass its path to the provided
to_local_kml_url function to receive a URL that will initialize to display the resulting path. For example, to show a path
from Adam's high school to Timber Edge Alpaca Farms (where Adam worked in high school), you could run the following:

```
phs = (41.375288, -89.459541)
timber_edge = (41.452802, -89.443683)
aux = build_auxiliary_structures('resources/midwest.nodes', 'resources/midwest.ways')
print(to_local_kml_url(find_short_path(aux, phs, timber_edge)))
```
which should print out a URL that, when pasted into the browser, shows the path you found (assuming the server is
running). If you started the server with the midwest dataset loaded, you can then double-click around that area to find
other paths.

## 4) Improving Runtime With Heuristics

By now, your code should be working for planning paths, and it should be passing the test cases associated with
find_short_path. In this section, we'll introduce an optimization that will allow us to speed up our searches while retaining
the optimality of the paths we return.

The method for finding minimum-cost paths described on the page linked above involves searching radially outward from a
starting point in terms of increasing cost, but it does not take into account information about where the goal is, and so it can
waste time considering paths that don't really make sense (for example, paths that move off in the wrong direction, away
from the goal position).

We consider those paths because our search process was deciding which paths to consider based on the total cost of the
path so far, let's call it :

```
= the path cost from the starting node to node
```
#### g ( n )

#### g ( n ) n


When we have multiple paths we could consider, we always start with the one that has the smallest value (the shortest
total distance in miles), with no regard for whether that path was moving in a sensible direction or not. While this does
guarantee that we end up with an optimal path, it wastes time.

We can do a bit better by introducing the notion of a _heuristic function_ :

```
= the estimated cost of the lowest-cost path from node to the goal node
```
From this, we can derive a new function:

Because represents the path cost from the start node to node , and is the estimated cost of the lowest-cost
path from to the goal, we have:

```
= the estimated cost of the lowest-cost solution involving
```
Because the total cost of the path is the thing we're trying to optimize, we can make a slight change to the algorithm from
above: when we have multiple paths to consider, we should always choose the one with the lowest value as the next
path to be considered (i.e., the one with the lowest estimated total path cost). By so doing, we can focus our attention on
paths that kind of move toward the goal, and avoid wasting time considering other paths. The most effective heuristics will
be easy to compute but will be reasonably accurate estimates of the cost of the optimal path to the goal.

Note that the introduction of a heuristic does not change the **cost** of the paths, only the order in which we consider the
various paths.

### 4.1) Heuristics and Optimality

Interestingly, as long as we're careful with the design of our heuristics, we can get this benefit without sacrificing the
optimality of the path we return, so long as our heuristic has a couple of properties, which we call _admissibility_ and
_consistency_ :

```
a heuristic is admissible if it never overestimates the cost of the optimal path from any node to the goal, i.e., for all
nodes :
```
```
where represents the actual cost of the least-cost path to the goal.
```
```
a heuristic is consistent if the value of the heuristic never increases as we get closer to the goal. More precisely, a
heuristic is consistent if, for each node and each successor of that node, the heuristic evaluated at is no more
than the heuristic evaluated at plus the cost of traveling directly from to :
```
If these two properties hold for our heuristic function , then we are guaranteed that we will still find an optimal path if
we sort our agenda according to.

### 4.2) A Heuristic For This Problem

Given the above, a reasonable heuristic in this domain is the distance directly from the given node to the goal node:

#### g ( n )

#### h ( n )

#### h ( n ) n

#### f ( n )= g ( n )+ h ( n )

#### g ( n ) n h ( n )

#### n

#### f ( n ) n

#### f ( n )

#### n

#### h ( n )≤ c ∗( n )

#### c ∗( n )

#### n n ′ n

#### n ′ n n ′

#### h ( n )≤ c ( n , n ′)+ h ( n ′)

#### h ( n )

#### f ( n )= g ( n )+ h ( n )

#### h ( n )= ( n ,goal)


This function provides a decent estimate of our overall cost, it is admissible and consistent, and it is pretty fast to compute.
As such, we should expect that it will do a decent job of improving the efficiency of our search procedure.

To test this theory, try running a search between the following two locations using the cambridge dataset, both _with_ and
_without_ the heuristic, and make note of _the total number of paths we pull off of the agenda in each case_. How do those two
numbers differ?

```
Location 1: (42.3858, -71.0783)
Location 2: (42.5465, -71.1787)
```
**Be prepared to discuss these results, including how you computed them, during your checkoff conversation.**

## 5) Need for Speed (Limits)

So far, our planning has been based purely on distance, but oftentimes, when planning a route between two locations, we
are actually interested in the amount of _time_ it will take to move from one location to another.

For the last part of this lab, you should implement find_fast_path, which, unlike find_short_path, should take into
account speed limits (we're responsible citizens of the world, so we won't drive over the speed limit).

Some ways in the dataset store information about the speed limit along that way. That said, unfortunately, speed limit
information is somewhat sparse in OSM data (at least in these datasets), and so we'll have to guess a little bit for some of
the roads. We have done a little bit of preprocessing of the data for you, to make extracting this information a little bit easier
than it would otherwise be.

For each way, we'll determine the speed limit as follows:

```
if the way has the 'maxspeed_mph' tag, the corresponding value (an integer) represents the speed limit in miles per
hour
if that tag does not exist, look up the way's 'highway' type in the DEFAULT_SPEED_LIMIT_MPH dictionary and use
the corresponding value.
```
If two nodes are connected by more than one way with distinct speed limits, you should always prefer higher of the two
speed limits.

Note that implementing this new function might require changing or reorganizing your code for find_short_path and/or
for build_auxiliary_structures. Even if it isn't required for functionality, you may also be presented with opportunities
to refactor your lab.py to avoid duplicated code.

### 5.1) Heuristics

With this new notion of optimality, the heuristic function from earlier is no longer admissible (so we are no longer guaranteed
to return an optimal path!).

As such, for this part of the lab, it is fine not to use a heuristic; but you may find it interesting to try to come up with an
effective heuristic that is admissible and consistent given this new measure of cost, in order to speed up your search.

### 5.2) Visualization

If, after starting server.py, you open the following URL in your browser (note the difference from above), the web UI will
use your find_fast_path instead of find_short_path for pathfinding when double-clicking:


```
http://localhost:6009/?type=fast
```
Try using both this and the shortest-path metric between a few different points on the Cambridge map. How would you
expect those to differ? Do your results match your expectation?

**During the checkoff, you will be asked to demonstrate your code running in the UI by finding both the shortest and
fastest paths from Waltham, MA (west of Cambridge on the map) to Salem, MA (north and east of Cambridge)
using the cambridge data set. As such, please make sure that you've got the server running with the cambridge
dataset loaded when you ask for your checkoff.**

## 6) Code Submission

When you have tested your code sufficiently on your own machine, submit your modified lab.py below. Note that your
checkoff (including style considerations) will be based on your most recent submission, and that all aspects of the file will
be considered in terms of style, including those that are not explicitly tested in test.py (for example, any helper functions
you write).

When submitting lab.py, the server will run the tests and report back the results (including timing). Submit your lab.py in
the box below:

```
Select File^ No file selected
This question is due on Friday February 28, 2020 at 040000 PM.
```
## 7) Checkoff

When you are ready, please come to a lab session or office hour and add yourself to the queue asking for a checkoff. **You
must be ready to discuss your code in detail before asking for a checkoff.** Since the clarity of your code will be
evaluated as part of the checkoff, you may wish to take some time to comment your code, use good variable names, avoid
repetitive code (create helper methods), etc.

Be prepared to discuss:

```
What auxiliary data you stored in build_auxiliary_structures, and why.
Your implementation of find_short_path.
Imagine using a BFS (as discussed in tutorial 2) to find shortest paths, instead of this method. What might we expect
to be different about the paths returned from BFS, versus the paths we're returning here?
Your implementation of the heuristic function, and the effect that it had on the number of total paths we considered in
the course of our search.
Your implementation of find_fast_path, and how it differs from find_short_path.
Demonstrate your code running in the UI by finding both the shortest and fastest paths from Waltham, MA (west of
Cambridge on the map) to Salem, MA (north and east of Cambridge) using the cambridge data set. What
differentiates these paths? Why do they look the way they do?
```
### 7.1) Grade

```
You have not yet received this checkoff. When you have completed this checkoff, you will see a grade here.
```

## 8) (Optional) Extra Pieces

If you are interested, there are a number of ways you could modify or build on the code from this lab to do some other kinds
of cool things! Here are a few ideas, and we're happy to help if you're interested to work on any of these (or on other ideas
of your own!):

```
. Import data from your own home state / home country! You can download data from http://download.bbbike.org/osm/
or https://download.geofabrik.de/ and use the osm_to_serial_pickle function in util.py to get convert the data
to the format used in this lab.
```
```
But be warned that if you're importing data from a country that uses a sensible measure of speed like kph, the speed
limit calculations might be a bit off, since they assume MPH...so that might require some other adjustments.
```
```
. Implement an admissible heuristic for the version of the code that takes speed limits into account.
```
```
. Use the route planning for bicycles or pedestrians rather than motorists by modifying the "highway" types that are
allowed. See this page for a list of "highway" types, and note also that many ways contain information about bicycle
travel.
```
```
Note that you probably won't be able to implement the 'speed limit' behavior in this case :)
```
```
. Often times, there are considerations other than speed limit that factor in to what makes a path desirable. You could
take this into account by introducing a penalty (in terms of cost) for paths that move through nodes that are labeled as
traffic lights, or for any time we transition between ways, or something like that.
```
```
. Depending on how you implemented your search process, you might have been storing (cost, path) tuples or
something like that, and using sort to sort that list each time through a loop. While this definitely works, there are
ways to make that process much more efficient, in particular by using the structures from the heapq module, which
provides a nice set of abstractions for maintaining a sorted list of items efficiently (you are welcome to import this
module if you wish).
```
```
. Depending on how you implemented things, you may find that a lot of the time spent planning paths between two
locations is actually spent looking up the two nodes that we want to use as our starting and ending points. So you
might try to find a way to speed that process up.
```
```
. As it currently stands, we are ignoring a large class of tags called "relations" in the OSM data, which include
restrictions on driving such as "no left turns." You could try modifying your code to take these relations into account.
```
**Footnotes**

(^1) In fact, all of the software and data we are working with in this lab is freely available, including the mapping data, the
images used to render the maps, and the software used to control the map display! In fact, a lot of the world's most-widely-
used software is created in this same spirit of sharing and community. If you are interested in exploring the philosophical
ideas behind these movements, a place to start would be the Wikipedia articles on the Free/Libre Software and Free-culture
movements. Or talk with Adam (who is passionately interested in these ideas).


