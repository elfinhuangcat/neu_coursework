#!/usr/bin/env python
"""referee.py:
   This is the definition of class Referee.
   The main program should create an object of this class
   to supervise the agent's actions and return the result
   of its action to the agent.
   The referee's obligations are:
   1.Receive action message from the agent.
   2.Judge if the action is legal.
   3.Return the result of the action.
   4.Meanwhile update the world it maintains.

   Permitted agent actions:
   1.'north'   : Agent moving north. y+1
   2.'east'    : Agent moving east.  x+1
   3.'south'   : Agent moving south. y-1
   4.'west'    : Agent moving west.  x-1
   5.'pick'    : Agent picking gold.
   6.'shoot n' : Agent shooting the arrow to the north.
   7.'shoot e' : Agent shooting the arrow to the east.
   8.'shoot s' : Agent shooting the arrow to the south.
   9.'shoot w' : Agent shooting the arrow to the west.

   Possible action results:
   1.'safe'    : The result of agent's moving to another square is safe.
                 A result of 'north' 'east' 'south' 'west'
   2.'pit'     : The agent fell to a pit and died. Game over.
                 A result of 'north' 'east' 'south' 'west'
   3.'wumpus'  : The agent was devoured by the wumpus. Game over.
                 A result of 'north' 'east' 'south' 'west'
   4.'scream'  : The agent killed the wumpus.
                 A result of 'shoot x'
   5.'gold'    : The agent successfully picked a gold.
                 A result of 'pick'
   6.'nothing' : The agent's action made no effect.
                 A result of 'shoot x' or 'pick'
   7.'illegal' : The action just made is illegal.

   Game Scoring:
   1.Move made: -1
   2.Arrow shooted: -100
   3.Gold picked: -1000
   4.Died: Game over.   
   """
import world_input
import world_structure
import agent

class Referee:
    def __init__(self):
        self.DEBUG = False
        self.score = 0
        self.w = None
        self.agent = None
        self.goldNumber = 0
        self.originGoldNumber = 0
        self.init_world()
        self.moves = 0

    """init: """
    def init_world(self):
        self.w = world_input.world_input()
        self.goldNumber = len(self.w.goldList)
        self.originGoldNumber = self.goldNumber

    """get/set score:"""
    def get_score(self):
        return self.score
    def add_score(self, num):
        self.score = self.score + num
    def deduct_score(self, num):
        self.score = self.score - num
    """get/set goldNumber:"""
    """P.S. The agent should not know the gold number."""
    def get_goldNumber(self):
        return self.goldNumber
    def set_goldNumber(self, num):
        self.goldNumber = num
    def decrease_goldNumber(self):
        self.goldNumber = self.goldNumber - 1

    """Apply to agent init: """
    def get_mapW(self):
        return self.w.get_mapW()
    def get_mapH(self):
        return self.w.get_mapH()
    def get_agent_xpos(self):
        return self.w.get_agent_xpos()
    def get_agent_ypos(self):
        return self.w.get_agent_ypos()
    def get_goal(self):
        return self.w.get_goal()

    """Apply to the agent's sensor: """
    def percept_stench(self):
        return self.w.percept_stench()
    def percept_breeze(self):
        return self.w.percept_breeze()
    def percept_glitter(self):
        return self.w.percept_glitter()
        
    """agent register:"""
    def register_agent(self, a):
        self.agent = a

    """Receive action from agent: """
    def action_rev(self,act):
        if (act == 'north' or 
           act == 'east' or 
           act == 'south' or 
           act == 'west'):
            return self.move_rev(act)
        elif act == 'pick':
            return self.pick_rev()
        elif act[0:5] == 'shoot':
            if self.w.arrow <= 0:
                return self.send_msg('illegal')
            else:
                return self.shoot_rev(act)
        else:
            return self.send_msg('illegal')

    def move_rev(self,act):
        self.moves += 1
        x = self.w.get_agent_xpos()
        y = self.w.get_agent_ypos()
        if act == 'north':
            y = y + 1
        elif act == 'east':
            x = x + 1
        elif act == 'south':
            y = y - 1
        elif act == 'west':
            x = x - 1
        if (x > self.w.get_mapW()
            or y > self.w.get_mapH()
            or x <= 0
            or y <= 0):
            return self.send_msg('illegal')
        else:
            """Move made. Score - 1."""
            self.deduct_score(1)
            self.w.set_agent_pos(x,y)
            if self.w.is_it_pit(x,y):
                return self.send_msg('pit')
            elif self.w.is_it_wumpus(x,y):
                return self.send_msg('wumpus')
            else:
                return self.send_msg('safe')
    
    def pick_rev(self):
        x = self.w.get_agent_xpos()
        y = self.w.get_agent_ypos()
        if self.w.is_it_gold(x,y):
            """Gold picked. Score + 1000"""
            self.add_score(1000)
            self.goldNumber = self.goldNumber - 1
            self.w.delete_gold(x,y)
            return self.send_msg('gold')
        else:
            return self.send_msg('nothing')
            
    def shoot_rev(self,act):
        """Arrow shooted. Score - 100."""
        self.deduct_score(100)
        self.w.arrow = 0
        wumpusKilled = False
        x = self.w.get_agent_xpos()
        y = self.w.get_agent_ypos()
        if act == 'shoot n':
            y = y + 1
            while y <= self.w.get_mapH():
                if self.w.is_it_wumpus(x,y):
                    wumpusKilled = True
                y = y + 1
        elif act == 'shoot e':
            x = x + 1
            while x <= self.w.get_mapW():
                if self.w.is_it_wumpus(x,y):
                    wumpusKilled = True
                x = x + 1
        elif act == 'shoot s':
            y = y - 1
            while y >= 1:
                if self.w.is_it_wumpus(x,y):
                    wumpusKilled = True
                y = y - 1
        elif act == 'shoot w':
            x = x - 1
            while x >= 1:
                if self.w.is_it_wumpus(x,y):
                    wumpusKilled = True
                x = x - 1
        else:
            #raise RuntimeError('shoot_rev: Wrong direction.')
            return self.send_msg('illegal')
        """Check whether the arrow hit the wumpus:"""
        if wumpusKilled:
            self.w.wumpus = ()
            self.w.stenchList.clear()
            return self.send_msg('scream')
        else:
            return self.send_msg('nothing')
            
    def send_msg(self, msg):
        if self.DEBUG:
            print(msg)
        else:
            return msg
    def print_result(self):
        log = open("log.txt","a")
        self.log_and_stdout(log,("Final Score: " + str(self.score)) + "\n")
        self.log_and_stdout(log,("Total Gold Number: "
                            + str(self.originGoldNumber) + "\n"))
        self.log_and_stdout(log,("Remain Gold Number: "
                            + str(self.goldNumber) + "\n"))
        self.log_and_stdout(log,("Remain Arrow Number: "
                            + str(self.w.arrow) + "\n"))
        self.log_and_stdout(log,("Total Moves: "
                            + str(self.moves) + "\n"))
            
    def log_and_stdout(self,log,string):
        print(string, end = '')
        log.write(string)
    """END OF CLASS REFEREE"""

