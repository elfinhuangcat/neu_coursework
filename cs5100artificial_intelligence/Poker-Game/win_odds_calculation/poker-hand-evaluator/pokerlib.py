#!/usr/bin/env python
import arrays
import poker_definitions
import random

"""perform a binary search on a pre-sorted array"""
def findit(key):
    low = 0
    high = 4887
    mid = 0
    while low <= high:
        mid = (high+low) >> 1 #Divided by two
        if key < arrays.products[mid]:
            high = mid - 1
        elif key > arrays.products[mid]:
            low = mid + 1
        else:
            return mid
    print("ERROR: no match found; key = " + str(key))
    return -1

"""  init_deck(deck):
//   This routine initializes the deck.  A deck of cards is
//   simply an integer array of length 52 (no jokers).  This
//   array is populated with each card, using the following
//   scheme:
//
//   An integer is made up of four bytes.  The high-order
//   bytes are used to hold the rank bit pattern, whereas
//   the low-order bytes hold the suit/rank/prime value
//   of the card.
//
//   +--------+--------+--------+--------+
//   |xxxbbbbb|bbbbbbbb|cdhsrrrr|xxpppppp|
//   +--------+--------+--------+--------+
//
//   p = prime number of rank (deuce=2,trey=3,four=5,five=7,...,ace=41)
//   r = rank of card (deuce=0,trey=1,four=2,five=3,...,ace=12)
//   cdhs = suit of card
//   b = bit turned on depending on rank of card
"""
def init_deck(deck):
    i = 0
    j = 0
    n = 0
    suit = 0x8000
    for i in range(0,4):
        for j in range(0,13):
            deck[n] = arrays.primes[j] | (j << 8) | suit | (1 << (16+j))
            n = n + 1
        suit >>= 1

"""init_deck_without_known_cards(knownCards):
   Given: An array of known cards, which are represented as an integer.
          Please use the translator provided in poker-hand-translator
          to get the integers.
   Returns: A deck without the known cards.
   """
def init_deck_without_known_cards(knownCards):
    i = 0
    j = 0
    n = 0
    suit = 0x8000
    deck = list()
    for i in range(0,4):
        for j in range(0,13):
            tempCard = arrays.primes[j] | (j << 8) | suit | (1 << (16+j))
            if tempCard not in knownCards:
                deck.append(tempCard)
            n = n + 1
        suit >>= 1
    return deck

""" int
    find_card( int rank, int suit, int *deck )
//  This routine will search a deck for a specific card
//  (specified by rank/suit), and return the INDEX giving
//  the position of the found card.  If it is not found,
//  then it returns -1"""
def find_card(rank, suit, deck):
    i = 0
    c = 0
    for i in range(0, 52):
        c = deck[i]
        if (c & suit) and (poker_definitions.RANK(c) == rank):
            return i
    return -1

"""
//  This routine takes a deck and randomly mixes up
//  the order of the cards."""
def shuffle_deck(deck):
    tempDeck = deck
    deck = list()
    while len(tempDeck) > 0:
        i = random.randint(0,(len(tempDeck)-1))
        deck.append(tempDeck.pop(i))
    return deck

"""hand is an array, n is the length of array"""
def print_hand(hand, n):
    i = 0
    r = 0
    suit = 'S'
    rank = "23456789TJQKA"
    for i in range(0, n):
        r = (hand[i] >> 8) & 0xF
        if hand[i] & 0x8000:
            suit = 'c'
        elif hand[i] & 0x4000:
            suit = 'd'
        elif hand[i] & 0x2000:
            suit = 'h'
        else:
            suit = 's'
        print(rank[r] + suit)

def hand_rank(val):
    if val > 6185:
        return poker_definitions.HIGH_CARD
    if val > 3325:
        return poker_definitions.ONE_PAIR
    if val > 2467:
        return poker_definitions.TWO_PAIR
    if val > 1609:
        return poker_definitions.THREE_OF_A_KIND
    if val > 1599:
        return poker_definitions.STRAIGHT
    if val > 322:
        return poker_definitions.FLUSH
    if val > 166:
        return poker_definitions.FULL_HOUSE
    if val > 10:
        return poker_definitions.FOUR_OF_A_KIND
    return poker_definitions.STRAIGHT_FLUSH

def eval_5cards(c1,c2,c3,c4,c5):
    q = 0
    s = 0
    q = (c1|c2|c3|c4|c5) >> 16
    
    """check for Flushes and StraightFlushes:"""
    if c1 & c2 & c3 & c4 & c5 & 0xF000:
        return arrays.flushes[q]
    """check for Straights and HighCard hands:"""
    s = arrays.unique5[q]
    if s:
        return s
    """let's do it the hard way:"""
    q = (c1&0xFF) * (c2&0xFF) * (c3&0xFF) * (c4&0xFF) * (c5&0xFF)
    q = findit( q )
    return arrays.values[q]

"""hand is an array"""
def eval_5hand(hand):
    c1 = hand[0]
    c2 = hand[1]
    c3 = hand[2]
    c4 = hand[3]
    c5 = hand[4]
    return eval_5cards(c1,c2,c3,c4,c5)

def eval_7hand(hand):
    q = 0
    best = 9999
    subhand = [0,0,0,0,0]

    for i in range(0,21):
        for j in range(0,5):
            subhand[j] = hand[arrays.perm7[i][j]]
        q = eval_5hand(subhand)
        if q < best:
            best = q
    return best

def eval_6hand(hand):
    q = 0
    best = 9999
    subhand = [0,0,0,0,0]

    for i in range(0,6):
        for j in range(0,5):
            subhand[j] = hand[arrays.perm6[i][j]]
        q = eval_5hand(subhand)
        if q < best:
            best = q
    return best
        


