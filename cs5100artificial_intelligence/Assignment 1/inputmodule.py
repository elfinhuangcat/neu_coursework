#!/usr/bin/env python
from griddefin import *
from outputmodule import *
def input_before_start(userInput):
    if userInput == 's':
        return 1
    elif userInput == 'e':
        return -1
    else:
        output_error_before_start()
        return 0

def input_move(state):
    while True:
        userInput = input('The squares with a number are your'
                          ' legal moves. \nPlease input one of '
                          'those numbers:')
        for x in state.get_legal_moves():
            if userInput == x:
                return userInput
        # The user's input is illegal:
        output_illegal_input()
    
