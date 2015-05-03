The evaluator's algorithm is translated from:
http://www.suffecool.net/poker/evaluator.html
which is originally a C source code.

Among all the python files,
CT.py contains the card translator created by myself.
main.py contains the examples of how to use the evaluator.
        (5,6,7-card hands)
arrays.py contains look-up tables.
poker_definitions.py contains definitions of some consts.
pokerlib.py contains the major algorithms.
            I added 6-card hand evaluator, in case the agent needs it.