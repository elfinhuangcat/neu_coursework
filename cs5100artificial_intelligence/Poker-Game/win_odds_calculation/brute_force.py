#!/usr/bin/env python
"""brute_force.py:
   This file provides the brute-force method to calculate the win odds."""
import CT
import pokerlib

"""cal_win_odds_bf(hole, community):
   Given:
   (1) hole: An array storing two cards.
             Each card is a string representing the card.
             eg. 'Qd' where Q is the rank, d is the suit(Diamonds).
                 'Ts' where T(10) is the rank, s is the suit(Spades).
                 '2h' where 2 is the rank, h is the suit(Hearts).
   (2) community: An array storing the dealt community cards. The length of
                  this array is the number of dealt community cards.
                  The length must be one of 3, 4, 5.
                  Each card is a string. Refer to "hole".
   Returns: The win odds given the cards you know.
            Float number.
   """
def cal_win_odds_bf(hole, community):
    """Now we don't check the len of hole and community.!!"""

    """1. Translate the known cards into integers."""
    hole[0] = CT.trans_string_to_int(hole[0])
    hole[1] = CT.trans_string_to_int(hole[1])
    for i in range(0,len(community)):
        community[i] = CT.trans_string_to_int(community[i])

    """2. Call the corresponding function to calculate."""
    if len(community) == 5:
        return cal_win_odds_bf_river(hole, community)
    elif len(community) == 4:
        return cal_win_odds_bf_turn(hole, community)
    else:
        return cal_win_odds_bf_flop(hole, community)

"""cal_win_odds_bf_flop(hole,community):
   This function will be called by the cal_win_odds_bf() method.
   Given: hole: an array with two integers.
          community: an array with three integers.
   Returns: Using brute-force method, look through all the possibilities,
            return the win odds of current state.
            """
def cal_win_odds_bf_flop(hole, community):
    """ The uncertainty are:
        1. Opponent's hole cards.
        2. Turn and River."""
    """1. Create a deck without known cards:"""
    deck = deck_without_known_cards(hole + community)
    
    sumOfComparison = 0
    sumOfWin = 0
    sampleCards = [0,0,0,0] # 4 cards of uncertainty

    """2. Look through all possiblities."""
    for a in range(0,44):
        sampleCards[0] = deck[a]
        for b in range(a+1,45):
            sampleCards[1] = deck[b]
            for c in range(b+1,46):
                sampleCards[2] = deck[c]
                for d in range(c+1,47):
                    sampleCards[3] = deck[d]
                    """There are C(2,4) possible combinations of
                       opponent's hole cards + (turn + river)"""
                    for comb in combination4:
                        sumOfComparison = sumOfComparison + 1
                        myValue = pokerlib.eval_7hand(hole + community +
                                                      [sampleCards[comb[2]],
                                                       sampleCards[comb[3]]])
                        oppoValue = pokerlib.eval_7hand([sampleCards[comb[0]],
                                                         sampleCards[comb[1]]]
                                                        + community +
                                                        [sampleCards[comb[2]],
                                                         sampleCards[comb[3]]])
                        if myValue < oppoValue:
                            sumOfWin = sumOfWin + 1
    return float(sumOfWin)/float(sumOfComparison)

def cal_win_odds_bf_turn(hole, community):
    """ The uncertainty are:
        1. Opponent's hole cards.
        2. River."""
    """1. Create a deck without known cards:"""
    deck = deck_without_known_cards(hole + community)
    
    # sumOfComparison = 0 //actually = 45540
    sumOfWin = 0
    sampleCards = [0,0,0]
    """2. Look through all possiblities."""
    for a in range(0,44):
        sampleCards[0] = deck[a]
        for b in range(a+1,45):
            sampleCards[1] = deck[b]
            for c in range(b+1,46):
                sampleCards[2] = deck[c]
                """There are C(2,3) possible combinations of
                   opponent's hole cards + river"""
                for comb in combination3:
                    myValue = pokerlib.eval_7hand(hole + community
                                                  + [sampleCards[comb[2]]])
                    oppoValue = pokerlib.eval_7hand([sampleCards[comb[0]],
                                                     sampleCards[comb[1]]]
                                                    + community +
                                                    [sampleCards[comb[2]]])
                    if myValue < oppoValue:
                        sumOfWin = sumOfWin + 1
    return float(sumOfWin)/float(45540)

def cal_win_odds_bf_river(hole, community):
    """ The uncertainty are:
        Opponent's hole cards."""
    """1. Create a deck without known cards:"""
    deck = deck_without_known_cards(hole + community)
    
    #sumOfComparison = 0  // actually = 990
    sumOfWin = 0
    """2. Look through all the possibilities."""
    for a in range(0,44):
        for b in range(a+1,45):
            myValue = pokerlib.eval_7hand(hole + community)
            oppoValue = pokerlib.eval_7hand([deck[a],deck[b]] + community)
            if myValue < oppoValue:
                sumOfWin = sumOfWin + 1
    return float(sumOfWin)/float(990)

combination4 = [(0,1,2,3),
                (0,2,1,3),
                (0,3,1,2),
                (1,2,0,3),
                (1,3,0,2),
                (2,3,0,1)]
combination3 = [(0,1,2),
                (0,2,1),
                (1,2,0)]

def deck_without_known_cards(knownCards):
    deck = [0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0]
    pokerlib.init_deck(deck)
    """2.1 Delete known cards:"""
    i = 0
    lenDeck = len(deck)
    while i < lenDeck:
        if deck[i] in knownCards:
            deck.pop(i)
            lenDeck = lenDeck - 1
        else:
            i = i + 1
    return deck
                        

if __name__ == '__main__':
    import time
    """*********************************
    Examples: Flop Stage:
    *********************************"""
    """
    print('time: ' + str(time.time()))
    print('3 com: '+ str(cal_win_odds_bf(['Ts','Th'],
                                         ['5d','Jc','Qc'])))
    """

    """*********************************
    Examples: Turn Stage:
    *********************************"""
    """
    print('time: ' + str(time.time()))
    print('4 com: '+ str(cal_win_odds_bf(['Ts','Th'],
                                         ['5d','Jc','Qc','9d'])))
    """

    """*********************************
    Examples: River Stage:
    *********************************"""

    print('time: ' + str(time.time()))
    print('5 com ex 1: '
          + str(cal_win_odds_bf(['Ah','Ac'],
                                ['6c','7c','8c','9c','Tc'])))
    print('time: ' + str(time.time()))
    print('5 com ex 2: '
          + str(cal_win_odds_bf(['9h','Qh'],
                                ['7s','Tc','4h','5h','2c'])))
    print('time: ' + str(time.time()))
    print('5 com ex 3: '
          + str(cal_win_odds_bf(['9s','Ah'],
                                ['8c','8s','Jd','9c','Td'])))
    print('time: ' + str(time.time()))



""" Test result : Turn stage ( about 10s ):
time: 1396903364.561571
turn - sum of comparison = 45540
4 com: 0.6987263943785683
"""

""" Test result: River stage (about half a second):
time: 1396903570.620357
river - sum of comparison = 990
5 com ex 1: 0.0
time: 1396903571.094384
river - sum of comparison = 990
5 com ex 2: 0.1494949494949495
time: 1396903571.459405
river - sum of comparison = 990
5 com ex 3: 0.3939393939393939
time: 1396903572.077441
"""
    
    
