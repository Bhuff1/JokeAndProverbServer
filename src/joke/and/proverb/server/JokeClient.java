package joke.and.proverb.server;
import java.io.*;
import java.net.*;
import java.lang.StringBuilder;
import java.util.Random;

public class JokeClient { 
	
	static boolean switchServer = false;

	public static void main (String args[]) {
		
		/*
		The following chunk of code handles the command line arguments, of
		which there can be three varieties:
	
		(1) No command line arguments, and JokeClient connects to localhost
		on port 4545.
		(2) One command line argument, and JokeClient connects to that 
		IP address on port 4545.
		(3) Two command line arguments, the first of which connects to that
		IP address on port 4545, the second of which connects to that IP
		address at port 4546.
		*/	
		
		String serverName1 = null;
		String serverName2 = null;
		int port1 = 4545;
		int port2 = 4546;
		boolean cond1 = false;
		boolean cond2 = false;
		boolean cond3 = false;

		if (args.length < 1){
			serverName1 = "localhost";
			System.out.println("Server one: " + serverName1 + ", port " + port1);
			cond1 = true;
		} else if(args[0] != null && args.length == 1){
			serverName1 = args[0];
	 		System.out.println("Server one: " + serverName1 + ", port " + port1);
			cond2 = true;
		} else if(args[0] != null && args[1] != null && args.length == 2) {
			serverName1 = args[0];
			serverName2 = args[1];
			System.out.println("Server one: " + serverName1 + ", port " + port1);
			System.out.println("Server two: " + serverName2 + ", port " + port2);
			cond3 = true;
		}

		/*
		in is a BufferedReader object which will read the input
		from the console.
		*/

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
		/*
		This try/catch block calls a method to get a universally unique 
		identifier, asks the user to enter their name, and lets the
		user have the option of selecting <Enter> for another joke, or
		'quit' to end the program.

		If cond3 == true, then the user can toggle between the two servers
		by entering 's'.
		*/

		try {
			String clientName = null;
			String newInput = "";
			String uuid = generateUUID();
			System.out.print("Please enter your name: ");
			clientName = in.readLine();

			do {
				System.out.flush();
				
				if(clientName.indexOf("quit") < 0) {
					if(cond1 == true || cond2 == true){ 
						getJokeOrProverb(clientName, uuid, serverName1, port1);
					} else if(cond3 == true){
						if(newInput.equals("s")){
							if(switchServer == true) { 
								switchServer = false;
								System.out.println("Now communicating with: " + serverName1 + ", port " + port1); 
							} else { 
								switchServer = true; 
								System.out.println("Now communicating with: " + serverName2 + ", port " + port2);
							}
						}
						getJokeOrProverbTwoServers(clientName, uuid, serverName1, serverName2, port1, port2);
					}
				}

				System.out.print("Press <Enter> for more, or quit: ");
                                
				newInput = in.readLine();

			} while(newInput.indexOf("quit") < 0);

			System.out.println("Canceled by user request.");

		} catch (IOException x) { x.printStackTrace(); }
	} //end main
	
	/*
	This method instantiates a Socket on the client side and enables the information
	returned from the JokeServer to be printed out to the console.
	*/ 

	static void getJokeOrProverb(String clientName, String uuid, String serverName, int port) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;

		try {
			sock = new Socket(serverName, port); //instantiate a Socket

			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); //used to get data from the JokeServer
			toServer = new PrintStream(sock.getOutputStream()); //used to send data to the JokeServer
				
			toServer.println(uuid); //send the uuid to the JokeServer
			toServer.flush(); //flush the buffer to make sure all the bytes are written

			/*
			This chunk of code gets the joke or proverb from the server, appends 
			the user's name to the joke or proverb, and prints it out to the
			console.
			*/
			
			textFromServer = fromServer.readLine();
			clientName += ": ";
			StringBuilder sb = new StringBuilder(textFromServer);
			StringBuilder completeOutput = sb.insert(3, clientName);
			if(textFromServer != null) 
				System.out.println(completeOutput.toString());
			
			sock.close(); //closing the socket

		} catch (IOException x){
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}//end getJokeOrProverb()

	/*
	This method is called if two command line arguments are given. It enables the
	JokeClient to toggle back and forth between the two servers.
	*/

	static void getJokeOrProverbTwoServers(String clientName, String uuid, String serverName1, String serverName2, int port1, int port2){
		Socket sock1;
		BufferedReader fromServer1;
		PrintStream toServer1;
		String textFromServer1;

		Socket sock2;
		BufferedReader fromServer2;
		PrintStream toServer2;
		String textFromServer2;

		try {
			if(JokeClient.switchServer == false) {
				sock1 = new Socket(serverName1, port1); //instantiate a socket

				fromServer1 = new BufferedReader(new InputStreamReader(sock1.getInputStream()));//used to get data from the JokeServer
                        	toServer1 = new PrintStream(sock1.getOutputStream()); //used to send data to JokeServer

                        	toServer1.println(uuid); //send the uuid to the JokeServer
                        	toServer1.flush(); //flush the buffer to make sure all the bytes are written

				textFromServer1 = fromServer1.readLine();//get a joke or proverb from the JokeServer and store it in textFromServer1
                        	clientName += ": ";
                        	StringBuilder sb = new StringBuilder(textFromServer1);
                        	StringBuilder completeOutput = sb.insert(3, clientName);
                        	if(textFromServer1 != null)
                                	System.out.println(completeOutput.toString());

                        	sock1.close(); //closing the socket
			} else if(JokeClient.switchServer == true) {
				sock2 = new Socket(serverName2, port2);

                        	fromServer2 = new BufferedReader(new InputStreamReader(sock2.getInputStream()));
                        	toServer2 = new PrintStream(sock2.getOutputStream());

                        	toServer2.println(uuid);
                        	toServer2.flush();

                        	textFromServer2 = fromServer2.readLine();
                        	clientName += ": ";
                        	StringBuilder sb = new StringBuilder(textFromServer2);
                        	StringBuilder completeOutput = sb.insert(3, clientName);
				completeOutput = completeOutput.insert(0, "<S2>");
                        	if(textFromServer2 != null)
                                	System.out.println(completeOutput.toString());

                        	sock2.close(); 
			}

                } catch (IOException x){
                        System.out.println("Socket error.");
                        x.printStackTrace();
                }
	}

	/*
	This method generates a random number between 0 and 1 billion, makes
	a StringBuilder object, appends the number and the Strng "UUID" to the 
	object, and then returns a String version of the StringBuilder object.
	*/ 
	
	static String generateUUID(){
		Random rand = new Random();
		int result = rand.nextInt(1000000000);
		if(result == 0) result += 1;
		StringBuilder sb = new StringBuilder();
		sb.append("UUID").append(result);
		return sb.toString();	
	} //end generateUUID()
} //end class

