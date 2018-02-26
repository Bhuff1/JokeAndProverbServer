package joke.and.proverb.server;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Random;

/*--------------------------------------------------------
This class does the work of getting the UUID from the JokeClient and getting
a joke or proverb--depending on the JokeServer mode--from either the JokeManager
or ProverbManager.

This class also initiates contact with the JokeServer class to update the state
of the conversation.
--------------------------------------------------------*/

class Worker extends Thread {
	Socket sock;
	Worker (Socket s) {sock = s;}

	/*
        This method is called after the thread is instantiated and then started by calling start().
	*/	

	public void run(){
		PrintStream out = null;
		BufferedReader in = null;
		try {
			/*
			in will read the information typed in on the
			client
			*/

			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			/*
			out will enable our server to send information
			back to the client
			*/

			out = new PrintStream(sock.getOutputStream());

			try {
				String uuid  = in.readLine();
				String jokeOrProverbValue = null;

				do {
					jokeOrProverbValue = getJokeOrProverb();
					
				}while(!JokeServer.updateConversation(uuid, jokeOrProverbValue));
				out.println(jokeOrProverbValue);
			} catch (IOException x){
				System.out.println("Server read error");
				x.printStackTrace();
			}
			sock.close();
		} catch(IOException ioe) {System.out.println(ioe);}
	}

	/*
	This method invokes the JokeManager or ProverbManager to get a
	joke or proverb. It checks to see what mode the server is in
	to decide which manager to call.
	*/	

	static String getJokeOrProverb() {
		if(JokeServer.jokeServerModeOn == true){
			Joke joResponse = JokeManager.getInstance().getJoke();
			return joResponse.getJoke();
		} else {
			Proverb proResponse = ProverbManager.getInstance().getProverb();
			return proResponse.getProverb(); 
		}
	}
}

/*--------------------------------------------------------
This class does work for the JavaClientAdmin, which has
only one purpose: to toggle the JokeServer mode from Joke to
Proverb, or from Proverb to Joke.
--------------------------------------------------------*/

class AdminWorker extends Thread {
	Socket sock;
	AdminWorker(Socket s) {sock = s;}

	public void run(){
		PrintStream out = null;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());

			try {
				String adminClientChoice = in.readLine();
			
				if(adminClientChoice.equals(""))
					switchServerMode(out);
				
			} catch(IOException x) { System.out.println(x); }
			sock.close();
		} catch(IOException ioe) { System.out.println(ioe); }
	}//end run()

	/*
	This method enables the JokeClientAdmin to toggle back
	and forth between the two modes, Joke and Proverb.

	If boolean variable jokeServerModeOn is FALSE, then
	we're in Proverb mode. 

	If boolean variable jokeServerModeOn is TRUE, then
	we're in Joke mode, which is the DEFAULT.
	*/

	static void switchServerMode(PrintStream o){
		if(JokeServer.jokeServerModeOn == true){
			JokeServer.jokeServerModeOn = false;
			o.println("Changed mode to Proverb.");
			System.out.println("JokeServer in Proverb mode.");
		}
		else {
			JokeServer.jokeServerModeOn = true;
			o.println("Changed mode to Joke.");
			System.out.println("JokeServer in Joke mode.");
		}
	}//end switchServerMode()
}//end AdminWorker class


/*--------------------------------------------------------
This class is meant to be used by the JokeClientAdmin
class. It instantiates an AdminWorker thread and starts
it.
--------------------------------------------------------*/

class AdminLooper implements Runnable {

	public static boolean adminControlSwitch = true;

	public void run(){

		int q_len = 6;
		int port;
		Socket sock;

		try {
			ServerSocket servSock = new ServerSocket((JokeServer.secondaryPorts == false ? 5050 : 5051), q_len);
			while(adminControlSwitch){
				sock = servSock.accept();
				if(sock == null) System.out.println("Sock was null.");
				new AdminWorker(sock).start();
			}
		} catch (IOException ioe) { System.out.println(ioe); }
	}//end run()
}//end AdminLooper class

