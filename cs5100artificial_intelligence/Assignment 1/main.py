#!/usr/bin/env python
from griddefin import *
from absearch import *
from inputmodule import *
from outputmodule import *
import random

def main_func():
    """gameStatus: a value to represent the status of the game.
       0 : The program has just been opened and the game has not started
       1 : The game is continuing
       2 : Last game ended and waiting for next instruction
       """
    gameStatus = 0
    terminalStatus = 0
    exitFlag = False

    while gameStatus == 0 and exitFlag == False:
        output_message_before_start()
        gameStatus = input_before_start(input())
        if gameStatus == -1:
            exitFlag = True
          
    while not exitFlag:        
        # Game started.
        turn = random.choice([True,False])
        output_who_go_first(turn)

        """Initialize the game state."""
        if turn:
            """If AI goes first:"""
            state = State()
        else:
            """If the player goes first:"""
            state = State(None,None,False)
        print('Game started:\n')
        output_sign(state.get_grid().get_aiSign(),
                    state.get_grid().get_playerSign())
        state.print_state()
        
        while gameStatus == 1:
            if turn:
                """If the last turn was the player's.
                   i.e. It is AI's turn."""
                move = state.ab_search()
            else:
                """If the last turn was the AI's.
                i.e. It is the player's turn."""
                move = input_move(state)

            output_move(turn,move)
            # Move on to the next state
            state = state.result(move)
            # Print the grid
            state.print_state()
            # Now we should take in the player's next move
            turn = not(turn)
            # And we also need to check if the game has terminated.
            terminalStatus = state.terminal_test()
            """0 : The gamehas not terminated.
               1 : AI wins.
               2 : Player wins.
               3 : Draw."""
            if terminalStatus != 0:
                gameStatus = 0
                output_game_result(terminalStatus)
        while gameStatus == 0 and exitFlag == False:
            output_message_before_start()
            gameStatus = input_before_start(input())
            if gameStatus == -1:
                exitFlag = True
    """END OF main_func()"""
            
if __name__ == '__main__':
    main_func()
