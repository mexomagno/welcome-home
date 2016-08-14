#!/usr/bin/python
import json

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


# Colors
PROMPT_COLOR = Fore.LIGHTCYAN_EX
USER_COLOR = Fore.WHITE
ERROR_COLOR = Fore.LIGHTRED_EX
NO_COLOR = Fore.RESET
WARNING_COLOR = Fore.LIGHTMAGENTA_EX
SUCCESS_COLOR = Fore.LIGHTGREEN_EX

def onlyOneIsSet(*args):
    return sum(args) == 1

def getUsersList():
    files = os.listdir(USER_DATA_DIRECTORY)
    users = []
    for file in files:
        if file.lower().endswith(".{}".format(USER_DATA_FILE_TYPE)):
            users.append((file.lower())[0:(len(file) - len(USER_DATA_FILE_TYPE) - 1)])
    return users

def listUsers():
    print PROMPT_COLOR + "\nRegistered usernames:"
    for user in getUsersList():
        print USER_COLOR + "\t{}".format(user)

def userExists(user):
    return user.lower() in getUsersList()

def addUser(modify = False, old_user_dict = None):
    user_data = {}
    for key in FIELD_PROMPT_ORDER:
        current_field = REQUIRED_USER_FIELDS[key]
        prompt = current_field["prompt"]
        prompt = "{PROMPT_COLOR}{PROMPT}{DEFAULT_VALUE} : {USER_COLOR}".format(PROMPT = prompt,
                                                                               DEFAULT_VALUE = " [default: {}]".format(
                                                                                   current_field["default_value"]) if "default_value" in current_field.keys() else "",
                                                                               PROMPT_COLOR = PROMPT_COLOR,
                                                                               USER_COLOR = USER_COLOR)
        user_input = raw_input(prompt)
        while ("valid_values" in current_field.keys() and user_input.strip().lower() not in current_field["valid_values"] and "default_value" not in current_field.keys())\
                or (user_input == "" and "default_value" not in current_field.keys())\
                or (key == "username" and userExists(user_input) and not modify)\
                or (user_input != "" and  "valid_values" in current_field.keys() and user_input.lower().strip() not in current_field["valid_values"]):
            if (key == "username" and userExists(user_input) and not modify):
                print ERROR_COLOR + "Error: User already exists. Please retry"
            else:
                print ERROR_COLOR + "Error: Your input is invalid. Please retry"
            user_input = raw_input(prompt)
        if user_input == "":
            user_input = current_field["default_value"]
        user_data[key] = user_input
        # Specific cases
        if key == "aliases":
            user_data[key] = user_data[key].split(",")
            user_data[key] = map(str.strip, user_data[key])
        if key == "gender":
            user_data[key] = "male" if user_data[key].lower().strip().startswith("m") else "female"
        print "You chose: '{}'".format(user_data[key]) + NO_COLOR
    # Fill remaining fields
    user_data["wants_music"] = "true" if int(user_data["music_probability"]) > 0 else "false"
    if not modify:
        user_data["last_greet"] = 0
    else:
        if old_user_dict is not None:
            user_data["last_greet"] = old_user_dict["last_greet"]
    # Write data to a file
    new_user_file_path = USER_DATA_DIRECTORY + "/" + user_data["username"].lower().strip() + "." + USER_DATA_FILE_TYPE
    with open(new_user_file_path, "w") as new_user_file:
        new_user_file.write(json.dumps(user_data, indent = 4, sort_keys = True))
    return user_data["username"].lower().strip()

