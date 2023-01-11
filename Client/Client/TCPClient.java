package Client;

import java.io.*;
import java.net.*;

public class TCPClient extends Client
{
	private static String s_serverHost = "localhost";
	private static int s_serverPort = 6666;

	private Socket clientSocket;
	private Boolean info = true;

	public static void main(String args[])
	{
		try {
			if (args.length > 1) {
				s_serverHost = args[1];
			}
			if (args.length > 2) {
				s_serverPort = Integer.parseInt(args[2]);
			}
		} catch(Exception e) {
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + e.toString());
			System.exit(1);
		}

		// Get a reference to the RMIRegister
		try {
			TCPClient client = new TCPClient();
			client.connect(true);
			client.start();
		} 
		catch (Exception e) {    
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TCPClient()
	{
		super();
	}

	public void connect(boolean printInfo) {
		try {
			boolean first = true;
			while (true) {
				try {
					clientSocket = new Socket(s_serverHost, s_serverPort);
					out = new PrintWriter(clientSocket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					if (printInfo) {
						System.out.println("Connected to host: " + s_serverHost + " at port: " + s_serverPort);
					}
					break;
				} catch (IOException e) {
					if (first) {
						System.out.println("Waiting for host: " + s_serverHost + " at port: " + s_serverPort);
						first = false;
					}
				}
				Thread.sleep(500);
			}
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void stopClient() {
		try {
			in.close();
			out.close();
			clientSocket.close();
		} catch(Exception e) {
			System.err.println((char)27 + "[31;1mUnable to close client: " + (char)27 + "[0m" + e.toString());
		}
	}
	
	public void connectServer() {
		connect(info);
	}
}

