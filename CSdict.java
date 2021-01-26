
// You can use this file as a starting point for your dictionary client
// The file contains the code for command line parsing and it also
// illustrates how to read and partially parse the input typed by the user. 
// Although your main class has to be in this file, there is no requirement that you
// use this template or hav all or your classes in this file.

import java.lang.System;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

//
// This is an implementation of a simplified version of a command
// line dictionary client. The only argument the program takes is
// -d which turns on debugging output. 
//

public class CSdict {

	// error codes (incomplete)
	static final String INVALID_COMMAND = "900 Invalid command";
	static final String TOO_MANY_OPTIONS = "901 Too many command line options - Only -d is allowed";
	static final String INVALID_OPTIONS = "902 Invalid command line option - Only -d is allowed";
	static final String INCORRECT_NUM_ARGS = "903 Incorrect number of arguments";
	static final String INVALID_ARGS = "904 Invalid argument";
	static final String CMD_NOT_EXPECTED = "910 Supplied command not expected at this time";
	static final String CONNECTION_ERROR = "";

	// dictionary client
	static DictClient dictClient;

	static final int MAX_LEN = 255;
	static Boolean debugOn = false;

	private static final int PERMITTED_ARGUMENT_COUNT = 1;
	private static String command;
	private static String[] arguments;

	public static void main(String[] args) {
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
			System.out.println("901 Too many command line options - Only -d is allowed");
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
					case "close":
						// cmd_close();
						System.out.println("close!");
						break;
					case "quit":
						// cmd_quit();
						System.out.println("quit!");
						break;
					default:
						System.out.println("unknown command: " + command);
				}
				System.out.print("317dict> ");
			}
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			System.exit(1);
		}
	}

	private static void cmd_open(String[] arguments) {
		// TODO argument validation
		if (arguments.length != 2)
			return; // error handling in this case??
		String host = arguments[0];
		int port = Integer.parseInt(arguments[1]);
		try {
			dictClient = new DictClient(host, port);
			dictClient.printInfo();
		} catch (UnknownHostException e) {
			System.err.println("UnknownHostException: " + e.getMessage());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			System.exit(1);
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
		if (arguments.length != 1)
			return;
		String dict = arguments[0];
		dictClient.setDictToUse(dict);
		System.out.println("dict is set to: " + dictClient.getDictToUse());
	}

	private static void cmd_define(String[] arguments) {
		if (arguments.length != 1)
			return;
		String word = arguments[0];
		System.out.println("word is: " + word);
		try {
			dictClient.retrieveWordDefn(word);
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			System.exit(1);
		}
		System.out.println("Done!");
	}

	// private static void cmd_close() {
	// dictClient.close();
	// }

	// private static void cmd_quit() {
	// dictClient.close();
	// System.exit(0);
	// }
}
