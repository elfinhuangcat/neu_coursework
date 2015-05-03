#!/usr/bin/env python
"""main.py:
   """
import agent
import referee
import world_structure

def start_the_world():
    """Create the referee for this world. The referee will be
       responsible for maintaining the world and supervising
       the actions of the agent."""
    r = referee.Referee()
    """Create the agent. The agent will be associated with
       the referee."""
    a = agent.Agent(r)
    a.start_exploration()
    r.print_result()


if __name__ == '__main__':
    start_the_world()
    


