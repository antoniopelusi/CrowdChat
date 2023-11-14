package crowdchat;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


/**
 * Communicate with other clients by saving itself in the "RMI register", 
 * save states (messages and name) on the server with the "Linker",
 * and update the GUI. 
 */
public interface Client extends Remote
{
    /**
     * Display the message sent by "sender" at time "time". Called by other client.
     */
    void writeMessage(String time, String sender, String message) throws RemoteException;

    /**
     * Notify that the user "name" is disconnected. Called by other client.
     */
    void notifyDisconnected(String name) throws RemoteException;

    /**
     * Notify that the user "name" is connected. Called by other client.
     */
    void notifyConnected(String name) throws RemoteException;


    class BasicClient implements Client, Serializable
    {
        private static final long serialVersionUID = 4885573965833413193L;

        // Current user state.
        private boolean mIsConnected;
        private String mName;
        // Remoted objects.
        private Registry mRegistry;
        private Linker mLinker;
        // To print messages and connected users.
        private Application mApp; 

        public BasicClient(String host)
        {
            mIsConnected = false;
            // Get server objects.
            getRemotedObjects(host);
        }

        /**
         * Bind this client with the current running gui, to print the messages 
         * and connected users.
         */
        public void bindWithGUI(Application app)
        {
            mApp = app;
        }

        /**
         * Connect the user to the server, and return true if successful.
         */
        public boolean connect(String name)
        {
            mApp.addToChat("[Server]: Initiating your connection...",
                    Application.ATTR_SERVER); 

            try 
            {
                // Try to create the user with the pseudo on the server side.
                if (! mLinker.connect(name))
                {
                    mApp.addToChat("[Server]: Error, this pseudo is not available.", 
                            Application.ATTR_ERROR);
                    return false;
                }
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error with the server, try again or " + 
                        "relaunch the app.", Application.ATTR_ERROR);
                return false;
            } 

            try
            {
                // Add this client to the registry.
                Client this_stub = (Client) 
                    UnicastRemoteObject.exportObject(this, 0);
                mRegistry.rebind("rmi://client/" + name, this_stub); 

                mName = name;
                mIsConnected = true;
            }
            catch (Exception e)
            {
                mApp.addToChat("[Server]: Error with the server, try again or " +
                        "relaunch the app.", Application.ATTR_ERROR);
                return false;
            }

            spreadConnection();
            retrieveMessages();

            mApp.addToChat("[Server]: You are connected as \"" + mName + "\".",
                    Application.ATTR_SERVER); 

            return true;
        }

        /**
         * Disconnect the user of the server by releasing her/his pseudo.
         */
        public void disconnect()
        {
            mApp.addToChat("[Server]: Initiating your disconnection...",
                    Application.ATTR_SERVER); 

            try
            {
                // Try to unbind the user on the server side.
                mRegistry.unbind("rmi://client/" + mName);
                UnicastRemoteObject.unexportObject(this, true);
                mLinker.disconnect(mName);
                mIsConnected = false;
            }
            catch (Exception e)
            {
                mApp.addToChat("[Server]: Error, cannot completely disconnect you. " + 
                        "Your username may be unavailable until the server restarts.",
                        Application.ATTR_ERROR); 
            }

            spreadDisconnection();
            // Remove the connected users.
            mApp.clearUsersList();  

            mApp.addToChat("[Server]: Disconnection finished.",
                    Application.ATTR_SERVER); 
        }

