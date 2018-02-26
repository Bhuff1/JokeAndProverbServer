package joke.and.proverb.server;
import java.io.*;
import java.net.*;


public class JokeClientAdmin {
	
	/*
	This is the toggle switch if the user enters two command
	line arguments. If the user enters 's', this variable 
	will be switched to the opposite of what it currently is
	and change its focus to one server or the other.	
	*/
	static boolean switchServer = false; 
	
	public static void main(String[] args){

		/*
		This is logic to handle the command line arguments 
		and the setup of the servers and ports for use 
		later on.
		*/
		
		String serverName1 = null;
                String serverName2 = null;
                int port1 = 5050;
                int port2 = 5051;
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


		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try {
			String input;
			System.out.print("Hit <Enter> to change the Joke Server's mode, or quit: ");
                        input = in.readLine();
			do {
				
				if(input.indexOf("quit") < 0){
					if(cond1 == true || cond2 == true){
						if(input.equals(""))
                                                	changeJokeServerState(serverName1, port1);
                                        } else if(cond3 == true){
                                                if(input.equals("s")){
                                                        if(switchServer == true) { 
								switchServer = false; 
								System.out.println("\nNow communicating with: " + serverName1 + ", port " + port1);
							} else { 
								switchServer = true; 
								System.out.println("\nNow communicating with: " + serverName2 + ", port " + port2);
							}
                                                }
                                                changeJokeServerStateTwoServers(serverName1, port1, serverName2, port2);
                                        }
				System.out.print("Hit <Enter> to change the Joke Server's mode, or quit: ");
                                input = in.readLine();
				}
			} while(input.indexOf("quit") < 0);
			System.out.println("Exited action changing server state.");

		} catch(IOException x) { x.printStackTrace(); }
	}//end main()

		/*
		This method does the work of toggling the JokeServer's mode.
		*/

		private static void changeJokeServerState(String sn, int pn){
			Socket sock;
			BufferedReader fromServer;
			PrintStream toServer;
			String textFromServer;
			try {
				sock = new Socket(sn, pn);
				fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				toServer = new PrintStream(sock.getOutputStream());
				toServer.println("");
				toServer.flush();
				textFromServer = fromServer.readLine();
				if(textFromServer != null)
					System.out.println("\n" + textFromServer + "\n");
				sock.close();
			} catch (IOException x) {
				System.out.println("Socket error.");
				x.printStackTrace();
			}
		}//end changeJokeServerState()

		/*
		If the user enters two command line arguments for different
		servers, this method is called. The method itself enables 
		the user to toggle between both servers.
		*/

		private static void changeJokeServerStateTwoServers(String serverName1, int port1, String serverName2, int port2){
			Socket sock1;
                	BufferedReader fromServer1;
                	PrintStream toServer1;
                	String textFromServer1;

                	Socket sock2;
                	BufferedReader fromServer2;
                	PrintStream toServer2;
                	String textFromServer2;

                	try {
                        	if(switchServer == false) {
                                	sock1 = new Socket(serverName1, port1); //instantiate a socket
                                	fromServer1 = new BufferedReader(new InputStreamReader(sock1.getInputStream()));//used to get information from the server
                                	toServer1 = new PrintStream(sock1.getOutputStream());//used to send information to the server
                                	toServer1.println("");//send information to the server
                                	toServer1.flush();//make sure all the bytes are written to the server 
                                	textFromServer1 = fromServer1.readLine();//get information back from the server
                                	if(textFromServer1 != null)
                                        	System.out.println("\n" + textFromServer1 + "\n");
                                	sock1.close(); //closing the socket
                        	} else if(switchServer == true) {
                                	sock2 = new Socket(serverName2, port2);//instantiate a socket
                                        fromServer2 = new BufferedReader(new InputStreamReader(sock2.getInputStream()));
                                        toServer2 = new PrintStream(sock2.getOutputStream());
                                        toServer2.println("");
                                        toServer2.flush();
                                        textFromServer2 = fromServer2.readLine();
                                        if(textFromServer2 != null)
                                                System.out.println("\n<S2>: " + textFromServer2 + "\n");
                                        sock2.close(); //closing the socket
                        	}

                	} catch (IOException x){
                        	System.out.println("Socket error.");
                        	x.printStackTrace();
               		 }
		}//end changeJokeServerStateTwoServers()
}//end class

