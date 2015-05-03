#!/usr/bin/env python
"""agent.py:
   This file defines the class Agent that is used to create an agent.
   """

import world_structure
import referee
import agent_sensor
import shlex, subprocess
from subprocess import PIPE, STDOUT, Popen

class Agent:
    def __init__(self,r):
        """w: The agent holds this structure to maintain its knowledge
              about the world."""
        self.DEBUG = False
        self.w = world_structure.World()
        self.sensor = agent_sensor.Sensor()
        self.score = 0
        
        """Subscribe to the referee."""
        self.referee = r
        self.referee.register_agent(self)
        self.__init_world()
        """rules: A string represents the rule of this world.
                  In FOL."""
        self.rule = '' #Store rules
        self.fact = '' #Store facts
        self.__init_rule_generate()
        
        self.prover9Path = ''
        self.__init_prover9()
        
        self.unexploredList = list() #To be explored list
        self.exploredSet = set()  #Explored squares list
        self.safeSet = set() #Deduced to be safe
        """Create the logfile to record all activities of the agent."""
        self.log = open('log.txt','a')
        
        self.picked_gold_set = set()
        self.wumpusDied = False
        
        
    def __init_world(self):
        """The agent is allowed to read Mxy, Axy, GOxy."""
        self.w.set_map(self.referee.get_mapW(),self.referee.get_mapH())
        self.w.set_agent_pos(self.referee.get_agent_xpos(),
                             self.referee.get_agent_ypos())
        goal = self.referee.get_goal()
        self.w.set_goal(goal[0],goal[1])

    """__init_rule_generate() is responsible for reading the
       init_rule.txt and creating the adjacency rule according
       to the size of the map."""
    def __init_rule_generate(self):
        path = ('init_rule.txt')
        ruleFile = open(path, 'r')
        self.rule = ruleFile.read() + '\n'
        mapW = self.w.get_mapW()
        mapH = self.w.get_mapH()
        adjRule = ''
        i = 1
        j = 1
        for i in range(1,(mapW+1)):
            for j in range(1,(mapH+1)):
                if j == mapH and i < mapW:
                    """Don't need to add adj([i,j],[i,j+1])"""
                    adjRule = (adjRule + 'Adjacent([' + str(i) +
                               ',' + str(j) + '],[' + str(i+1) +
                               ',' + str(j) + ']).\n')
                elif i == mapW and j < mapH:
                    """Don't need to add adj([i,j],[i+1,j])"""
                    adjRule = (adjRule + 'Adjacent([' + str(i) +
                               ',' + str(j) + '],[' + str(i) +
                               ',' + str(j+1) + ']).\n')
                elif i == mapW and j == mapH:
                    continue
                else:
                    adjRule = (adjRule + 'Adjacent([' + str(i) +
                               ',' + str(j) + '],[' + str(i) +
                               ',' + str(j+1) + ']).\n' +
                               'Adjacent([' + str(i) + ',' + str(j) +
                               '],[' + str(i+1) + ',' + str(j) +
                               ']).\n')
        self.rule = self.rule + adjRule


    def __init_prover9(self):
        pathfile = open("prover9path.txt","r")
        self.prover9Path = pathfile.read()

    def log_and_stdout(self, string):
        print(string, end = '')
        self.log.write(string)

    def start_exploration(self):
        """This method is called by the main function to start
           this agent's exploration."""
        x = self.w.get_agent_xpos()
        y = self.w.get_agent_ypos()
        self.safeSet = self.safeSet | {(x,y)}
        self.fact += ("Safe([" + str(x) + "," + str(y) + "]).\n")

        self.log_and_stdout("******************Start the game"
                            + "******************\n"
                            + "Now the agent is exploring for gold.\n")
        """mode: The mode of the agent's action.
                 True: Search for gold.
                 False: Search for goal."""
        mode = True
        
        """****MODE 1: SEARCH FOR GOLD****"""
        while mode:
            #Remember to update x and y
            self.log_and_stdout("At (" + str(x) + "," + str(y) + ").\n")
            """1.Perceive and enlarge the knowledge base: """
            self.add_perception_facts(self.sensor.percept_stench(self.referee),
                                      self.sensor.percept_breeze(self.referee),
                                      self.sensor.percept_glitter(self.referee))
            """2.If there is a piece of gold, pick it up:"""
            self.action_pick_gold()
                    
            """3.Use prover9 to do inference:"""
            neighbours = self.cal_neighbours(x,y)
            if self.DEBUG: #DEBUG
                print("neighbours: " + str(neighbours))
                print("unexploredList: " + str(self.unexploredList))
            for square in neighbours:
                if (square not in self.exploredSet and
                    square not in self.unexploredList):
                    if self.DEBUG: #DEBUG
                        print("Add neighbour to unexlist:" + str(square))
                    self.unexploredList.append(square)
            self.inference(self.unexploredList)

            """4.choose an adjacent square to move on:"""
            if len(self.unexploredList) == 0:
                """The list is initially empty, so the exploration
                   for gold should end."""
                mode = False
                break
            else:      
                i = len(self.unexploredList)-1
                while i >= 0: 
                    square = self.unexploredList[i]
                    if square in self.safeSet:
                        if self.DEBUG:
                            print("Found safe square to move: " + str(square))
                        break
                    else:
                        if (self.w.is_it_wumpus(square[0],square[1])
                            or self.w.is_it_pit(square[0],square[1])):
                            self.unexploredList.remove(square)
                    i = i - 1
                    
                if i == -1:
                    """There is no safe square in the unexploredList:
                       We should search for the wumpus and kill it in
                       order to open a new path."""
                    if self.w.wumpus == () and not self.wumpusDied:
                        wumpusFound = False
                        #Here is a little tricky, try to find the wumpus
                        #by the agent itself (instead of asking prover9)
                        for s in self.safeSet:
                            neighbours = self.cal_neighbours(s[0],s[1])
                            stenchSet = set()
                            for nsquare in neighbours:
                                if nsquare in self.w.stenchList:
                                    """This does not violate the rule:
                                       Only after the agent has been to
                                       this square, the fact "stench" will
                                       be added to self.w.stenchList."""
                                    stenchSet = stenchSet | {nsquare}
                            if len(stenchSet) == 2:
                                """Then the wumpus is on the opposite side."""
                                wumpusFound = True
                                wu = self.cal_wumpus_pos(s,stenchSet)
                                self.log_and_stdout("Wumpus found: "
                                                    + str(wu) + "\n")
                                """To see if it is necessary
                                   to kill the wumpus:"""
                                neis = self.cal_neighbours(wu[0],wu[1])
                                for n in neis:
                                    if ((n not in self.exploredSet) and
                                        (n not in self.unexploredList)):
                                        t = self.move_kill_w(wu[0],wu[1])
                                        temp_square = t
                                        square = t
                                        self.wumpusDied = True
                                        if wu not in self.unexploredList:
                                            self.unexploredList.append(wu)
                                        for n in neis:
                                            if n not in self.unexploredList:
                                                self.unexploredList.append(n)
                                        break
                                if not self.wumpusDied:
                                    """Didn't find a square to kill the w:"""
                                    mode = False
                                    square = self.w.get_agent_pos()
                                    temp_square = square
                                    break
                        if not wumpusFound:        
                            """There is no way to find the wumpus. Quit."""
                            print("Search for gold ends.")
                            self.log.write("Search for gold ends.\n")
                            mode = False #Quit mode 1
                            break
                    elif not self.wumpusDied:
                        wx = self.w.wumpus[0]
                        wy = self.w.wumpus[1]
                        temp_square = self.move_kill_w(wx,wy)
                        self.delete_wumpus_facts()
                    else:
                        mode = False
                        break
                else:
                    """There is an safe square, so there are two situation:
                       1.The square we found is next to the position now.
                       2.The square is not adjacent to the position now."""
                    if self.adjacent((x,y),square):
                        """The agent can go there directly."""
                        path = list()
                        path.append(square)
                        temp_square = self.go_to((x,y),square,path)
                    else:
                        temp_square = self.print_short_safe_path(square)
                """A move is made, check whether it is correct:"""
                if temp_square != square:
                    self.log_and_stdout("exploration Error: "
                                        + "didn't get to the "
                                        + "destination.\n")
                    return False #Any error will cause the exploration stop.
                else:
                    x = square[0]
                    y = square[1]
                    self.w.set_agent_pos(x,y)
                    if square in self.unexploredList:
                        self.unexploredList.remove(square)
                        self.exploredSet = self.exploredSet | {square}
        mode = False
        square = self.print_short_safe_path(self.w.get_goal())
        self.log.close()

    def cal_wumpus_pos(self, square, stenchSet):
        x = square[0]
        y = square[1]
        n1 = stenchSet.pop()
        n2 = stenchSet.pop()
        if ((self.north_to(n1,square) and self.west_to(n2,square)) or
            (self.north_to(n2,square) and self.west_to(n1,square))):
            return (x-1,y+1)
        elif ((self.north_to(n1,square) and self.east_to(n2,square)) or
              (self.north_to(n2,square) and self.east_to(n1,square))):
            return (x+1,y+1)
        elif ((self.south_to(n1,square) and self.west_to(n2,square)) or
              (self.south_to(n2,square) and self.west_to(n1,square))):
            return (x-1,y-1)
        elif ((self.south_to(n1,square) and self.east_to(n2,square)) or
              (self.south_to(n2,square) and self.east_to(n1,square))):
            return (x+1,y-1)

    def north_to(self, s1, s2):
        if s1[1] > s2[1]:
            return True
        else:
            return False
    def south_to(self, s1, s2):
        if s1[1] < s2[1]:
            return True
        else:
            return False
    def east_to(self, s1, s2):
        if s1[0] > s2[0]:
            return True
        else:
            return False
    def west_to(self, s1, s2):
        if s1[0] < s2[0]:
            return True
        else:
            return False
        
    def delete_wumpus_facts(self,wx,wy):
        self.w.stenchList.clear()
        self.w.wumpus = ()
        self.wumpusDied = True
        """Delete useless facts in self.fact:"""
        neighbours = self.cal_neighbours(wx,wy)
        for n in neighbours:
            string = "S([" + str(n[0]) + "," + str(n[1]) + "]).\n"
            if string in self.fact:
                self.fact = self.fact.replace(string,'')
        

    """move_kill_w(wx,wy):
       Find a safe and reachable square, at which the agent can use the
       arrow to kill the wumpus (The arrow goes in a straight line.
       If no such square exists, then RETURN FALSE."""
    def move_kill_w(self, wx, wy):
        """1.Find a safe square among the self.safeSet:"""
        for square in self.safeSet:
            if square[0] == wx or square[1] == wy:
                break
        if not(square[0] == wx or square[1] == wy):
            return False
        temp_square = self.print_short_safe_path(square)
        if temp_square != square:
            print("kill_wumpus Error: Did not get to the destination.")
            self.log.write("kill_wumpus Error: Did not get "
                           + "to the destination.\n")
            return False
        else:
            msg = None
            if square[0] == wx:
                if square[1] > wy:
                    msg = self.referee.action_rev('shoot s')
                    print("Shoot towards south.")
                    self.log.write("Shoot towards south.\n")
                else:
                    msg = self.referee.action_rev('shoot n')
                    print("Shoot towards north.")
                    self.log.write("Shoot towards north.\n")
            elif square[1] == wy:
                if square[0] > wx:
                    msg = self.referee.action_rev('shoot w')
                    print("Shoot towards west.")
                    self.log.write("Shoot towards west.\n")
                else:
                    msg = self.referee.action_rev('shoot e')
                    print("Shoot towards east.")
                    self.log.write("Shoot towards east.\n")
            else:
                print("kill_wumpus Error: Cannot kill the wumpus.")
                self.log.write("kill_wumpus Error: Cannot "
                               + "kill the wumpus.\n")
            """To see the result of shooting:"""
            if msg == 'scream':
                print("Wumpus killed.")
                self.log.write("Wumpus killed.\n")
                self.delete_wumpus_facts(wx,wy)
                if square not in self.unexploredList:
                    self.unexploredList.append(square)
                return temp_square
            elif msg == 'nothing':
                print("Arrow used. Killed nothing.")
                self.log.write("Arrow used. Killed nothing.\n")
                return temp_square
            else:
                print("kill_wumpus Error: Wrong protocol.")
                self.log.write("kill_wumpus Error: Wrong protocol.\n")
                return False
        
            
    """print_short_safe_path(square):
       Destination: square
       Source: The position now (stored in self.w)
       Dijkstra.
       Find the shortest safe path to the destination.
       Finally go to the destination step by step, each step is an actual
       action, which will need communication with the referee."""
    def print_short_safe_path(self, terminal):
        x = self.w.get_agent_xpos()
        y = self.w.get_agent_ypos()
        result = self.dijkstra(x,y)
        INFINITY = 9999 
        if result.dist[terminal] == INFINITY:
            return False
        else:
            """The destination is reachable:"""
            path = list()
            u = terminal
            path.append(u)
            while result.dist[u] != 0:
                u = result.previous[u]
                path.append(u)
            """Now the agent moves step by step according to the path:"""
            path.pop()
            square = self.go_to((x,y),terminal,path)
            if square != False:
                self.w.set_agent_pos(square[0],square[1])
                if square in self.unexploredList:
                    self.unexploredList.remove(square)
                self.exploredSet = self.exploredSet | {square}
            return square

    """go_to(source,terminal,path):
       Given the path, make the agent go to the terminal from the
       source.
       Returns nothing."""
    """The path should not contain the source but should contain the
       terminal. The path starts at the last element of the list."""
    """Example:
       Source = (1,1)  Terminal = (3,3)
       Path = [(3,3),(3,2),(3,1),(2,1)]
       """
    def go_to(self,source,terminal,path):
        x = source[0]
        y = source[1]
        while len(path) > 0:
            square = path.pop() #Remove the last element
            msg = 'None'
            if x == square[0]:
                if y > square[1]:
                    msg = self.referee.action_rev('south')
                else:
                    msg = self.referee.action_rev('north')
            else:
                if x > square[0]:
                    msg = self.referee.action_rev('west')
                else:
                    msg = self.referee.action_rev('east')
            if msg == 'None':
                self.log_and_stdout("go_to Error: "
                                    + "The action has no result.\n")
                return False
            elif msg == 'safe': #Only this branch should happen
                x = square[0]
                y = square[1]
                self.w.set_agent_pos(x,y)
                self.log_and_stdout("Move to (" + str(x)
                                    + "," + str(y) + ").\n")
                if square in self.unexploredList:
                    """Now you have visited this square:"""
                    self.unexploredList.remove(square)
                    self.exploredSet = self.exploredSet | {square}
                    self.add_perception_facts(self.sensor.percept_stench
                                              (self.referee),
                                              self.sensor.percept_breeze
                                              (self.referee),
                                              self.sensor.percept_glitter
                                              (self.referee))
                    """Add your neighbours in the unexploredList:"""
                    neighbours = self.cal_neighbours(x,y)
                    for square in neighbours:
                        if (square not in self.unexploredList and
                            square not in self.exploredSet):
                            self.unexploredList.append(square)
                    self.inference(self.unexploredList)
            elif msg == 'pit':
                x = square[0]
                y = square[1]
                self.w.set_agent_pos(x,y)
                self.log_and_stdout("Move to (" + str(x) + "," + str(y)
                                    + ").\n" + "And I died...pit\n")
                return False
            elif msg == 'wumpus':
                x = square[0]
                y = square[1]
                self.w.set_agent_pos(x,y)
                self.log_and_stdout("Move to (" + str(x) + "," + str(y)
                                    + ").\n" + "And I died...wumpus\n")
                return False
            elif msg == 'illegal':
                self.log_and_stdout("go_to: Illegal move made.\n")
                return False
            else:
                self.log_and_stdout("go_to: Something wrong with "
                                    + "the protocol.\n")
                return False
        return (x,y)
        """Return the position that the agent is standing at."""

    """Given the x,y of the source, return its distance to all the
       safe square(now) and the precedent graph."""
    def dijkstra(self,x,y):
        """x,y are the coordinates of the source."""
        result = DijkstraGraph()
        INFINITY = 9999 #Bad design, fix it
        for square in self.safeSet:
            result.dist[square] = INFINITY
            result.previous[square] = None
        result.dist[(x,y)] = 0
        Q = self.safeSet.copy()
        while len(Q) > 0:
            """Find a quare with smallest distance."""
            u = None
            for square in Q:
                if u == None:
                    u = square
                    continue
                elif result.dist[u] > result.dist[square]:
                        u = square
            """Now u is the square in Q with smallest distance."""
            Q.remove(u)
            if result.dist[u] == INFINITY:
                """No where else can go."""
                break
            neighbours = self.cal_neighbours(u[0],u[1])
            neighboursCopy = neighbours.copy()
            for square in neighbours:
                if square not in self.safeSet:
                    neighboursCopy.remove(square)
            for n in neighboursCopy:
                alt = result.dist[u] + 1
                if alt < result.dist[n]:
                    result.dist[n] = alt
                    result.previous[n] = u
        """The distance to all the safe squares have been calculated."""
        return result        
                
    def adjacent(self, square1, square2):
        if ((abs(square1[0]-square2[0]) == 1 and square1[1] == square2[1]) or
            (abs(square1[1]-square2[1]) == 1 and square1[0] == square2[0])):
            return True
        else:
            return False
        
            
    """self.inference(unexList):
       Input : unexList -- the list of all squares that the agent has not
                           been to.
       Function: It will first leave out all the squares in unexList that
                 are considered safe. Then it do inference on the remaining
                 squares, according to self.rule and self.fact, to see:
                 (1) Whether this square is safe.
                     If safe, add it to self.safeSet
                 (2) If this square is not safe:
                     -- Whether this square IS A WUMPUS
                        If it is, add this fact to self.w
       Returns: nothing
       """
    def inference(self, unexList):
        #Avoid revising the original unexList
        temp = unexList.copy()
        for square in temp:
            if square in self.safeSet:
                temp.remove(square)                
        """Inference begin:"""
        for square in temp:
            """1.Whether this square is safe."""
            goalSafe = ('Safe([' + str(square[0]) + ','
                        + str(square[1]) + ']).\n')
            resultSafe = self.prover_infer(goalSafe)
            if resultSafe:
                self.safeSet = self.safeSet | {square}
                if ((square not in self.unexploredList) and
                    (square not in self.exploredSet)):
                    self.unexploredList.append(square)
                self.log.write("Infer result: (" + str(square[0])
                               + "," + str(square[1]) + ") is safe.\n")
            else:
                """It is not safe: Whether this square IS A WUMPUS"""
                goalW = ('W([' + str(square[0]) + ','
                         + str(square[1]) + ']).\n')
                resultW = self.prover_infer(goalW)
                if resultW:
                    self.log.write("Infer result: (" + str(square[0])
                                    + "," + str(square[1]) + ") is a wumpus.\n")
                    self.w.set_wumpus(square[0],square[1])

    """add_perception_facts() is respobsible to update the w (World)
       and adding new facts to the rule."""
    """stench, breeze, glitter are all boolean values to represent
       whether there is such a thing.
       P.S. Use the self.w.pos as the agent's position !!!
       Return : nothing."""
    def add_perception_facts(self,stench,breeze,glitter):
        x = self.w.get_agent_xpos()
        y = self.w.get_agent_ypos()
        if stench:
            if (x,y) not in self.w.stenchList:
                self.w.add_stench(x,y)
                self.fact +=('S([' + str(x) + ',' + str(y) + ']).\n')
                self.log_and_stdout("Stench in (" + str(x)
                                    + "," + str(y) + ").\n")
        else:
            if str('-S([' + str(x) + ',' + str(y) + ']).\n') not in self.fact:
                self.fact += ('-S([' + str(x) + ',' + str(y) + ']).\n')
        if breeze:
            if (x,y) not in self.w.breezeList:
                self.w.add_breeze(x,y)
                self.fact += ('B([' + str(x) + ',' + str(y) + ']).\n')
                self.log_and_stdout("Breeze in (" + str(x)
                                    + "," + str(y) + ").\n")
        else:
            if str('-B([' + str(x) + ',' + str(y) + ']).\n') not in self.fact:
                self.fact += ('-B([' + str(x) + ',' + str(y) + ']).\n')
        if glitter:
            if ((not self.w.is_it_gold(x,y)) and
                (x,y) not in self.picked_gold_set):
                self.w.add_gold(x,y)
                self.log_and_stdout("Glitter in (" + str(x)
                                    + "," + str(y) + ").\n")

    """action_pick_gold():
       At each new square, the agent should call this method to
       check if it can pick gold."""
    def action_pick_gold(self):
        gx = self.w.get_agent_xpos()
        gy = self.w.get_agent_ypos()
        if self.w.is_it_gold(gx,gy):
            msg = self.referee.action_rev('pick')
            if msg == 'gold':
                self.w.delete_gold(gx,gy)
                self.picked_gold_set = self.picked_gold_set | {(gx,gy)}
                self.log_and_stdout("Gold picked up.\n")
            elif self.DEBUG:
                self.log_and_stdout("Warning: The agent send pick action but"
                                    + " there is no gold!!\n")

    """prover_infer(goal):
       Combine self.rule and self.fact and use them to prove the goal.
       If the goal is proved, return true, else return false.
       This method will change nothing. Everything is done locally."""
    def prover_infer(self,goal):
        p = Popen(self.prover9Path, stdout=PIPE, stdin=PIPE, stderr=PIPE)
        args = ("formulas(assumptions).\n" + self.rule + self.fact
                + "end_of_list.\n formulas(goals).\n"
                + goal + "end_of_list.\n")
        p.stdin.write(args.encode('utf-8'))
        result = p.communicate()[0].decode('utf-8')
        if "THEOREM PROVED" in result:
            return True
        else:
            return False

    """cal_neighbours(x,y):
       Given the x,y coords of a position, calculate the neighbours
       and return them.
       Returns: a set of neighbours. Each neighbour is in the form
                of (x,y)
    """
    def cal_neighbours(self, x, y):
        neighbours = set()
        if x > 1:
            neighbours = neighbours | {(x-1,y)}
        if x < self.w.get_mapW():
            neighbours = neighbours | {(x+1,y)}
        if y > 1:
            neighbours = neighbours | {(x,y-1)}
        if y < self.w.get_mapH():
            neighbours = neighbours | {(x,y+1)}
        return neighbours
        
    """END OF CLASS AGENT"""