/*--------------------------------------------------------
This class holds the main() method which instantiates the
server. 

In the main() method, we have two different types of threads
being used: (1) an AdminWorker thread for the JokeClientAdmin, and 
(2) a Worker thread for the JokeClient.

It has a boolean variable named jokeServermodeOn which is TRUE
when we're in joke mode, and FALSE when we're in Proverb mode.

It also has a data structure called conversationRecord the 
keeps track of the state of the conversation on the server itself.
The breakdown of the data structure is that it's a HashMap of ArrayLists 
of HashSets:

(1) The HashMap's keys are the UUIDs sent from the JokeClient.
(2) The ArrayList only has two valid indexes: (0) for jokes and (1)
for proverbs.
(3) The HashSet holds values of type String that are the jokes or
proverbs.
--------------------------------------------------------*/

public class JokeServer {

	public static boolean secondaryPorts = false;//acts as a flag to indicate if the JokeServer is using secondary ports
	
	public static boolean jokeServerModeOn = true;//acts as a flag to indicate if we are in Joke or Proverb mode

	public static HashMap<String, ArrayList<HashSet<String>>> conversationRecord = new HashMap<>();

	public static void main(String[] args) throws IOException {
		int q_len = 6;
		int port = 0;
		Socket sock;
		
		if(args.length < 1)
			port = 4545;
		else if(args[0].equals("secondary")) {
			port = 4546;
			secondaryPorts = true;
		}
		

		AdminLooper AL = new AdminLooper();
		Thread t = new Thread(AL);
		t.start();	
	
		/*
		The ServerSocket listens for requests and then responds
		to the request.
		*/

		ServerSocket servsock = new ServerSocket(port, q_len);

		System.out.println("\nJoke Server is ready to serve up some wisecracks and wisdom.\n");
		
		if(secondaryPorts == true) {
			System.out.println("Joke Server is in secondary port mode.");
		}

		while(true){
			sock = servsock.accept(); //listens for the connection and returns a Socket object.
			new Worker(sock).start(); //instantiate the thread--with our socket!--and start it.
		}
	}
	
	/*
	This method is the starting point for updating the state of the conversation
	between the JokeClient and JokeServer. 

	The method first looks to see if there is a UUID for the JokeClient that has 
	connected. If there is NOT a UUID in the conversationRecord data structure, then
	we make one, and then call parseJokeOrProverb(). If there is, then we call 
	parseJokeOrProverb() right away.
	*/
	
	public static boolean updateConversation(String uuid, String jokeOrProverb) {
		if(!conversationRecord.containsKey(uuid)){
					
			conversationRecord.put(uuid, new ArrayList<>());
                        conversationRecord.get(uuid).add(0, new HashSet<>()); //jokes
                        conversationRecord.get(uuid).add(1, new HashSet<>()); //proverbs

			return parseJokeOrProverb(uuid, jokeOrProverb);
		} else {
			return parseJokeOrProverb(uuid, jokeOrProverb);
		}
	}//end updateConversation()

	/*
	This method does the work of determining whether or not a JokeClient
	has seen a joke or proverb before. 

	If the JokeClient has NOT seen the joke/proverb, we return TRUE.

	If the JokeClient HAS seen the joke/proverb, we return FALSE.
	
	If the HashSet for the joke/proverb has 4 elements in it, we clear()
	it out so we can begin inserting random jokes/proverbs for the JokeClient
	again.
	*/

	public static boolean parseJokeOrProverb(String uuid, String jokeOrProverb) {

		char jOrP = jokeOrProverb.charAt(0);

                        switch (jOrP) {

                                case 'J': {
                                        if(!conversationRecord.get(uuid).get(0).contains(jokeOrProverb)){
                                                conversationRecord.get(uuid).get(0).add(jokeOrProverb);

                                                if(conversationRecord.get(uuid).get(0).size() == 4){
                                                        conversationRecord.get(uuid).get(0).clear();
                                                }

                                                return true;
                                        } else {
                                                return false;
                                        }
                                }
                                case 'P': {
                                        if(!conversationRecord.get(uuid).get(1).contains(jokeOrProverb)){
                                                conversationRecord.get(uuid).get(1).add(jokeOrProverb);

                                                if(conversationRecord.get(uuid).get(1).size() == 4){
                                                        conversationRecord.get(uuid).get(1).clear();
                                                }
                                                return true;
                                        } else {
                                                return false; 
                                        }
                                }
                        }
                        System.out.println("ERROR: problem in parseJokeOrProverb() method.");
                        return false;
	} //end parseJokeOrProverb()
} //end JokeServer class	