        /**
         * Send the user message.
         */
        public void sendMessage(String message)
        {
            try 
            {
                // Save this message on the server.
                String time = mLinker.addMessage(mName, message);
                // Spread this message to every client (including herself/himself).
                mLinker.getClientNames().forEach(
                        s -> 
                        {
                            try 
                            {
                                Client client = (Client) mRegistry.lookup("rmi://client/" + s);
                                client.writeMessage(time, mName, message);
                            } 
                            catch (Exception e)  
                            {
                                mApp.addToChat("[Server]: Error, cannot distribute this message " +
                                        "to \"" + s + "\".", Application.ATTR_ERROR); 
                            }
                        }
                ) ;
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error, cannot distribute this message.",
                        Application.ATTR_ERROR); 
            }
        }

        /**
         * Load every remoted object reference from the server into memory.
         */
        private void getRemotedObjects(String host)
        {
            try 
            {
                mRegistry = LocateRegistry.getRegistry(host); 
                mLinker = (Linker) mRegistry.lookup("rmi://server/ConnectService");
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error with the server, please " + 
                        "relaunch the app.", Application.ATTR_ERROR);
                // Can't continue without them.
                System.exit(-1);
            }
        }

        /**
         * Spread the user connection to every other client (and herself/himself)
         * and populate the connected users list.
         */
        private void spreadConnection()
        {
            try 
            {
                mLinker.getClientNames().forEach(
                        s -> 
                        {
                            try 
                            {
                                // Notify.
                                Client client = (Client) mRegistry.lookup("rmi://client/" + s);
                                client.notifyConnected(mName);
                                // Populate.
                                if (! s.equals(mName))
                                {
                                    mApp.addToUsersList(s);
                                }
                            } 
                            catch (Exception e)  
                            {
                                mApp.addToChat("[Server]: Error, cannot notify your connection " +
                                        "to \"" + s + "\".", Application.ATTR_ERROR); 
                            }
                        }
                ) ;
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error, cannot notify your connection.",
                        Application.ATTR_ERROR); 
            }
        }


        /**
         * Spread the user disconnection to every other client (and herself/himself)
         * and populate the connected users list.
         */
        private void spreadDisconnection()
        {
            try 
            {
                mLinker.getClientNames().forEach(
                        s -> 
                        {
                            try 
                            {
                                Client client = (Client) mRegistry.lookup("rmi://client/" + s);
                                client.notifyDisconnected(mName);
                            } 
                            catch (Exception e)  
                            {
                                mApp.addToChat("[Server]: Error, cannot notify your disconnection " +
                                        "to \"" + s + "\".", Application.ATTR_ERROR); 
                            }
                        }
                ) ;

                notifyDisconnected(mName);
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error, cannot notify your disconnection.",
                        Application.ATTR_ERROR); 
            }
        }

        /**
         * Fetch the message history.
         */
        private void retrieveMessages()
        {
            mApp.addToChat("[Server]: Recovering message history...",
                    Application.ATTR_SERVER); 

            try 
            {
                mLinker.getClientMessages().forEach(
                        m -> 
                        {
                            try
                            {
                                writeMessage(m.getTime(), m.getSender(), m.getContent());
                            }
                            catch (Exception e)
                            {
                                mApp.addToChat("[Server]: Error, cannot retrieve a message " +
                                       "from the history.",
                                        Application.ATTR_ERROR); 
                            }
                        }
                );
            } 
            catch (Exception e)  
            {
                mApp.addToChat("[Server]: Error, cannot retrieve message history.",
                        Application.ATTR_ERROR); 
            }
        }

        @Override
        public void writeMessage(String time, String sender, String message) throws RemoteException
        {
            mApp.addToChat("(" + time + ") ", Application.ATTR_BOLD);
            mApp.addToChat(sender + ": ", Application.ATTR_BOLD);
            mApp.addToChat(message, Application.ATTR_PLAIN);
        }

        @Override
        public void notifyDisconnected(String name) throws RemoteException
        {
            mApp.removeFromUserList(name);

            if (! name.equals(mName))
            {
                mApp.addToChat(name + " is disconnected.", Application.ATTR_SERVER);
            }
        }

        @Override
        public void notifyConnected(String name) throws RemoteException
        {
            if (! name.equals(mName))
            {
                mApp.addToChat(name + " is connected.", Application.ATTR_SERVER);
            }

            mApp.addToUsersList(name);
        }

        public boolean isConnected()
        {
            return mIsConnected;        
        }
    }
}
