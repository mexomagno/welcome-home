#!/usr/bin/python
from server import USER_DATA_DIRECTORY, USER_DATA_FILE_TYPE
import os
from colorama import init, Style, Fore, Back

MODULE_DESCRIPTION = """
This module manages users.
You can create, edit and remove users with this tool.
I tried to make it as straight forward as possible.
"""
MODULE_VERSION = "1.0"

REQUIRED_USER_FIELDS = {"username": {"prompt": "Enter a unique user nickname"},
                        "aliases" : {"prompt": "Now, some friendly names people calls you by (separated by a comma)",
                                     "default_value": "Mister Flopsypops"},
                        "gender": {"prompt": "Your gender (m/f)",
                                   "valid_values": ["m", "f"]},
                        "real_name" : {"prompt": "Whats your real first name?",
                                       "default_value": "Flopsy"},
                        "real_lastname" : {"prompt": "And your real lastname?",
                                           "default_value": "Von Harley"},
#                        "wants_music": {"prompt": "Do you want to be greeted with some music?",
#                                        "default_value": "true",
#                                        "valid_values": ["true", "false"]},
                        "music_probability": {"prompt": "How often would you want to be greeted with music? (from 0 to 100)",
                                              "default_value": 100,
                                              "valid_values": " ".join(str(e) for e in range(0,100+1)).split()}
                        }
FIELD_PROMPT_ORDER = ["username", "real_name", "real_lastname", "aliases", "gender", "music_probability"]
def onlyOneIsSet(*args):
    return sum(args) == 1

def listUsers():
    files = os.listdir(USER_DATA_DIRECTORY)
    users = ""
    for file in files:
        if file.lower().endswith(".{}".format(USER_DATA_FILE_TYPE)):
            users += "\t" + (file.lower())[0:(len(file) - len(USER_DATA_FILE_TYPE) - 1)] + "\n"
    print "\nRegistered usernames:"
    print users

def addUser():
    user_data = {}
    PROMPT_COLOR = Fore.LIGHTCYAN_EX
    USER_COLOR = Fore.WHITE
    ERROR_COLOR = Fore.LIGHTRED_EX
    NO_COLOR = Fore.RESET
    for key in FIELD_PROMPT_ORDER:
        current_field = REQUIRED_USER_FIELDS[key]
        prompt = current_field["prompt"]
        prompt = "{PROMPT_COLOR}{PROMPT}{DEFAULT_VALUE} : {USER_COLOR}".format(PROMPT = prompt,
                                                                               DEFAULT_VALUE = " [default: {}]".format(
                                                                                   current_field["default_value"]) if "default_value" in current_field.keys() else "",
                                                                               PROMPT_COLOR = PROMPT_COLOR,
                                                                               USER_COLOR = USER_COLOR)
        user_input = raw_input(prompt)
        while ("valid_values" in current_field.keys() and user_input.strip().lower() not in current_field["valid_values"]) \
                or (user_input == "" and "default_value" not in current_field.keys()):
            print ERROR_COLOR + "Error: Your input is invalid. Please retry"
            user_input = raw_input(prompt)
        if user_input == "":
            user_input = current_field["default_value"]
        user_data[key] = user_input

        # Specific cases
        if key == "aliases":
            user_data[key] = user_data[key].split(",")

        print "You chose: '{}'".format(user_data[key]) + NO_COLOR

    print user_data

def parseArguments():
    import argparse as AP
    parser = AP.ArgumentParser(prog = os.path.basename(__file__), description = MODULE_DESCRIPTION, epilog = "V{}".format(MODULE_VERSION))
    parser.add_argument("-a", "--add", action = "store_true")
    parser.add_argument("-m", "--modify", action = "store_true")
    parser.add_argument("-d", "--delete", action = "store_true")
    parser.add_argument("-l", "--list", action = "store_true")
    args = parser.parse_args()
    # validate
    if sum([args.add, args.modify, args.delete, args.list]) == 0:
        print "Warning: No arguments. Assuming --list"
        args.list = True
    if args.list:
        if sum([args.add, args.modify, args.delete]):
            print "Ignoring unused parameters: {} {} {}".format("-a" if args.add else "", "-m" if args.modify else "", "-d" if args.delete else "")
        listUsers()
        return
    if not onlyOneIsSet(args.add, args.modify, args.delete):
        print "Error: You can only select one of the three user editing options"
        exit(1)
    if args.add:
        addUser()
        return
    if args.delete:
        #deleteUser()
        print "delete user"
        return
    if args.modify():
        #modifyUser()
        print "modify user"
        return

def main():
    args = parseArguments()

if __name__ == "__main__":
    main()