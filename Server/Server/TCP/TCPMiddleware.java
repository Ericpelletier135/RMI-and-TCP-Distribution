// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.TCP;

import Server.Common.*;

import java.rmi.NotBoundException;
import java.util.*;
import java.io.*;
import java.net.*;

public class TCPMiddleware extends Middleware
{
	private static String s_serverName = "Server";
	//TODO: ADD YOUR GROUP NUMBER TO COMPLETE
	private static String s_rmiPrefix = "group_35_";

	private static TCPMiddleware middleware = null;
	private static int s_serverPort = 6666;
	private ServerSocket serverSocket;
	private static String flightIP = "localhost";
	private static int flightPort = 6667;
	private static String carIP = "localhost";
	private static int carPort = 6668;
	private static String roomIP = "localhost";
	private static int roomPort = 6669;
	private static String customerIP = "localhost";
	private static int customerPort = 6670;

	public static void main(String args[])
	{
		try {
			String flightRM_name = s_serverName + "Flight";
			String carRM_name = s_serverName + "Car";
			String roomsRM_name = s_serverName + "Rooms";
			String customerRM_name = s_serverName + "Customer";

			middleware = new TCPMiddleware(flightIP, flightPort, carIP, carPort, roomIP, roomPort, customerIP, customerPort);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					middleware.stop();
				}
			});
			System.out.println("Starting 'Middleware:" + s_serverPort + "'");
			middleware.start(s_serverPort);
		} catch(Exception e) {
			System.err.println((char)27 + "[31;1mMiddleware exception: " + (char)27 + e.toString());
			System.exit(1);
		}
	}

	public TCPMiddleware(String flightIP, int flightPort, String carIP, int carPort, String roomIP, int roomPort, String customerIP, int customerPort)
	{
		super(flightIP, flightPort, carIP, carPort, roomIP, roomPort, customerIP, customerPort);
	}

	private void start(int port) {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Listening on port: " + port);
			while (true)
				new ClientHandler(serverSocket.accept()).start();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			stop();
		}
	}

	public void stop() {
		try {
			this.close();
			serverSocket.close();
			System.out.println("Middleware: " + s_serverPort + " Server Socket closed");
		}
		catch(IOException e) {
			System.err.println((char)27 + "[31;1mMiddleware exception: " + (char)27 + e.toString());
		}
	}


	private static class ClientHandler extends Thread {
		private Socket clientSocket;
		private PrintWriter out;
		private BufferedReader in;

		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String inputLine = in.readLine();

				Vector<String> parsedCommand = parse(inputLine);

				if (parsedCommand == null) {
					out.println("");
					in.close();
					out.close();
					clientSocket.close();
					return;
				}

				String result = middleware.execute(parsedCommand);

				out.println(result);
				in.close();
				out.close();
				clientSocket.close();
			} catch(IOException e) {
				System.err.println((char)27 + "[31;1mMiddleware exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
			}
		}

		public static Vector<String> parse(String input)
		{
			if (input == null || input.length() == 0)
				return null;

			String command;

			if (input.charAt(0) == '[' && input.charAt(input.length() - 1) == ']')
				command = input.substring(1,input.length() - 1);
			else
				command = input;

			Vector<String> arguments = new Vector<String>();
			StringTokenizer tokenizer = new StringTokenizer(command,",");
			String argument = "";
			while (tokenizer.hasMoreTokens())
			{
				argument = tokenizer.nextToken();
				argument = argument.trim();
				arguments.add(argument);
			}
			return arguments;
		}
	}
}
