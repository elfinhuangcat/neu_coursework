#!/usr/bin/env python
from griddefin import *
def output_message_before_start():
    print('Press \'s\' to start the game.\n',
          'Press \'e\' to exit the program.\n',
          'Please input:')

def output_sign(aiSign,playerSign):
    print('Your notation is \''+playerSign+'\','
          'and AI\'s is \''+aiSign+'\'.\n')

def output_error_before_start():
    print('Input not correct!\n')

def output_who_go_first(turn):
    if turn:
        print('AI goes first.')
    else:
        print('You go first.')

def output_move(turn,move):
    """turn : True means AI made the move.
              False means the player made the move."""
    if turn:
        print('AI\'s move: ' + move + '\n')
    else:
        print('Your move: ' + move + '\n')

def output_game_result(terminalStatus):
    if terminalStatus == 1:
        print('AI won!!\n')
    elif terminalStatus == 2:
        print('You won!!\n')
    else:
        print('Draw!!')

def output_illegal_input():
    print('Illegal input! Please try again.\n')
