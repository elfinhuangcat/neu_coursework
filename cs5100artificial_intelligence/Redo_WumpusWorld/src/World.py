#!/usr/bin/env python
import Cell
"""
This module defines the class World which will be utilized by the agent.
The instance of class World represents the information got or inferred by
the agent. This structure also contains information about the agent itself.
The world is made up of cells.
"""

class World:
    def __init__(self, height, width, goal, startpos):
        # A matrix representing the map inferred by the agent.
        self._h = height
        self._w = width
        self._matrix = [[Cell.Cell() for dummy_i in xrange(width)] 
                       for dummy_y in xrange(height)]
        # The number of arrows the agent is holding
        self._arrow = 1
        # The exit
        self._goal = goal
        # The current agent's position
        self._pos = startpos
        # The direction that the agent is facing towards.
        # It can be "NORTH", "SOUTH", "EAST", "WEST"
        # At first it is set to be "NORTH"
        self._facing = "NORTH"
        
        ## Getters:
        def get_h(self):
            return self._h
        def get_w(self):
            return self._w
        def get_matrix(self):
            return self._matrix
        def get_arrow(self):
            return self._arrow
        def get_goal(self):
            return self._goal
        def get_pos(self):
            return self._pos
        def get_facing(self):
            return self._facing
        
        ## Setters:
        def set_matrix(self,m):
            self._matrix = m
        def use_arrow(self):
            if (get_arrow() > 0):
                self._arrow -= 1
            else:
                raise Exception('You have used up arrows!\n')
        def action_result(self,action):
            """
            Function name: action_result
            Input: `action`, which is a string and the possible values are:
                   1. "TURNLEFT"
                   2. "TURNRIGHT"
                   3. "FORWARD"
                   4. "GRAB"
                   5. "SHOOT"
            Output: None
            Effect: 1. "TURNLEFT","TURNRIGHT" will change the value of _facing.
                    2. "FORWARD" will change the value of _pos.
                    3. "GRAB" will call ANOTHER FUNCTION TO BE FIX HERE
                    4. "SHOOT" will call function use_arrow and ANOTHER FUNCTION TO BE FIX HERE.
            """
            
        
        