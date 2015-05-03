#!/usr/bin/env python
from griddefin import *
class State:
    def __init__(self,preState=None,preMove=None,player=True):
        """If preState == None, then this state is the first
           move."""
        """ self.moveval is a dict.
            It represents the correspondance of a move and its
            utility value. The utility value will be filled after
            you run the ab_search."""
        self.moveval = {}
        """self.grid tells us what does the state looks like now."""
        self.grid = Grid()
        """self.lastTurn tells us whose turn it was.
           True : Last turn is the player's turn
           False: Last turn is the AI's turn"""
        self.lastTurn = player
        if (preState == None):
            self.moveval = {'1':0,'2':0,'3':0,'4':0,'5':0,'6':0,
                             '7':0,'8':0,'9':0}
        else:
            self.init_grid(preState,preMove)
            self.init_move()

    def get_moveval(self):
        return self.moveval
    def add_moveval(self,move,value):
        self.moveval[move] = value
    def del_moveval(self,move):
        del self.moveval[move]
    def get_grid(self):
        return self.grid
    def get_lastTurn(self):
        return self.lastTurn
    def set_lastTurn(self,turn):
        self.lastTurn = turn
    def print_state(self):
        self.grid.print_grid()

    def init_grid(self,preState,move):
        """preState is an instance of class State, and it
           is the previous state of this state.
           move is the move led preState to this state.
           We assume that the move is legal."""
        for i in range(9):
            (self.grid.get_data())[i] = (preState.grid.get_data())[i]
        if self.lastTurn: # If was is the player's move:
            (self.grid.get_data())[int(move)-1] = self.grid.get_playerSign()
        else:
            (self.grid.get_data())[int(move)-1] = self.grid.get_aiSign()

    def init_move(self):
        legalMoves = self.get_legal_moves()
        for x in legalMoves:
            # Initialize self.moveval
            self.moveval[x] = 0

    def get_legal_moves(self):
        return self.grid.legal_move_list()
    def legal_move_judge(self,move):
        return self.grid.legal_move_judge(move)
        
    def result(self,move):
        """Given this state, returns the next state."""
        if self.legal_move_judge(move):
            nextState = State(self,move,not(self.lastTurn))
            return nextState
        else:
            print('Illegal move happened in state:'
                  + self.data + '.\n'
                  +'(result() function)')
    def ab_search(self):
        """Return the action with value v."""
        v = max_value(self,-2500,2500) #2500 represents Infinity.
        for x in self.moveval.keys():
            if self.moveval[x] == v:
                return x
    def terminal_test(self):
        """Terminal Test Function:
           0 : The game has not terminated.
           1 : AI wins.
           2 : Player wins.
           3 : Draw."""
        return self.grid.terminal_test()
    """END OF CLASS STATE"""

def utility(state):
    if state.grid.terminal_test() == 1:
        return 1
    elif state.grid.terminal_test() == 2:
        return 0
    elif state.grid.terminal_test() == 3:
        return 0.5

def max_value(state,a,b):
    """Returns a utility value."""
    if state.grid.terminal_test() != 0:
        return utility(state)
    else:
        v = -2500 #2500 represents Infinity.
        for move in state.moveval.keys():
            v = max(v,min_value(state.result(move),a,b))
            state.moveval[move] = v
            if v >= b:
                return v
            a = max(a,v)
        return v

def min_value(state,a,b):
    """Returns a utility value."""
    if state.grid.terminal_test() != 0:
        return utility(state)
    else:
        v = 2500 #2500 represents Infinity.
        for move in state.moveval.keys():
            v = min(v,max_value(state.result(move),a,b))
            state.moveval[move] = v
            if v <= a:
                return v
            b = min(b,v)
        return v
        

if __name__ == '__main__':
    stateA = State()
    # AI chooses the move '4'
    stateB = State(stateA,'4',True)
    stateC = stateA.result('4')
        
