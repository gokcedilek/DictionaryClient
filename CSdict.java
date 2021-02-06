
// You can use this file as a starting point for your dictionary client
// The file contains the code for command line parsing and it also
// illustrates how to read and partially parse the input typed by the user. 
// Although your main class has to be in this file, there is no requirement that you
// use this template or hav all or your classes in this file.

import java.lang.System;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

//
// This is an implementation of a simplified version of a command
// line dictionary client. The only argument the program takes is
// -d which turns on debugging output. 
//

public class CSdict {

	/* map from error codes to messages */
	static HashMap<Integer, String> errors = new HashMap<Integer, String>();

	/* dictionary client */
	static DictClient dictClient;

	static final int MAX_LEN = 255;
	static Boolean debugOn = false;

	private static final int PERMITTED_ARGUMENT_COUNT = 1;
	private static String command;
	private static String[] arguments;

	/*
	 * client commands are categorized into two states: socket connection is open /
	 * socket connection is closed
	 */
	private static enum States {
		CLOSED, OPEN
	}

	/* the initial program state is socket is closed */
	private static States state = States.CLOSED;

	/* set program errors */
	static void initializeErrors() {
		errors.put(900, "900 Invalid command");
		errors.put(901, "901 Too many command line options - Only -d is allowed");
		errors.put(902, "902 Invalid command line option - Only -d is allowed");
		errors.put(903, "903 Incorrect number of arguments");
		errors.put(904, "904 Invalid argument");
		errors.put(910, "910 Supplied command not expected at this time");
		errors.put(920, "920 Control connection to %s on port %s failed to open.");
		errors.put(925, "925 Control connection I/O error, closing control connection");
		errors.put(998, "998 Input error while reading commands, terminating");
		errors.put(999, "999 Processing error. %s.");
	}

	/*
	 * validate the command entered by the user by i) if this command is a valid
	 * command the client can process, ii) checking if this command is allowed in
	 * the current program state
	 */
	static boolean validateCommand(String command) {
		switch (command) {
			case "open":
				if (state == States.OPEN) {
					System.err.println(errors.get(910));
					return false;
				}
				break;
			case "dict":
			case "set":
			case "define":
			case "match":
			case "prefixmatch":
				if (state == States.CLOSED) {
					System.err.println(errors.get(910));
					return false;
				}
				break;
			case "close":
				if (state == States.CLOSED) {
					System.err.println(errors.get(910));
					return false;
				}
				break;
			case "quit":
				return true;
			default:
				System.err.println(errors.get(900));
				return false;
		}
		return true;
	}

	public static void main(String[] args) {

		/* set program errors */
		initializeErrors();

		/* verify command line arguments */
		if (args.length == PERMITTED_ARGUMENT_COUNT) {
			debugOn = args[0].equals("-d");
			if (debugOn) {
				System.out.println("Debugging output enabled");
			} else {
				System.err.println(errors.get(902));
				return;
			}
		} else if (args.length > PERMITTED_ARGUMENT_COUNT) {
			System.err.println(errors.get(901));
			return;
		}

		/* read command line input and extract arguments */
		try {
			String userInput;
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

			/* initial prompt */
			System.out.print("317dict> ");

			while ((userInput = stdIn.readLine()) != null) {
				/* split input into words */
				String[] inputs = userInput.trim().split("( |\t)+");

				/* the command is the first word of input */
				command = inputs[0].toLowerCase().trim();

				/* empty lines and lines starting with '#' are ignored */
				if (command.equals("") || command.equals("#") || command.charAt(0) == '#') {
					System.out.print("317dict> ");
					continue;
				}

				/* arguments follow the command */
				arguments = Arrays.copyOfRange(inputs, 1, inputs.length);

				/* check if the input command is valid */
				if (!validateCommand(command)) {
					System.out.print("317dict> ");
					continue;
				}

				/* print client input if debug mode is on */
				if (debugOn) {
					System.out.println("--> " + userInput);
				}

				/* handle the input */
				/*
				 * NOTE we do not need a default case in this switch because invalid commands
				 * would have been caught in validateCommand above
				 */
				switch (command) {
					case "open":
						cmd_open(arguments);
						break;
					case "dict":
						cmd_dict(arguments);
						break;
					case "set":
						cmd_set(arguments);
						break;
					case "define":
						cmd_define(arguments);
						break;
					case "match":
						cmd_match(arguments, "exact");
						break;
					case "prefixmatch":
						cmd_match(arguments, "prefix");
						break;
					case "close":
						cmd_close(arguments);
						break;
					case "quit":
						cmd_quit(arguments);
						break;
				}
				System.out.print("317dict> ");
			}
		} catch (IOException e) {
			System.err.println(errors.get(998));
			System.exit(1);
		}
	}

	/*
	 * if the arguments are valid, open a socket connection to the specified server
	 * and port and set the state to open
	 */
	private static void cmd_open(String[] arguments) {
		if (arguments.length != 2) {
			System.err.println(errors.get(903));
		} else {
			String host = arguments[0];
			try {
				int port = Integer.parseInt(arguments[1]);
				dictClient = new DictClient(host, port, debugOn);
				state = States.OPEN;
			} catch (NumberFormatException e) {
				System.err.println(errors.get(904));
			} catch (Exception e) {
				String error = String.format(errors.get(920), host, arguments[1]);
				System.err.println(error);
			}
		}
	}

	/* retrieve a list of dictionaries that the server supports */
	private static void cmd_dict(String[] arguments) {
		if (arguments.length != 0) {
			System.err.println(errors.get(903));
			return;
		}
		try {
			dictClient.retrieveDictList(debugOn);
		} catch (IOException e) {
			System.err.println(errors.get(925));
			cmd_close(arguments);
		}
	}

	/* set the dictionary to use */
	private static void cmd_set(String[] arguments) {
		if (arguments.length != 1) {
			System.err.println(errors.get(903));
		} else {
			String dict = arguments[0];
			dictClient.setDictToUse(dict);
		}
	}

	/* retrieve definitions of a word */
	private static void cmd_define(String[] arguments) {
		if (arguments.length != 1) {
			System.err.println(errors.get(903));
			return;
		}
		String word = arguments[0];
		try {
			dictClient.retrieveDefinitions(word, debugOn);
		} catch (IOException e) {
			System.err.println(errors.get(925));
			cmd_close(arguments);
		}
	}

	/* retrieve matches of a word based on a given strategy */
	private static void cmd_match(String[] arguments, String strategy) {
		if (arguments.length != 1) {
			System.err.println(errors.get(903));
			return;
		}
		String word = arguments[0];
		try {
			dictClient.retrieveMatches(word, debugOn, strategy);
		} catch (IOException e) {
			System.err.println(errors.get(925));
			cmd_close(arguments);
		}
	}

	/* close the socket connection */
	private static void cmd_close(String[] arguments) {
		if (arguments.length != 0) {
			System.err.println(errors.get(903));
			return;
		}
		try {
			dictClient.close(debugOn);
			state = States.CLOSED;
		} catch (IOException e) {
			System.err.println(String.format(errors.get(999), e.getMessage()));
		}
	}

	/* close the socket connection if open and exit the program */
	private static void cmd_quit(String[] arguments) {
		if (state == States.OPEN) {
			cmd_close(arguments);
		}
		System.exit(0);
	}
}
