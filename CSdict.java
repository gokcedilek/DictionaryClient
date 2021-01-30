
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

//states: start, open, 

public class CSdict {

	static HashMap<Integer, String> errors = new HashMap<Integer, String>();

	// dictionary client
	static DictClient dictClient;

	static final int MAX_LEN = 255;
	static Boolean debugOn = false;

	private static final int PERMITTED_ARGUMENT_COUNT = 1;
	private static String command;
	private static String[] arguments;

	private static enum States {
		START, OPEN, CMD, CLOSE, QUIT
	}

	private static States state = States.START;

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

	// ask: can we return 910 for all these cases?
	static boolean validate(String command) {
		System.out.println("entered validate with: " + command);
		switch (command) {
			case "open":
				if (state == States.OPEN || state == States.CMD) {
					System.out.println(errors.get(910));
					return false;
				}
				break;
			case "dict":
			case "set":
			case "define":
			case "match":
			case "prefixmatch":
				if (state == States.CLOSE || state == States.START) {
					System.out.println(errors.get(910));
					return false;
				}
				break;
			case "close":
				if (state == States.QUIT || state == States.START || state == States.CLOSE) {
					System.out.println(errors.get(910));
					return false;
				}
				break;
			case "quit":
				return true;
			default:
				System.out.println(errors.get(900));
				return false;
		}
		return true;
	}

	public static void main(String[] args) {
		initializeErrors();

		byte cmdString[] = new byte[MAX_LEN];
		int len;
		// Verify command line arguments

		if (args.length == PERMITTED_ARGUMENT_COUNT) {
			debugOn = args[0].equals("-d");
			if (debugOn) {
				System.out.println("Debugging output enabled");
			} else {
				System.out.println("902 Invalid command line option - Only -d is allowed");
				return;
			}
		} else if (args.length > PERMITTED_ARGUMENT_COUNT) {
			// System.out.println("901 Too many command line options - Only -d is allowed");
			System.out.println(errors.get(901));
			return;
		}

		// Example code to read command line input and extract arguments.

		try {
			String userInput;
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("317dict> ");
			while ((userInput = stdIn.readLine()) != null) {
				String[] inputs = userInput.trim().split("( |\t)+");

				command = inputs[0].toLowerCase().trim();

				if (command.equals("") || command.equals("#") || command.charAt(0) == '#') {
					System.out.print("317dict> ");
					continue;
				}

				arguments = Arrays.copyOfRange(inputs, 1, inputs.length);

				if (!validate(command)) {
					System.out.print("317dict> ");
					continue;
				}

				switch (command) {
					case "open":
						cmd_open(arguments);
						break;
					case "dict":
						cmd_dict();
						break;
					case "set":
						cmd_set(arguments);
						break;
					case "define":
						cmd_define(arguments);
						break;
					case "match":
						cmd_match(arguments);
						break;
					case "close":
						// cmd_close();
						break;
					case "quit":
						// cmd_quit();
						break;
				}
				System.out.print("317dict> ");
			}
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			System.exit(1);
		}
	}

	private static void cmd_open(String[] arguments) {
		if (arguments.length != 2) {
			System.err.println(errors.get(903));
		} else {
			String host = arguments[0];
			int port = Integer.parseInt(arguments[1]);
			try {
				dictClient = new DictClient(host, port);
				dictClient.printInfo();
				state = States.OPEN;
			} catch (Exception e) {
				String error = String.format(errors.get(920), host, arguments[1]);
				System.err.println(error);
			}
		}
	}

	private static void cmd_dict() {
		try {
			dictClient.retrieveDictList();
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			System.exit(1);
		}
		System.out.println("Done!");
	}

	private static void cmd_set(String[] arguments) {
		if (arguments.length != 1) {
			System.err.println(errors.get(903));
		} else {
			String dict = arguments[0];
			dictClient.setDictToUse(dict);
			System.out.println("dict is set to: " + dictClient.getDictToUse());
		}
	}

	private static void cmd_define(String[] arguments) {
		if (arguments.length != 1)
			return;
		String word = arguments[0];
		System.out.println("word is: " + word);
		try {
			dictClient.retrieveDefinitions(word);
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			System.exit(1);
		}
	}

	private static void cmd_match(String[] arguments) {
		if (arguments.length != 1)
			return;
		String word = arguments[0];
		System.out.println("word is: " + word);
		try {
			dictClient.retrieveMatchesExact(word);
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			System.exit(1);
		}
	}

	// private static void cmd_close() {
	// dictClient.close();
	// }

	// private static void cmd_quit() {
	// dictClient.close();
	// System.exit(0);
	// }
}
