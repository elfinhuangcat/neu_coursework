#!/usr/bin/env python
"""card_translate.py"""
import arrays

"""trans_string_to_int(card):
   Given: card is a string, eg. 'Qd'.
          card[0] is the rank. card[1] is the suit.
   Return: An integer that is the inner representation of the card.

   This procedure actually combines the card_translate() and
   translate_from_mp_to_string() together.
   """
def trans_string_to_int(card):
    cardTuple = card_translate_from_str_to_rs(card)
    return card_translate_from_rs(cardTuple[0],cardTuple[1])

"""card_translate_from_rs(rank, suit):
   Translate the card into the evaluator's inner representation and return it.
   rank: (Number) (deuce=0,trey=1,four=2,five=3,...,ace=12)
   suit: (String) 's','h','c','d'
   """
def card_translate_from_rs(rank, suit):
    suitInt = 0
    if suit == 's':
        suitInt = 1 << 12
    elif suit == 'h':
        suitInt = 2 << 12
    elif suit == 'd':
        suitInt = 4 << 12
    else:
        suitInt = 8 << 12
    prime = ( arrays.primes[rank] |
              rank << 8 |
              suitInt |
              (1 << (16+rank)))
    return prime

"""card_translate_from_str_to_rs(card):
   Translate a card in the MachinePoker form to the rank + suit form."""
"""eg. 'Qd' -> (10,'d')
   The return value is a tuple, (rank, suit)"""
def card_translate_from_str_to_rs(card):
    card = str(card)
    rank = card[0]
    suit = card[1]
    if rank == '2':
        return (0,suit)
    elif rank == '3':
        return (1,suit)
    elif rank == '4':
        return (2,suit)
    elif rank == '5':
        return (3,suit)
    elif rank == '6':
        return (4,suit)
    elif rank == '7':
        return (5,suit)
    elif rank == '8':
        return (6,suit)
    elif rank == '9':
        return (7,suit)
    elif rank == 'T':
        return (8,suit)
    elif rank == 'J':
        return (9,suit)
    elif rank == 'Q':
        return (10,suit)
    elif rank == 'K':
        return (11,suit)
    elif rank == 'A':
        return (12,suit)
    else:
        raise ValueError('translate from mp to string:'
                         + 'The given card is incorret!')
    

if __name__ == '__main__':
    print(trans_string_to_int('Qd'))
    print(trans_string_to_int('Ts'))
    print(trans_string_to_int('2h'))
    print(trans_string_to_int('Jc'))
    print(card_translate_from_str_to_rs('Qd'))
    print(card_translate_from_str_to_rs('Ts'))
    print(card_translate_from_str_to_rs('2h'))
    print(card_translate_from_str_to_rs('Jc'))
    print(card_translate_from_str_to_rs('Kc'))
    print(card_translate_from_str_to_rs('7s'))