def modifyUser():
    prompt = PROMPT_COLOR + "Enter user to modify : "+ USER_COLOR
    user_input = raw_input(prompt)
    while not userExists(user_input.strip().lower()):
        print ERROR_COLOR + "Error: User doesn't exists. Please retry"
        user_input = raw_input(prompt)
    # User selected. Load user configuration
    selected_user = user_input.lower().strip()
    user_config_file_path = USER_DATA_DIRECTORY + "/" + selected_user + "." + USER_DATA_FILE_TYPE
    with open(user_config_file_path) as user_file:
        user_config_dict = json.loads(user_file.read().replace("\n", ""))
    # Replace local dict default values with previously loaded values
    for key in REQUIRED_USER_FIELDS:
        if type(user_config_dict[key]) == list:
            REQUIRED_USER_FIELDS[key]["default_value"] = ", ".join(str(e) for e in user_config_dict[key])
        else:
            REQUIRED_USER_FIELDS[key]["default_value"] = user_config_dict[key]
    # Repeat process similar to addUser
    username = addUser(modify = True, old_user_dict = user_config_dict)
    # If we changed the user name, we must delete old config file
    if username != selected_user:
        os.remove(user_config_file_path)
    return username

def deleteUser():
    prompt = PROMPT_COLOR + "Enter user to delete : "+ USER_COLOR
    user_input = raw_input(prompt)
    while not userExists(user_input.strip().lower()):
        print ERROR_COLOR + "Error: User doesn't exists. Please retry"
        user_input = raw_input(prompt)
    prompt = WARNING_COLOR + "WARNING: ARE YOU SURE YOU WANT TO DELETE USER '{}'? (yes/no) : ".format(USER_COLOR + user_input.strip().lower() + WARNING_COLOR) + USER_COLOR
    username_to_delete = user_input.strip().lower()
    user_input = raw_input(prompt)
    while user_input.strip().lower() not in ["yes", "no"]:
        print ERROR_COLOR + "Error: You must enter 'yes' or 'no'. Please retry"
        user_input = raw_input(prompt)
    if user_input == "no":
        print PROMPT_COLOR + "Cancelled." + NO_COLOR
        exit(0)
    # Delete selected user
    os.remove(USER_DATA_DIRECTORY + "/" + username_to_delete + "." + USER_DATA_FILE_TYPE)
    return username_to_delete

def parseArguments():
    import argparse as AP
    parser = AP.ArgumentParser(prog = os.path.basename(__file__), description = MODULE_DESCRIPTION, epilog = "V{}".format(MODULE_VERSION))
    parser.add_argument("-a", "--add", action = "store_true", help = "Add a new user")
    parser.add_argument("-m", "--modify", action = "store_true", help = "Modify an existing user")
    parser.add_argument("-d", "--delete", action = "store_true", help = "Delete an existing user")
    parser.add_argument("-l", "--list", action = "store_true", help = "List currently available users")
    args = parser.parse_args()
    # validate
    if sum([args.add, args.modify, args.delete, args.list]) == 0:
        print WARNING_COLOR + "Warning: No arguments. Assuming --list" + NO_COLOR
        args.list = True
    if args.list:
        if sum([args.add, args.modify, args.delete]):
            print WARNING_COLOR + "Ignoring unused parameters: {} {} {}".format("-a" if args.add else "", "-m" if args.modify else "", "-d" if args.delete else "") + NO_COLOR
        listUsers()
        return
    if not onlyOneIsSet(args.add, args.modify, args.delete):
        print "Error: You can only select one of the three user editing options"
        exit(1)
    if args.add:
        username = addUser()
        if username is not None:
            print SUCCESS_COLOR + "Successfully created user '{}'".format(USER_COLOR + username + SUCCESS_COLOR) + NO_COLOR
        return
    if args.delete:
        username = deleteUser()
        if username is not None:
            print SUCCESS_COLOR + "Succesfully deleted user '{}'".format(USER_COLOR + username + SUCCESS_COLOR) + NO_COLOR
        return
    if args.modify:
        username = modifyUser()
        if username is not None:
            print SUCCESS_COLOR + "Successfully edited user '{}'".format(USER_COLOR + username + SUCCESS_COLOR) + NO_COLOR
        return

def main():
    args = parseArguments()

if __name__ == "__main__":
    main()