#!/usr/bin/env python
"""world_structure.py:
   """

class World:
    def __init__(self):
        """mapW: The width of map.
           mapH: The height of map.
           pos: The position of the agent (x,y)
           pits: A set containing the positions of pits.
           wumpus: The position of the wumpus.
           breezeList: A set containing the positions of breezes.
           stenchList: A set containing the positions of stenches.
           goldList: A set containing the positions of golds.
           arrow: The number of arrows that the agent holds.
           """
        self.mapW = 0 
        self.mapH = 0 
        self.pos = ()
        self.pits = set()
        self.wumpus = ()
        self.breezeList = set()
        self.stenchList = set()
        self.goldList = set() 
        self.goal = ()
        self.arrow = 1
        """More about these data:
           1. Any position is represented as (x,y).
              eg. self.pits = {(x1,y1),(x2,y2)}
           2. The agent can read some information of:
              (1)mapW, mapH
              (2)pos
              (3)breezeList, stenchList, goldList
              (4)goal
              (5)arrow
              by using the corresponding get method.
        """
        
    """set/get map:"""
    def set_map(self,width,height):
        self.mapW = width
        self.mapH = height
    def get_mapW(self):
        return self.mapW
    def get_mapH(self):
        return self.mapH
    """set/get agent position:"""
    def get_agent_pos(self):
        return self.pos
    def set_agent_pos(self,x,y):
        self.pos = (x,y)
    def get_agent_xpos(self):
        return self.pos[0]
    def get_agent_ypos(self):
        return self.pos[1]
    """set/get pits:"""
    def add_pit(self,x,y):
        self.pits = self.pits | {(x,y)}
    def is_it_pit(self,x,y):
        if (x,y) in self.pits:
            return True
        else:
            return False
    """set/get wumpus:"""
    def set_wumpus(self,x,y):
        self.wumpus = (x,y)
    def is_it_wumpus(self,x,y):
        if self.wumpus == ():
            return False
        if x == self.wumpus[0] and y == self.wumpus[1]:
            return True
        else:
            return False
    """set/get breeze:"""
    def add_breeze(self,x,y):
        self.breezeList = self.breezeList | {(x,y)}
    def percept_breeze(self):
        x = self.pos[0]
        y = self.pos[1]
        if (x,y) in self.breezeList:
            return True
        else:
            return False
    """set/get stench:"""
    def add_stench(self,x,y):
        self.stenchList = self.stenchList | {(x,y)}
    def percept_stench(self):
        x = self.pos[0]
        y = self.pos[1]
        if (x,y) in self.stenchList:
            return True
        else:
            return False
    """set/get gold:"""
    def add_gold(self,x,y):
        self.goldList = self.goldList | {(x,y)}
    def percept_glitter(self):
        x = self.pos[0]
        y = self.pos[1]
        if (x,y) in self.goldList:
            return True
        else:
            return False
    def is_it_gold(self,x,y):
        if (x,y) in self.goldList:
            return True
        else:
            return False
    def delete_gold(self,x,y):
        if (x,y) in self.goldList:
            self.goldList = self.goldList - {(x,y)}
        else:
            print("Warning: There is no gold. Nothing will be deleted.\n")
    """set/get goal:"""
    def set_goal(self,x,y):
        self.goal = (x,y)
    def get_goal(self):
        return self.goal
    """get arrow number:"""
    def get_arrow(self):
        return self.arrow

    """The world will change after the wumpus is killed:"""
    def after_wumpus_killed(self):
        """1. Empty the list of stench positions."""
        self.stenchList = {}
        """2. False means the wumpus is dead."""
        self.wumpus = False 
        self.arrow = 0
        return 'Scream'

    def after_gold_picked(self,x,y):
        self.goldList = self.goldList - (x,y)

"""Tests:"""
if __name__ == '__main__':
    w = World()
    w.set_map(4,4)
    w.set_agent_pos(1,1)
    w.add_breeze(2,1)
    w.add_pit(3,1)
    w.add_breeze(4,1)
    w.add_stench(1,2)
    w.add_breeze(3,2)
    w.set_wumpus(1,3)
    w.add_stench(2,3)
    w.add_gold(2,3)
    w.add_breeze(2,3)
    w.add_pit(3,3)
    w.add_breeze(4,3)
    w.add_stench(1,4)
    w.add_breeze(3,4)
    w.add_pit(4,4)
    w.set_goal(1,1)

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
