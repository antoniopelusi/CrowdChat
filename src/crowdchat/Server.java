package crowdchat;

import java.util.ArrayList; 

import java.io.File; 
import java.io.FileOutputStream; 
import java.io.FileInputStream; 
import java.io.ObjectOutputStream; 
import java.io.ObjectInputStream; 
import java.io.EOFException; 

import java.rmi.server.*; 
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;


/**
 * Create the "RMI register" and "Linker" used to communicate with clients, 
 * and load/save messages history on start/shut off.
 */
public class Server 
{
    public static void main(String[] args) 
    {
        new Server(parseArgs(args));
    }

    public static String parseArgs(String[] args)
    {
        if (args.length < 1) 
        {
            return "localhost";
        }

        return args[0];
    }
    
    
    // Path constants.
    private final String HOME_DIR_PATH = System.getProperty("user.home") 
        + File.separator + ".crowdchat";
    private final String HISTORY_FILE_PATH = HOME_DIR_PATH + File.separator 
        + "history"; 

    // Linker btw server and clients.
    private final Linker.BasicLinker mLinker;

    public Server(String host)
    {
        mLinker = new Linker.BasicLinker();
        // Create/check existence of message history file. 
        createHomeDir();
        createHistoryFile();
        // And retrieve this history (if needed).
        retrieveMessageHistory();

        try 
        {
            // Avoid the "rmiregistry & / start rmiregistry" command if on local.
            if (host.equals("localhost"))
            {
                LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            }
            // Register the remoted object.
            Linker linker_stub = (Linker) 
                UnicastRemoteObject.exportObject(mLinker, 0);
            Registry registry = LocateRegistry.getRegistry(host);
            registry.rebind("rmi://server/ConnectService", linker_stub);
            // Save the messages when exiting.  
            Runtime.getRuntime().addShutdownHook(new Thread(this::saveMessageHistory));
        } 
        catch (Exception e) 
        {
            System.err.println("Error: " + e);
        }

        // Debug.
        System.out.println ("Server ready...");
    }

    public void retrieveMessageHistory()
    {
        try
        {
            // Open a stream to the file.
            ObjectInputStream stream = new ObjectInputStream(
                    new FileInputStream(HISTORY_FILE_PATH));
            // Then read the messages.
            @SuppressWarnings("unchecked")
            ArrayList<Linker.Message> messages = (ArrayList<Linker.Message>) stream.readObject(); 

            if (messages != null)
            {
                mLinker.setClientMessages(messages);
            }

            stream.close();
        }
        catch (EOFException e) 
        {
            // If no EOFException then the history file is just empty => normal behavior.
        }
        catch (Exception e) 
        {
            // If no EOFException then the history file is empty.
            System.err.println("Error: cannot retrieve messages in the history file."); 
        }
    }   

    public void saveMessageHistory()
    {
        try
        {
            // Open a stream to the file.
            ObjectOutputStream stream = new ObjectOutputStream(
                    new FileOutputStream(HISTORY_FILE_PATH));
            // Then write the messages. 
            stream.writeObject(mLinker.getClientMessages()); 

            stream.close();
        }
        catch (Exception e) 
        {
            System.err.println("Error: cannot save messages in the history file."); 
        }
    }

    private void createHomeDir()
    {
        File homeDir = new File(HOME_DIR_PATH);

        try
        {
            if (! homeDir.exists() && ! homeDir.mkdirs())
            {
                System.err.println("Error: cannot create the crowdchat home directory " +
                        HOME_DIR_PATH + ".");
                System.exit(-1);
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: cannot create the crowdchat home directory " + 
                    HOME_DIR_PATH + "."); 
            System.exit(-1);
        }
    }

    private void createHistoryFile()
    {
        File file = new File(HISTORY_FILE_PATH);

        try
        {
            if (! file.exists() && ! file.createNewFile())
            {
                System.err.println("Error: cannot create the history file " +
                        HISTORY_FILE_PATH + ".");
                System.exit(-1);
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: cannot create the history file " + 
                    HISTORY_FILE_PATH + "."); 
            System.exit(-1);
        }
    }
}
