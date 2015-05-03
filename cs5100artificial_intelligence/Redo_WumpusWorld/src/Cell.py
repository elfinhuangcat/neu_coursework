#!/usr/bin/env python

"""
This module defines the Cell structure that will be used by the agent.
Each cell contains these information:
1. Is it safe?
2. Is it a pit?
3. Is there a wumpus?
4. Is there a piece of gold?
"""

class Cell:
    """
    Class name: Cell
    Variables:
        1. _safe: True if this cell is safe.
                  False if this cell is dangerous.
                  None if no info is provided.
        2. _pit: True if this cell is a pit.
                 False if this cell is DEFINITELY not a pit.
                 None if no info is provided.
        3. _wumpus: True if there is a wumpus in this cell.
                    False if there is no wumpus in the cell.
                    None if no info is provided.
        4. _gold: True if there is a piece of gold.
                  False if there is no gold in the cell.
                  None if no info is provided.
    Methods:
        1. Getters for each variable.
        2. Setters for each variable.
        3. setter_input_argument_check(v)
    """
    def __init__(self):
        # Among these values, None represents "Not known",
        # False represents "False"
        # True represents "True"
        # At first we don't know any info about the cell, so we 
        # store the info as None
        self._safe = None
        self._pit = None
        self._wumpus = None
        self._gold = None
    
    ## Getter functions:
    def get_safe(self):
        return self._safe
    def get_pit(self):
        return self._pit
    def get_wumpus(self):
        return self._wumpus
    def get_gold(self):
        return self._gold
    
    ## Setter functions:
    ## The input arguments can only be either `True` or `False`
    def set_safe(self, v):
        if self.setter_input_argument_check(v):
            self._safe = v
    def set_pit(self,v):
        if self.setter_input_argument_check(v):
            self._pit = v
    def set_wumpus(self,v):
        if self.setter_input_argument_check(v):
            self._wumpus = v
    def set_gold(self,v):
        if self.setter_input_argument_check(v):
            self._gold = v
    
    ## Input argument check for setters:
    def setter_input_argument_check(self,v):
        """
        Function name: setter_input_argument_check
        Input: A value `v`
        Output: True if `v` is boolean. Otherwise returns False.
        """
        if v != True and v != False:
            raise Exception('Setter input argument error:' + 
                            'Can only accept boolean values.\n')
            return False
        else:
            return True
        