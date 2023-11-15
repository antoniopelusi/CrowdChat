package crowdchat;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;


/**
 * The GUI bound with a "Client".
 */
public class Application
{
    public static void main(String[] args) 
    {   
        // Create the client.
        Client.BasicClient client = new Client.BasicClient(parseArgs(args));
        // Start the app with this client.
        SwingUtilities.invokeLater(() -> new Application(client));
        // Handle the ctrl-C exits (alt-F4 done with Swing).
        Runtime.getRuntime().addShutdownHook(new Thread(
                () ->
                {
                    if (client.isConnected())
                    {
                        client.disconnect();
                    }       
                }
            )
        );
    }

    public static String parseArgs(String[] args)
    {
        if (args.length < 1) 
        {
            return "localhost";
        }

        return args[0];
    }


    // Constants.
    private final String FONT = "";

    public final static SimpleAttributeSet ATTR_PLAIN 
        = new SimpleAttributeSet(); 
    public final static SimpleAttributeSet ATTR_BOLD 
        = new SimpleAttributeSet(); 
    public final static SimpleAttributeSet ATTR_ITALIC 
        = new SimpleAttributeSet(); 
    public final static SimpleAttributeSet ATTR_ERROR 
        = new SimpleAttributeSet(); 
    public final static SimpleAttributeSet ATTR_SERVER 
        = new SimpleAttributeSet(); 


    // To manage the client session and messages. 
    private final Client.BasicClient mClient;
    // App icon.
    private BufferedImage mIcon;
    // The GUI window.
    private JFrame mFrame;
    // The chat messages.
    private JTextPane mChatArea;
    // The connected user names.
    private DefaultListModel<String> mUserList;

    public Application(Client.BasicClient client)
    {
        // Load text styles for the chat.
        loadTextStyles();
        // Load the app icon.
        loadAssets();
        // Set pop up dialogs style.
        setDialogs();
        // Load the window.
        createFrame();
        // Load the client.
        mClient = client;
        mClient.bindWithGUI(this);
    }

    private void loadTextStyles()
    {
        // Bold.
        StyleConstants.setBold(ATTR_BOLD, true);
        StyleConstants.setForeground(ATTR_BOLD, new Color(140, 140, 140));
        StyleConstants.setFontSize(ATTR_BOLD, (int) convertFontSizeForWindows(20D));
        // Italic.
        StyleConstants.setItalic(ATTR_ITALIC, true);
        StyleConstants.setForeground(ATTR_ITALIC, new Color(150, 150, 150));
        StyleConstants.setFontSize(ATTR_ITALIC, (int) convertFontSizeForWindows(20D));
        // Error.
        StyleConstants.setBold(ATTR_ERROR, true);
        StyleConstants.setForeground(ATTR_ERROR, new Color(110, 25, 25));
        StyleConstants.setFontSize(ATTR_ERROR, (int) convertFontSizeForWindows(20D));
        // Server.
        StyleConstants.setBold(ATTR_SERVER, true);
        StyleConstants.setForeground(ATTR_SERVER, new Color(230, 90, 90));
        StyleConstants.setFontSize(ATTR_SERVER, (int) convertFontSizeForWindows(20D));

    }