class DijkstraGraph:
    def __init__(self):
        self.dist = dict()
        self.previous = dict()
        
        
            
if __name__ == '__main__':
    r = referee.Referee()
    a = Agent(r)
    print("At " + str(a.w.get_agent_pos()))
    print("(1,1)'s neighbour:"+str(a.cal_neighbours(1,1)))
    print("(4,1)'s neighbour:"+str(a.cal_neighbours(4,1)))
    print("(1,4)'s neighbour:"+str(a.cal_neighbours(1,4)))
    print("(4,4)'s neighbour:"+str(a.cal_neighbours(4,4)))
    print("(1,2)'s neighbour:"+str(a.cal_neighbours(1,2)))
    print("(2,1)'s neighbour:"+str(a.cal_neighbours(2,1)))
    print("(3,3)'s neighbour:"+str(a.cal_neighbours(3,3)))
    a.add_perception_facts(False,False,False)
    print("safe (1,2)? " + str(a.prover_infer("Safe([1,2]).\n")))
    print("safe (2,1)? " + str(a.prover_infer("Safe([2,1]).\n")))
    print("safe (1,3)? " + str(a.prover_infer("Safe([1,3]).\n")))
    a.safeSet = a.safeSet | {(1,1)}
    print("safe set before inference: " + str(a.safeSet))
    unexList = list()
    square1 = (1,2)
    unexList.append(square1)
    square2 = (2,1)
    unexList.append(square2)
    print("unexList before inference: " + str(unexList))
    a.inference(unexList)
    print("safe set after inference: " + str(a.safeSet))
    print("unexList after inference: " + str(unexList))
    
    a.add_perception_facts(False,True,False)
    print('Add facts: B([2,1]).  -S([2,1]).\n')
    unexList.remove(square2)
    print("unexList before inference: " + str(unexList))
    a.inference(unexList)
    print("safe set after inference: " + str(a.safeSet))
    print("unexList after inference: " + str(unexList))

    r2 = referee.Referee()
    a2 = Agent(r2)