/*--------------------------------------------------------
This class simply encapsulates a Joke.
--------------------------------------------------------*/

class Joke {

        private String joke;

        public Joke(String joke){
                this.joke = joke;
        }

        public String getJoke(){
                return joke;
        }
}

/*--------------------------------------------------------
This class simply encapsulates a Proverb.
--------------------------------------------------------*/

class Proverb {

        private String proverb;

        public Proverb(String proverb){
                this.proverb = proverb;
        }

        public String getProverb(){
                return proverb;
        }
}

/*--------------------------------------------------------
This class facilitates the management of the jokes and enables the
randomization process in the getJoke() method. It uses the Singleton
design pattern because we only want one instance of it, and we
want to be able to get a joke at any place in our JokeServer
code.

The following are where the jokes were taken from:


JA was taken from http://kickasshumor.com/c/4/funny-one-liner-jokes
JB was taken from http://laffgaff.com/funny-one-liners/
JC was taken from http://laffgaff.com/funny-one-liners/
JD was taken from http://laffgaff.com/funny-one-liners/
--------------------------------------------------------*/

class JokeManager {

        HashMap<String, Joke> jokes;
        private static JokeManager instance;

        private JokeManager() {
                loadJokes();
        }

        public static JokeManager getInstance(){
                if(instance == null)
                        instance = new JokeManager();
                return instance;
        }

	private void loadJokes(){

                jokes = new HashMap<>();
                jokes.put("JA", new Joke("JA  Phones are getting thinner and smarter. People, not so much."));
                jokes.put("JB", new Joke("JB  Where there's a will, there's a relative."));
                jokes.put("JC", new Joke("JC  I recently decided to sell my vacuum cleaner as all it was doing was gathering dust."));
                jokes.put("JD", new Joke("JD  I like to hold hands at the movies...which always seems to startle strangers."));
        }//end loadJokes()

        /*
        This method enables randomization for what jokes are returned. It
        uses the Random class's nextInt() method to get a random integer
        between 0--if it returns 0, we just add 1 to it-- and 5 (non-inclusive).
        */

        public Joke getJoke(){
                Random rand = new Random();
                int result = rand.nextInt(5);
                if(result == 0) result += 1;
                StringBuilder sb = new StringBuilder();
                sb.append("J");
                switch(result) {
                        case 1: sb.append("A");
                                break;
                        case 2: sb.append("B");
                                break;
                        case 3: sb.append("C");
                                break;
                        case 4: sb.append("D");
                                break;
                }
                return jokes.get(sb.toString());
        }//end getJoke()
}//end class

/*--------------------------------------------------------
This class facilitates the management of the proverbs and enables the
randomization process in the getProverb() method. It uses the Singleton
design pattern because we only want one instance of it, and we
want to be able to get a proverb at any place in our JokeServer
code.

All of the proverbs were taken from:

http://www.phrasemix.com/collections/the-50-most-important-english-proverbs
--------------------------------------------------------*/

class ProverbManager {
        HashMap<String, Proverb> proverbs;
        private static ProverbManager instance;

        private ProverbManager(){
                loadProverbs();
        }

        public static ProverbManager getInstance(){
                if(instance == null)
                        instance = new ProverbManager();
                return instance;
        }

        private void loadProverbs(){
                proverbs = new HashMap<>();
                proverbs.put("PA", new Proverb("PA  The pen is mightier than the sword."));
                proverbs.put("PB", new Proverb("PB  Easy come, easy go."));
                proverbs.put("PC", new Proverb("PC  Necessity is the mother of invention."));
                proverbs.put("PD", new Proverb("PD  Absence makes the heart grow fonder."));
        }//end loadProverbs()

	        /*
        This method enables randomization for what proverbs are returned. It
        uses the Random class's nextInt() method to get a random integer
        between 0--if it returns 0, we just add 1 to it--and 5 (non-inclusive).
        */

        public Proverb getProverb(){
                Random rand = new Random();
                int result = rand.nextInt(5);
                if(result == 0) result += 1;
                StringBuilder sb = new StringBuilder();
                sb.append("P");
                switch(result) {
                        case 1: sb.append("A");
                                break;
                        case 2: sb.append("B");
                                break;
                        case 3: sb.append("C");
                                break;
                        case 4: sb.append("D");
                                break;
                }
                return proverbs.get(sb.toString());
        }//end getProverb()
}//end class


