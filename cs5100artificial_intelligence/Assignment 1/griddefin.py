#!/usr/bin/env python
class Grid:
    def __init__(self):
        """self.data represents the 3*3 grid.
            0 - 2 is the first row, 
            3 - 5 is the second row, 
            6 - 8 is the third row."""
        self.data = ['1','2','3','4','5','6','7','8','9']
        self.aiSign = 'O'
        self.playerSign = 'X'

    def get_data(self):
        return self.data
    def set_data(self,new_data_array):
        self.data = new_data_array
    def get_aiSign(self):
        return self.aiSign
    def get_playerSign(self):
        return self.playerSign
    def switch_sign(self):
        tempSign = self.aiSign
        self.aiSign = self.playerSign
        self.playerSign = tempSign
        
    def legal_move_judge(self,move):
        """ move is from '1' to '9'"""
        try:
            x = int(move)
            if x >= 1 and x <= 9:
                if self.data[x-1] == move:
                    # This square has not been occupied
                    return True
                else:
                    # Occupied
                    return False
            else:
                # Illegal input
                return False
        except ValueError:
            return False
        
    def legal_move_list(self):
        """Return a list of legal moves"""
        moveSet = []
        for x in self.data:
            if not(x == self.aiSign or x == self.playerSign):
                moveSet += [x]
        return moveSet
    
    def terminal_test(self):
        """Terminal Test Function:
           0 : The game has not terminated.
           1 : AI wins.
           2 : Player wins.
           3 : Draw."""
        if self.data[0] == self.data[1] == self.data[2]:
            if self.data[0] == self.aiSign:
                return 1
            else:
                return 2
        elif self.data[3] == self.data[4] == self.data[5]:
            if self.data[3] == self.aiSign:
                return 1
            else:
                return 2
        elif self.data[6] == self.data[7] == self.data[8]:
            if self.data[6] == self.aiSign:
                return 1
            else:
                return 2
        elif self.data[0] == self.data[3] == self.data[6]:
            if self.data[0] == self.aiSign:
                return 1
            else:
                return 2
        elif self.data[1] == self.data[4] == self.data[7]:
            if self.data[1] == self.aiSign:
                return 1
            else:
                return 2
        elif self.data[2] == self.data[5] == self.data[8]:
            if self.data[2] == self.aiSign:
                return 1
            else:
                return 2
        elif self.data[0] == self.data[4] == self.data[8]:
            if self.data[0] == self.aiSign:
                return 1
            else:
                return 2
        elif self.data[2] == self.data[4] == self.data[6]:
            if self.data[2] == self.aiSign:
                return 1
            else:
                return 2
        else:
            for x in range(0,9):
                if (self.data[x] != self.aiSign
                    and self.data[x] != self.playerSign):
                    return 0
            return 3

    def print_grid(self):
        print(' - - - - - -\n| '+self.data[0]+' | '+self.data[1]
              +' | '+self.data[2]+' |\n - - - - - -\n| '
              +self.data[3]+' | '+self.data[4]+' | '+self.data[5]
              +' |\n - - - - - -\n| '+self.data[6]+' | '
              +self.data[7]+' | '+self.data[8]+' |\n - - - - - -\n')
    """END OF CLASS Grid"""
    
"""Tests:"""
if __name__ == '__main__':
    testGrid = Grid()
    print(testGrid.legal_move_judge('asdf'))
    print(testGrid.legal_move_judge('\n'))
    print(testGrid.legal_move_judge('1'))
    testGrid.set_data(['O','X','3','4','5','6','7','8','X'])
    print(testGrid.legal_move_judge('1'))
    print(testGrid.legal_move_judge('4'))
    print(testGrid.terminal_test())