    private void loadAssets()
    {
        try
        {
            // Read from jar.
            mIcon = ImageIO.read(getClass().getResource("/assets/launcher.png"));
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    private void setDialogs()
    {
        UIManager.put("OptionPane.messageFont", new Font(FONT, Font.BOLD, (int) convertFontSizeForWindows(30D)));
        UIManager.put("OptionPane.buttonFont", new Font(FONT, Font.PLAIN, (int) convertFontSizeForWindows(20D)));
        UIManager.put("TextField.font", new Font(FONT, Font.PLAIN, (int) convertFontSizeForWindows(20D)));
        UIManager.put("Panel.background", new Color(30, 30, 30));
        UIManager.put("Label.foreground", new Color(225, 225, 225));
        UIManager.put("Button.background", new Color(60, 60, 60));
        UIManager.put("Button.foreground", new Color(225, 225, 225));
        UIManager.put("Button.border", new EmptyBorder(5, 5, 5, 5));
        UIManager.put("TextField.background", new Color(20, 20, 20));
        UIManager.put("TextField.foreground", new Color(225, 225, 225));
        UIManager.put("TextField.border", new EmptyBorder(5, 5, 5, 5));
        UIManager.put("OptionPane.background",new ColorUIResource(30, 30, 30));
        UIManager.put("OptionPane.foreground",new ColorUIResource(225, 225, 225));
    }

    private void createFrame()
    {
        mFrame = new JFrame("CrowdChat");
        Container container = mFrame.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(getRightPanel(), BorderLayout.CENTER);
        container.add(getLeftPanel(), BorderLayout.WEST);


        // On exit.
        mFrame.addWindowListener(new java.awt.event.WindowAdapter() 
        {
            // Can't use lambda.
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) 
            {
                if (mClient.isConnected())
                {
                    mClient.disconnect();
                }

                System.exit(0);
            }
        });
        // App icon.
        mFrame.setIconImage(mIcon);
        // Auto-exit when closing app.
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Center app.
        mFrame.setLocationRelativeTo(null);
        // Fullscreen.
        mFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Default size.
        mFrame.pack();
        // Show it.
        mFrame.setVisible(true);
    }

    private JPanel getLeftPanel()
    {
        // Icon.
        GridBagConstraints constraints1 = new GridBagConstraints();
        constraints1.weightx = 0;
        constraints1.weighty = 0;
        constraints1.gridx = 0;
        constraints1.gridy = 0;
        // User names. 
        GridBagConstraints constraints2 = new GridBagConstraints();
        constraints2.weightx = 1;
        constraints2.weighty = 1;
        constraints2.gridx = 0;
        constraints2.gridy = 1;
        constraints2.fill = GridBagConstraints.BOTH;
        // Navigation buttons. 
        GridBagConstraints constraints3 = new GridBagConstraints();
        constraints3.weightx = 1;
        constraints3.weighty = 0;
        constraints3.gridx = 0;
        constraints3.gridy = 2;
        constraints3.fill = GridBagConstraints.HORIZONTAL;
        constraints3.anchor = GridBagConstraints.PAGE_END;

        JPanel leftPanel = new JPanel(new GridBagLayout());

        leftPanel.add(getIconPanel(), constraints1); 
        leftPanel.add(getUsersPanel(), constraints2); 
        leftPanel.add(getCommandsPanel(), constraints3); 

        return leftPanel;
    }

    private JPanel getRightPanel()
    {
        // Chat content panel.
        GridBagConstraints constraints1 = new GridBagConstraints();
        constraints1.weightx = 1;
        constraints1.weighty = 1;
        constraints1.gridx = 0;
        constraints1.gridy = 0;
        constraints1.fill = GridBagConstraints.BOTH;
        // Input user message panel.
        GridBagConstraints constraints2 = new GridBagConstraints();
        constraints2.weightx = 1;
        constraints2.weighty = 0;
        constraints2.gridx = 0;
        constraints2.gridy = 1;
        constraints2.fill = GridBagConstraints.HORIZONTAL;
        constraints2.anchor = GridBagConstraints.PAGE_END;

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.add(getChatPanel(), constraints1); 
        rightPanel.add(getInputPanel(), constraints2);

        return rightPanel;
    }

    /**
     * Return the panel in which the user messages are displayed.
     */
    private JPanel getChatPanel()
    {
        // Message list.
        mChatArea = new JTextPane();
        mChatArea.setMargin(new Insets(20, 20, 20, 20));
        mChatArea.setFont(new Font(FONT, Font.PLAIN, (int) convertFontSizeForWindows(20D)));
        mChatArea.setEditable(false);
        mChatArea.setBackground(new Color(20, 20, 20));
        mChatArea.setForeground(new Color(225, 225, 225));
        mChatArea.setBorder(BorderFactory.createLineBorder(new Color(20, 20, 20), 10));

        addToChat("Welcome on CrowdChat.\n" +
                "You can log in using the button at the bottom left.\n\n",
                ATTR_ITALIC); 

        JScrollPane scrollPane = new JScrollPane(mChatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(20, 20, 20), 10));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 30), 30));
        panel.add(scrollPane);

        return panel;
    }

    /**
     * Return the panel in which the user write her/his message.
     */
    private JPanel getInputPanel()
    {
        // User input.
        JTextField textField = new JTextField();
        textField.setMargin(new Insets(20, 20, 20, 20));
        textField.setFont(new Font(FONT, Font.PLAIN, (int) convertFontSizeForWindows(20D)));
        textField.addActionListener(onSendInput(textField));
        textField.setBackground(new Color(20, 20, 20));
        textField.setForeground(new Color(225, 225, 225));
        textField.setBorder(new EmptyBorder(10, 10, 10, 10));
        // Send button.
        JButton button = new JButton("SEND");
        button.setFont(new Font(FONT, Font.BOLD, (int) convertFontSizeForWindows(20D)));
        button.addActionListener(onSendInput(textField));
        button.setBackground(new Color(60, 60, 60));
        button.setForeground(new Color(225, 225, 225));
        button.setBorder(new EmptyBorder(10, 10, 10, 10));

        // User input
        GridBagConstraints constraints1 = new GridBagConstraints();
        constraints1.weightx = 0.9;
        constraints1.weighty = 0;
        constraints1.gridx = 0;
        constraints1.gridy = 0;
        constraints1.fill = GridBagConstraints.HORIZONTAL;
        // Send button. 
        GridBagConstraints constraints2 = new GridBagConstraints();
        constraints2.weightx = 0.1;
        constraints2.weighty = 0;
        constraints2.gridx = 1;
        constraints2.gridy = 0;
        constraints2.fill = GridBagConstraints.HORIZONTAL;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(40, 20, 40, 40));
        panel.add(textField, constraints1);
        panel.add(button, constraints2);

        return panel;
    }

    private ActionListener onSendInput(JTextField textField)
    {
        return e ->  
        {
            if (! mClient.isConnected())
            {
                addToChat("[Server]: Please log in to " +
                       "send messages.", ATTR_ERROR);  
                textField.setText("");
                return ;
            }

            String input = textField.getText();

            if (input != null && ! input.isEmpty())
            {
                if (! mClient.isConnected())
                {
                    mClient.connect(input);
                }
                else
                {
                    mClient.sendMessage(input);
                }

                textField.setText("");
            }
        };
    }

    /**
     * Return the panel which contains the app icon and title. 
     */
    private JPanel getIconPanel()
    {
        // App title.
        String APP_NAME = "CrowdChat";
        JLabel label1 = new JLabel(APP_NAME, SwingConstants.CENTER);
        label1.setFont(new Font(FONT, Font.BOLD, (int) convertFontSizeForWindows(50D)));
        label1.setForeground(new Color(250, 65, 65));
        JPanel panel_ = new JPanel(); // To force margins...
        panel_.setBorder(new EmptyBorder(40, 40, 80, 40));
        panel_.add(label1);
        // App icon
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(40, 40, 40, 20));
        panel.add(panel_, BorderLayout.NORTH);
        return panel;
    }

    /**
     * Return the panel in which the connected user names are displayed. 
     */
    private JPanel getUsersPanel()
    {
        // Title.
        JLabel label = new JLabel("Current Users", JLabel.CENTER);
        label.setFont(new Font(FONT, Font.BOLD, (int) convertFontSizeForWindows(30D)));
        JPanel panel_ = new JPanel(); // To force margins...
        panel_.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel_.add(label);

        // User names.
        mUserList = new DefaultListModel<>();
        JList<String> list = new JList<>(mUserList);
        list.setBorder(new EmptyBorder(40, 40, 40, 20));
        list.setFont(new Font(FONT, Font.PLAIN, (int) convertFontSizeForWindows(20D)));
        list.setVisibleRowCount(8);

        JScrollPane scrollPane = new JScrollPane(list);

        JPanel panel = new JPanel(new BorderLayout());
        list.setBackground(new Color(20, 20, 20));
        list.setForeground(new Color(225, 225, 225));
        scrollPane.setBackground(new Color(20, 20, 20));
        scrollPane.setForeground(new Color(225, 225, 225));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 30), 10));
        

        panel.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 30), 10));
        panel.add(panel_, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        
        return panel;
    }

    /**
     * Return the panel in which the command buttons (connect, disconnect, ect)
     * are available. 
     */
    private JPanel getCommandsPanel()
    {
        JButton button1 = new JButton("CONNECT");
        JButton button2 = new JButton("DISCONNECT");

        // Connect button.
        button1.setFont(new Font(FONT, Font.BOLD, (int) convertFontSizeForWindows(20D)));
        button1.addActionListener(onConnection(button1, button2));
        button1.setEnabled(true);
        button1.setBackground(new Color(60, 60, 60));
        button1.setForeground(new Color(225, 225, 225));
        button1.setBorder(new EmptyBorder(10, 10, 10, 10));
        // Disconnect button.
        button2.setFont(new Font(FONT, Font.BOLD, (int) convertFontSizeForWindows(20D)));
        button2.addActionListener(onDisconnection(button1, button2));
        button2.setEnabled(false);
        button2.setBackground(new Color(30, 30, 30));
        button2.setForeground(new Color(225, 225, 225));
        button2.setBorder(new EmptyBorder(10, 10, 10, 10));
        // Connect button.
        GridBagConstraints constraints1 = new GridBagConstraints();
        constraints1.weightx = 0.5;
        constraints1.weighty = 0;
        constraints1.gridx = 0;
        constraints1.gridy = 0;
        constraints1.fill = GridBagConstraints.HORIZONTAL;
        // Disconnect button.
        GridBagConstraints constraints2 = new GridBagConstraints();
        constraints2.weightx = 0.5;
        constraints2.weighty = 0;
        constraints2.gridx = 1;
        constraints2.gridy = 0;
        constraints2.fill = GridBagConstraints.HORIZONTAL;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 40, 40, 20));
        panel.add(button1, constraints1); 
        panel.add(button2, constraints2); 

        return panel;
    }

    private ActionListener onConnection(JButton connectButton, 
            JButton disconnectButton)
    {
        return e -> 
        {
            // Ask for user pseudo.
            // Build a pop up dialog:
            String name = JOptionPane.showInputDialog(mFrame, 
                    "", "Enter the username",
                    JOptionPane.PLAIN_MESSAGE);

            // Try to connect on server.
            if (name != null)
            {
                if (! name.isEmpty())
                {
                    if (mClient.connect(name))
                    {
                        connectButton.setEnabled(false);
                        disconnectButton.setEnabled(true);
                        connectButton.setBackground(new Color(30, 30, 30));
                        disconnectButton.setBackground(new Color(60, 60, 60));
                    }
                }
                else
                {
                    addToChat("[Server]: Error on connection, " +
                            "your name was empty.", ATTR_ERROR); 
                }
            }
        };
    }

    private ActionListener onDisconnection(JButton connectButton, 
            JButton disconnectButton)
    {
        return e -> 
        {
            // Disconnect on server.
            mClient.disconnect();
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            connectButton.setBackground(new Color(60, 60, 60));
            disconnectButton.setBackground(new Color(30, 30, 30));
        };
    }

    public void addToChat(String message, SimpleAttributeSet attributes) 
    {
        Document doc = mChatArea.getDocument();

        if (attributes == ATTR_ERROR || attributes == ATTR_SERVER)
        {
            message = "\n" + message + "\n\n";
        }
        else if (attributes == ATTR_PLAIN)
        {
            message = message + "\n";
        }

        try
        {
            doc.insertString(doc.getLength(), message, attributes);
        }
        catch (Exception ignored)
        {
        }

        mChatArea.setCaretPosition(doc.getLength());
    }

    public void addToUsersList(String name)
    {
        mUserList.addElement(name);
    }

    public void removeFromUserList(String name)
    {
        mUserList.removeElement(name);
    }

    public void clearUsersList()
    {
        mUserList.clear();
    }

    public double convertFontSizeForWindows(double fontSize)
    {
        // Are we running within a Windows platform?
        if (System.getProperty("os.name").toLowerCase().contains("windows"))
        {
            // Yes, so let's convert the font size to accommodate windows.
            double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
            double testedWidth = 1500D; // Tested windows platform.
            return fontSize / ((testedWidth / screenWidth) * 2D);
        }
        // No, just return the original font size.
        return fontSize;
    }
}
