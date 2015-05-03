#!/usr/bin/python
"""potOddsBot.py:
   This is an agent which will bet the exact amount of chips according
   to the pot odds."""
import sys
import json
import monte_carlo
import brute_force
import CT
import os
import random

debugF = open("debugPotOddsBot", 'a')
if (len(sys.argv) < 2):
    sys.exit(1)
else:
    game_data_location = sys.argv[1]
    game_string = open(game_data_location, 'r')
    game_info = game_string.read()
    game = json.loads(game_info)
    """get the game's unique ID. We use this to read/write from our persistent game data file"""
    game_id = game["gameID"]
    file_exists = os.path.isfile("data_" + game_id)
    #debugF.write("file exists: " + str(file_exists) + " \n")
    if file_exists:
        dataFile = open("data_" + game_id, 'r+')
        fileData = dataFile.read()
        #debugF.write(str(fileData) + "\n")
        data = json.loads(fileData)
        #debugF.write(str(data) + "\n")
    else:
        dataFile = open("data_" + game_id, 'w')
        # construct the json data
        data = {}
        data["bluffChance"] = 0.5
        data["lastBluffHand"] = 0
        data["needToUpdateBluffChance"] = 0
        data["looseness"] = 0.5
        #debugF.write(str(data) + "\n")
    #debugF.write(str(game) + "\n")

    """1. Get the current amount in pot"""
    pot = 0
    for player in game["players"]:
        pot = pot + player["wagered"]
    
    """2. Check the game state:"""
    gameState = game["state"]
    winOdds = 0
    hole = game["self"]["cards"]
    community = []
    if len(game["community"]) > 0:
        community = game["community"]
    #debugF.write("hole " + str(hole) + " community: " + str(community) + " ")
    #debugF.write("here now \n")

    """ This is where we modify our bluff chance based on the opponent's response to our bluff.
        If they called the bluff (call, raise) then we will decrease our bluff chance
        If they folded, we increase our bluff chance
        Thus we learn from successful or unsuccessful bluffs """
    if data["needToUpdateBluffChance"]:
        """ Get our opponent's actions """
        for player in game["players"]:
            if player["name"] != game["self"]["name"]:
                opponent = player
        last_action = ""
        if "river" in opponent["actions"] and (len(opponent["actions"]["river"]) > 0):
            last_action = opponent["actions"]["river"][-1]["type"]
        elif "turn" in opponent["actions"] and (len(opponent["actions"]["turn"]) > 0):
            last_action = opponent["actions"]["turn"][-1]["type"]
        elif "flop" in opponent["actions"] and (len(opponent["actions"]["flop"]) > 0):
            last_action = opponent["actions"]["flop"][-1]["type"]
        elif "pre-flop" in opponent["actions"] and (len(opponent["actions"]["pre-flop"]) > 0):
            last_action = opponent["actions"]["pre-flop"][-1]["type"]
        #debugF.write("last opponent action: " + str(last_action) + "\n")
        """ Modify the bluff chance. If they folded, increase by a maximum of 0.2
            If they called it, decrease the bluff chance by half """
        if last_action == "fold":
            # don't want to increase bluff chance too much
            data["bluffChance"] = data["bluffChance"] + min(float((data["bluffChance"] / 2)), 0.2)
        else:
            data["bluffChance"] = data["bluffChance"] - float((data["bluffChance"] / 2))
        if data["bluffChance"] > 1:
            data["bluffChance"] = 1
        data["needToUpdateBluffChance"] = False

    if (gameState == "pre-flop" or
        gameState == "flop" or
        gameState == "turn" or
        gameState == "river"):
        if gameState != "river":
            winOdds = float(monte_carlo.cal_win_odds_mc(hole, community))
        else:
            winOdds = float(brute_force.cal_win_odds_bf(hole, community))
        """Calculate the bet (Pot odds):"""
        bet = abs(winOdds * pot / (1-winOdds))
        #debugF.write("intial bet: " + str(bet) + " win odds " + str(winOdds) + " pot " + str(pot) + " \n")

        debugF.write(str(game["betting"]) + "\n")
        if bet < game["betting"]["call"]:
            """The largest amout you can bet < what you need to call.
               Can only fold."""
            bet = 0
            action = "FOLDING"
        elif game["betting"]["canRaise"] == False:
            bet = game["betting"]["call"]
            action = "CALLING"
        else:
            """Determine if we will bluff or not based on our bluffChance variable"""
            bet = int(bet/game["betting"]["raise"]) * game["betting"]["raise"]
            action = "RAISING"
            willBluff = random.random()
            """ Will bluff if we haven't already bluffed this hand, the random number is less than the bluff chance, and the raise amount is less than 50"""
            willBluff = (data["lastBluffHand"] != game["hand"]) and (willBluff <= data["bluffChance"]) and (game["betting"]["raise"] < 50)
            if willBluff:
                bet += (2 * game["betting"]["raise"])
                action += " BLUFFING"
                data["lastBluffHand"] = game["hand"]
                data["needToUpdateBluffChance"] = True
        debugF.write("WIN ODDS: " + str(winOdds) + " GAME STATE: " + str(gameState) +  " BET: "
                         + str(bet) + " " + action + "\n")
        print(bet)
    else:
        """complete """
        #debugF.write(str(game["self"]["position"]) + "\n")
        win = (game["winners"][0]["position"] == game["self"]["position"])
        if win:
            debugF.write("WON " + str(game["self"]) + "\n")
        else:
            debugF.write("LOST " + str(game["players"]) + "\n")
        #debugF.write("WIN? " + str(win) + " our chips: " + game["self"]["chips"] + " \n")
        print(0)

    """ dump our data JSON back into the file """
    data_string = json.dumps(data)
    debugF.write(data_string + " \n")
    if file_exists:
        dataFile.seek(0)
    dataFile.write(data_string)
    if file_exists:
        dataFile.truncate()
    dataFile.close()
    debugF.close()
    sys.exit(0)