if __name__ == '__main__':
    r = Referee()
    a = agent.Agent(r)
    print('score1: ' + str(r.get_score()))
    print('score2: ' + str(r.add_score(1)))
    print('score3: ' + str(r.deduct_score(1)))
    print('goldNumber: ' + str(r.get_goldNumber()))
    print('de goldNumber: ' + str(r.decrease_goldNumber()))
    print('mapW: ' + str(r.get_mapW()))
    print('mapH: ' + str(r.get_mapH()))
    print('xpos: ' + str(r.get_agent_xpos()))
    print('ypos: ' + str(r.get_agent_ypos()))
    print('goal: ' + str(r.get_goal()))
    print('stench? ' + str(r.percept_stench()))
    print('breeze? ' + str(r.percept_breeze()))
    print('glitter? ' + str(r.percept_glitter()))
    r.register_agent(a)
    print(r.agent)

    """Permitted agent actions:
   1.'north'   : Agent moving north. y+1
   2.'east'    : Agent moving east.  x+1
   3.'south'   : Agent moving south. y-1
   4.'west'    : Agent moving west.  x-1
   5.'pick'    : Agent picking gold.
   6.'shoot n' : Agent shooting the arrow to the north.
   7.'shoot e' : Agent shooting the arrow to the east.
   8.'shoot s' : Agent shooting the arrow to the south.
   9.'shoot w' : Agent shooting the arrow to the west.
   """
    r.action_rev('north')
    r.action_rev('east')
    r.action_rev('south')
    r.action_rev('west')
    r.action_rev('pick')
    r.action_rev('shoot n')
    r.action_rev('shoot e')
    r.action_rev('shoot s')
    r.action_rev('shoot w')
    print(str(r.get_agent_xpos()) + ',' + str(r.get_agent_ypos()))
    r.action_rev('west')
    print(str(r.get_agent_xpos()) + ',' + str(r.get_agent_ypos()))
    r.action_rev('east')
    print(str(r.get_agent_xpos()) + ',' + str(r.get_agent_ypos()))
    
    
    
