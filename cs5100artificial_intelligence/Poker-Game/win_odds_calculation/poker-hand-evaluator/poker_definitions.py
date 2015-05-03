#!/usr/bin/env python
STRAIGHT_FLUSH=1
FOUR_OF_A_KIND=2
FULL_HOUSE=3
FLUSH=4
STRAIGHT=5
THREE_OF_A_KIND=6
TWO_PAIR=7
ONE_PAIR=8
HIGH_CARD=9

def RANK(x):
    return ((x >> 8) & 0xF)

value_str = [
	"",
	"Straight Flush",
	"Four of a Kind",
	"Full House",
	"Flush",
	"Straight",
	"Three of a Kind",
	"Two Pair",
	"One Pair",
	"High Card"
]

CLUB=0x8000
DIAMOND=0x4000
HEART=0x2000
SPADE=0x1000

Deuce=0
Trey=1
Four=2
Five=3
Six=4
Seven=5
Eight=6
Nine=7
Ten=8
Jack=9
Queen=10
King=11
Ace=12
