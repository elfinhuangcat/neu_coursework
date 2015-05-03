README
======

This is the README file for the Wumpus World project. This is a redo version and we will not use the prover9 to do inference. Instead, we will make up our own inference methods. (The shortcoming of doing this is the process of translating the rules into python code!) The purpose of redoing this project is to practice python.

####Wumpus World PEAS Description

1. **Performance measure**  
  gold +1000  
  death -1000  
  -1 per step  
  -10 for using the arrow

2. **Environment**  
  Squares adjacent to wumpus are SMELLY  
  Squares adjacent to pit are BREEZY  
  GLITTER iff gold is in the same square  
  SHOOT kills wumpus if you are facing it  
  SHOOT uses up the only arrow  
  GRAB picks up gold if in same square

3. **Sensors**  
  BREEZE  
  GLITTER  
  SMELL

4. **Actuators**  
  TURNLEFT  
  TURNRIGHT  
  FORWARD  
  GRAB  
  SHOOT

####Input World Format

`Mxy`: x is the width and y is the height. When x or y is larger than 9, `Mx,y` will be used instead.  
`Axy`: Represents the starting (x,y) position of the agent. When x or y is larger than 9, `Ax,y` will be used instead.  
`Bxy`: There is BREEZE in (x,y). When x or y is larger than 9, `Bx,y` will be used instead.  
`Sxy`: There is SMELL in (x,y). When x or y is larger than 9, `Sx,y` will be used instead.  
`Pxy`: There is a PIT in (x,y). When x or y is larger than 9, `Px,y` will be used instead.  
`Wxy`: There is a WUMPUS in (x,y). When x or y is larger than 9, `Wx,y` will be used instead.  
`Gxy`: There is a GOLD/GLITTER in (x,y). When x or y is larger than 9, `Gx,y` will be used instead.  
`GOxy`: After the agent traversed the world, it should go back to the position (x,y). When x or y is larger than 9, `GOx,y` will be used instead.

Please find the sample input world in the following link:  
<https://github.com/elfinhuangcat/PythonRelated/blob/master/AI_assign_proj/Assignment%202/source%20code/wumpus_world_m44.txt>

####Reference

1. A pdf about the wumpus world: <https://courses.cs.washington.edu/courses/cse473/06au/schedule/lect8.pdf>