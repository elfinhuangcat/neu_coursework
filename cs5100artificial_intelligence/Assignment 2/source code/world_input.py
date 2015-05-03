#!/usr/bin/env python
"""world_input.py:
   This file define the function world_input(), which is
   in charge of reading the wumpus_world.txt and create
   the world with the data structure World.
   When the AI is playing this game, it is permitted to read
   some of the information in this world.
   """

import world_structure

"""world_input():
   Given the file path, this method will read the content of
   this file and return a World object defined by the content.
   """
def world_input():
    INPUT_MODE = True
    path = 'wumpus_world.txt'
    if INPUT_MODE:
        """1. Let the user enter the path of the input txt file."""
        print("Please input the name of the world file you put under"
              + " the directory \"source code\".\n eg.wumpus_world.txt")
        path = input("Please enter the file name: ")
    inputFile = open(path, 'r')
    w = world_structure.World()

    for line in inputFile:
        if line[0].isalpha():
            s = fetch_index_str(line)
            x = fetch_x(s)
            y = fetch_y(s)
            if line[0] == 'A':
                w.set_agent_pos(x,y)
            elif line[0] == 'B':
                w.add_breeze(x,y)
            elif line[0] == 'G' and line[1] != 'O':
                w.add_gold(x,y)
            elif line[0] == 'G' and line[1] == 'O':
                w.set_goal(x,y)
            elif line[0] == 'M':
                w.set_map(x,y)
            elif line[0] == 'P':
                w.add_pit(x,y)
            elif line[0] == 'S':
                w.add_stench(x,y)
            elif line[0] == 'W':
                w.set_wumpus(x,y)
    return w

"""fetch_index_str(line):
   line : A line of the input file which starts with a letter.
   This method returns the part of the line indicating the dimensions
   of the world.
   """
def fetch_index_str(line):
    i = 0
    while i < len(line) and line[i].isalpha():
        i = i + 1
    tmp = line[i:]
    while i < len(tmp) and (tmp[i].isdigit() or tmp[i] == ','):
        i = i + 1
    return tmp[:i]

"""fetch_x(s):
   s : A string only consists of the dimensions.
   This method returns the x coordinate.
   """
def fetch_x(s):
    if len(s) == 2:
        return int(s[0])
    else:
        i = 1
        while i < len(s) and s[i] != ',':
            i = i + 1
        return int(s[:i])

"""fetch_y(s):
   s : A string only consists of the dimensions.
   This method returns the y coordinate.
   """
def fetch_y(s):
    if len(s) == 2:
        return int(s[1])
    else:
        i = 1
        while i < len(s) and s[i] != ',':
            i = i + 1
        return int(s[(i+1):])
        

if __name__ == '__main__':
    w = world_input()
    print('mapW: ' + str(w.get_mapW()))
    print('mapH: ' + str(w.get_mapH()))
    print('pos: ' + str(w.get_agent_pos()))
    print('pits: ' + str(w.pits)) #Didn't provide get method
    print('wumpus: ' + str(w.wumpus)) #Didn't provide get method
    print('breeze: ' + str(w.breezeList)) #Didn't provide get method
    print('stench: '+ str(w.stenchList)) #Didn't provide get method
    print('gold: ' + str(w.goldList)) #Didn't provide get method
    print('goal: ' + str(w.get_goal()))
    print('arrow: ' + str(w.get_arrow()))
    

