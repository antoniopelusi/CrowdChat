package crowdchat;

import java.io.Serializable;

import java.util.ArrayList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.rmi.*; 


/**
 * Connect the server and all the client together by saving
 * the list of usernames currently used, and the messages sent.
 */
public interface Linker extends Remote 
{
    /**
     * Add the message from sender to the server history. The server will
     * return the date/time the message was sent (thus all the message dates
     * will be from the same source i.e. the server).
     */
    String addMessage(String sender, String message) throws RemoteException;

    /**
     * Return true if the user was correctly created on the server side,
     * Otherwise return false.
     * Reasons why the operation could not be successful:
     * - An user with the same name already exists.
     */
    boolean connect(String name) throws RemoteException;

    /**
     * Remove the client identified by name from the list of connected users.
     */
    void disconnect(String name) throws RemoteException;

    ArrayList<String> getClientNames() throws RemoteException;

    ArrayList<Message> getClientMessages() throws RemoteException;

    void setClientMessages(ArrayList<Message> messages) throws RemoteException;


    class BasicLinker implements Linker
    {
        private final ArrayList<String> mClientNames;
        private ArrayList<Message> mClientMessages;

        public BasicLinker()
        {
            mClientNames = new ArrayList<>();
            mClientMessages = new ArrayList<>();
        }

        @Override
        public String addMessage(String sender, String message) throws RemoteException
        {
            // Constants.
            String DATE_FORMAT = "HH:mm:ss";
            String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT));
            mClientMessages.add(new Message(time, sender, message));

            return time; 
        }

        @Override
        public boolean connect(String name) throws RemoteException 
        {
            if (mClientNames.contains(name))
            {
                return false; 
            }

            System.out.println("Client joining: " + name); 
            mClientNames.add(name);
            return true;
        }

        @Override
        public void disconnect(String name) throws RemoteException
        {
            System.out.println("Client exiting: " + name); 
            mClientNames.remove(name);
        }

        @Override
        public ArrayList<String> getClientNames() throws RemoteException
        {
            return mClientNames;
        }

        @Override
        public ArrayList<Message> getClientMessages() throws RemoteException
        {
            return mClientMessages;
        }

        @Override
        public void setClientMessages(ArrayList<Message> messages) throws RemoteException
        {
            mClientMessages = messages;
        }
    }


    class Message implements Serializable
    {
        private static final long serialVersionUID = 667363824879925614L;

        private final String mTime;
        private final String mSender;
        private final String mContent;

        private Message(String time, String sender, String content)
        {
            mTime = time;
            mSender = sender;
            mContent = content;
        }

        public String getTime()
        {
            return mTime;
        }

        public String getSender()
        {
            return mSender;
        }

        public String getContent()
        {
            return mContent;
        }
    }
}
