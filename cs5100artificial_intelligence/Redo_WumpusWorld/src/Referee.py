#!/usr/bin/env python

"""
This module defines the class structure for Referee. The instance of this 
class is a referee who is responsible for recording the score and monitoring
and receiving the actions of the agent. The referee needs to respond to the 
agent's action as well.
"""

class Referee:
    def __init__(self, height, width):
        self._cell_dict = {0:"BREEZE",
                           1:"SMELL",
                           2:"GLITTER", # Equivalent to "GOLD".
                           3:"PIT",
                           4:"WUMPUS"}
        self._h = height
        self._w = width
        self._map = [[[0,0,0,0,0] for dummy_i in range(width)] 
                     for dummy_y in range(height)]
        
        