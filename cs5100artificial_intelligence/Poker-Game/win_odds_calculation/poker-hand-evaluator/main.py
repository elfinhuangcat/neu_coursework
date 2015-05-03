#!/usr/bin/env python
"""main.py:
   This file is created to test the evaluator."""
import pokerlib
import poker_definitions
import arrays
import CT
def main_hand5():
    deck = [0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0]
    hand = [0,0,0,0,0]
    freq = [0,0,0,0,0,0,0,0,0,0]

    """ initialize the deck"""
    pokerlib.init_deck(deck)
    print(str(deck))
    """loop over every possible five-card hand:"""
    for a in range(0,48):
        hand[0] = deck[a]
        for b in range(a+1,49):
            hand[1] = deck[b]
            for c in range(b+1,50):
                hand[2] = deck[c]
                for d in range(c+1,51):
                    hand[3] = deck[d]
                    for e in range(d+1,52):
                        hand[4] = deck[e]
                        i = pokerlib.eval_5hand(hand)
                        j = pokerlib.hand_rank(i)
                        freq[j] = freq[j] + 1
    for i in range(1,10):
        print(str(poker_definitions.value_str[i]) + " " + str(freq[i]))

def main_hand7():
    """Now begin to test the 7 cards hand eval:"""
    hand = [0,0,0,0,0,0,0]
    deck = [0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0]
    freq = [0,0,0,0,0,0,0,0,0,0]
    """ initialize the deck"""
    pokerlib.init_deck(deck)
    """loop over every possible 7-card hand:"""
    for a in range(0,46):
        hand[0] = deck[a]
        for b in range(a+1,47):
            hand[1] = deck[b]
            for c in range(b+1,48):
                hand[2] = deck[c]
                for d in range(c+1,49):
                    hand[3] = deck[d]
                    for e in range(d+1,50):
                        hand[4] = deck[e]
                        for f in range(e+1,51):
                            hand[5] = deck[f]
                            for g in range(f+1,52):
                                hand[6] = deck[g]
                                i = pokerlib.eval_7hand(hand)
                                j = pokerlib.hand_rank(i)
                                freq[j] = freq[j] + 1
    for i in range(1,10):
        print(str(poker_definitions.value_str[i]) + " " + str(freq[i]))

def hand7_small_example():
    hand = [0,0,0,0,0,0,0]
    """Exampe 1:"""
    hand[0] = CT.card_translate(0,'s') #2s
    hand[1] = CT.card_translate(1,'s') #3s
    hand[2] = CT.card_translate(3,'s') #5s
    hand[3] = CT.card_translate(9,'d') #Jd
    hand[4] = CT.card_translate(2,'s') #4s
    hand[5] = CT.card_translate(12,'c') #Ac
    hand[6] = CT.card_translate(12,'s') #As
    i = pokerlib.eval_7hand(hand)
    j = pokerlib.hand_rank(i)
    """Print the evaluation result of the best 5 cards out of 7."""
    print("ex1: "
          + "strength value: " + str(i) + " "
          + str(poker_definitions.value_str[j]))
    """Exampe 2:"""
    hand[0] = CT.card_translate(2,'c') #4c
    hand[1] = CT.card_translate(2,'s') #4s
    hand[2] = CT.card_translate(10,'h') #Qh
    hand[3] = CT.card_translate(9,'d') #Jd
    hand[4] = CT.card_translate(7,'s') #9s
    hand[5] = CT.card_translate(4,'c') #6c
    hand[6] = CT.card_translate(6,'s') #As
    i = pokerlib.eval_7hand(hand)
    j = pokerlib.hand_rank(i)
    """Print the evaluation result of the best 5 cards out of 7."""
    print("ex2: "
          + "strength value: " + str(i) + " "
          + str(poker_definitions.value_str[j]))

def hand5_small_example():
    hand = [0,0,0,0,0,0,0]
    hand[0] = CT.card_translate(0,'s') #2s
    hand[1] = CT.card_translate(1,'s') #3s
    hand[2] = CT.card_translate(3,'s') #5s
    hand[3] = CT.card_translate(2,'s') #4s
    hand[4] = CT.card_translate(12,'s') #As
    i = pokerlib.eval_5hand(hand)
    j = pokerlib.hand_rank(i)
    """Print the evaluation result of the best 5 cards out of 7."""
    print(str(poker_definitions.value_str[j]))

def hand6_small_example():
    hand = [0,0,0,0,0,0]
    hand[0] = CT.card_translate(0,'s') #2s
    hand[1] = CT.card_translate(1,'s') #3s
    hand[2] = CT.card_translate(3,'s') #5s
    hand[3] = CT.card_translate(9,'d') #Jd
    hand[4] = CT.card_translate(2,'s') #4s
    hand[5] = CT.card_translate(12,'c') #Ac
    i = pokerlib.eval_6hand(hand)
    j = pokerlib.hand_rank(i)
    """Print the evaluation result of the best 5 cards out of 7."""
    print("hand_6: "
          + "strength value: " + str(i) + " "
          + str(poker_definitions.value_str[j]))

def hand7_small_example_int():
    hand = [69634, 33564957, 73730, 81922, 268471337, 98306, 81922]
    i = pokerlib.eval_7hand(hand)
    j = pokerlib.hand_rank(i)
    """Print the evaluation result of the best 5 cards out of 7."""
    print(str(poker_definitions.value_str[j]))

if __name__ == '__main__':
    hand7_small_example_int()
    main_hand5()
