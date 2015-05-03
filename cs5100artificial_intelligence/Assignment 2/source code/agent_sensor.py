#!/usr/bin/env python
"""agent_sensor.py:
   This file defines the class Sensor which is used by the agent. The
   sensor will directly communicate with the referee to percept the
   world.(The referee maintains the world.)
   """
import referee
class Sensor:
    """Here lists all the things that an agent can sense.
       r is the referee.
       """
    """1. Perceive stench: """
    def percept_stench(self, r):
        return r.percept_stench()
    """2. Perceive breeze: """
    def percept_breeze(self, r):
        return r.percept_breeze()
    """3. Perceive glitter: """
    def percept_glitter(self, r):
        return r.percept_glitter()

if __name__ == '__main__':
    s = Sensor()
    r = referee.Referee()
    print('xpos: ' + str(s.get_agent_xpos(r)))
    print('ypos: ' + str(s.get_agent_ypos(r)))
    print('stench? ' + str(s.percept_stench(r)))
    print('breeze? ' + str(s.percept_breeze(r)))
    print('glitter? ' + str(s.percept_glitter(r)))

