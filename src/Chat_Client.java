//Student Name:		Adam Fallon
//Student Number: 	40080046
//Module Code:		CSC 2008
//Practical Day:	Friday
//Email:			afallon02@qub.ac.uk

import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.text.*;
 
public class Chat_Client extends JFrame
{   // socket to communicate with server
    Socket socket;
    // input stream - data sent by the server will be read from this stream
    ObjectInputStream clientInputStream;
    // output stream - data sent to the server will be written to this stream
    ObjectOutputStream clientOutputStream;
     
    // variables for the GUI components of the game
    Container c;
    ButtonHandler bHandler, spHandler, saveHandler;
    NameButtonHandler nbHandler;
    ImageButtonHandler ibHandler;
    JButton sendButton, logonButton,saveChatlog;
    JButton[] nameButtons, imageButtons;
    ImageIcon[] images = {new ImageIcon("smile.gif"), new ImageIcon("frown.gif"), new ImageIcon("wink.gif"), new ImageIcon("eek.gif"), new ImageIcon("confused.gif"), new ImageIcon("embarrassed.gif"), new ImageIcon("stop.gif")};
    JPasswordField password;
    JTextField username;
    JTextPane outputArea,inputArea;
    StyledDocument docInputArea, docOutputArea;
    Style style;
    JPanel namesPanel, imageButtonsPanel, nameButtonsPanel, sendButtonPanel, inputAreaPanel, logonFieldsPanel, logonButtonPanel, leftPanel, rightPanel, cCenterPanel, lowerPanel, outputAreaPanel;
    JLabel namesLabel, usernameLabel, passwordLabel, imageLabel;
     
    String [] names = {"Arken", "Ben", "Darklark", "Free", "Group"};
    //boolean[] loggedOn = new boolean[5];
    boolean[] loggedOn = {false, false, false, false, true};
    String recipients = "";
    String plainTextPW = "";
    String message;
 
    public Chat_Client()
    {   super("Chat_Client");
        addWindowListener
        (   new WindowAdapter()
            {   @Override
			public void windowClosing(WindowEvent e)
                {   System.exit(0);
                }
            }
        );
 
        /* the initial GUI will provide a text field and password field
           to enable the user to enter their username and password and
           attempt to logon to the game system */
 
        // create and add GUI components
        c = getContentPane();
        c.setLayout(new BorderLayout());
 
        // GUI components for the username
        logonFieldsPanel = new JPanel();
        logonFieldsPanel.setLayout(new GridLayout(2,2,5,5));
        usernameLabel = new JLabel("Enter Username: ");
        logonFieldsPanel.add(usernameLabel);
        username = new JTextField(10);
        logonFieldsPanel.add(username);
 
        // GUI components for the password
        passwordLabel = new JLabel("Enter Password: ");
        logonFieldsPanel.add(passwordLabel);
        password = new JPasswordField(10);
        logonFieldsPanel.add(password);
        c.add(logonFieldsPanel,BorderLayout.CENTER);
 
        // panel for the logon button
        logonButtonPanel = new JPanel();
        logonButton = new JButton("logon");
        
      
        bHandler = new ButtonHandler();
        logonButton.addActionListener(bHandler);
        logonButtonPanel.add(logonButton);
        c.add(logonButtonPanel, BorderLayout.SOUTH);
 
        setSize(300,125);
        setLocation(400,350);
        setResizable(false);
        setVisible(true);
    }
    void setUpChatClient(boolean chatting)
    {   // remove iniial GUI components (textfield, password field, logon button)
        c.remove(logonButtonPanel);
        c.remove(logonFieldsPanel);
         
        if(!chatting)
            // if the user has not logged on an error message will be displayed
            c.add(new JTextArea("Logon unsuccessful"));
        else
        {   // if the user has logged on the message service GUI will be set up
            c.setLayout(new BorderLayout());
            leftPanel = new JPanel(new GridLayout(2,1));
            leftPanel.setBackground(Color.WHITE);
            rightPanel = new JPanel(new GridLayout(2,1));
             
            imageLabel = new JLabel(new ImageIcon("people1.jpg"));
            imageLabel.setBackground(Color.WHITE);
            leftPanel.add(imageLabel);
             
            // name buttons enable user to choose message recipient(s)
            nameButtonsPanel = new JPanel(new GridLayout(5,1));
            nameButtons = new JButton[names.length];
            nbHandler = new NameButtonHandler();
            for(int r = 0; r < nameButtons.length; r++)
            {   nameButtons[r] = new JButton(names[r]);
                nameButtons[r].addActionListener(nbHandler);
                nameButtons[r].setEnabled(loggedOn[r]);
                nameButtonsPanel.add(nameButtons[r]);
            }
            leftPanel.add(nameButtonsPanel);
              
            outputAreaPanel = new JPanel();
            outputAreaPanel.setBackground(Color.WHITE);
            // messages from the server will be displayed in this JTextPane
            outputArea = new JTextPane();
            outputArea.setEditable(false);
            Dimension d = new Dimension(300,150);
            outputArea.setPreferredSize(d);
            docOutputArea = (StyledDocument) outputArea.getDocument();
            style = docOutputArea.addStyle("StyleName", null);
            JScrollPane outputScrollPane = new JScrollPane(outputArea);
            outputAreaPanel.add(outputScrollPane);
            rightPanel.add(outputAreaPanel);
 
            inputAreaPanel = new JPanel();
            inputAreaPanel.setBackground(Color.WHITE);        

            // image buttons enable user to add an image to a text message
            imageButtonsPanel = new JPanel();
            imageButtonsPanel.setBackground(Color.WHITE);
            d = new Dimension(25,25);
            ibHandler = new ImageButtonHandler();
            imageButtons = new JButton[images.length];
            for(int j = 0; j < imageButtons.length; j++)
            {   imageButtons[j] = new JButton(images[j]);
                imageButtons[j].setPreferredSize(d);
                imageButtons[j].setBorderPainted(false);
                imageButtons[j].addActionListener(ibHandler);
                imageButtonsPanel.add(imageButtons[j]);
            }
            inputAreaPanel.add(imageButtonsPanel);
 
            d = new Dimension(300,60);
            // text messages will be entered into this JTextPane
            inputArea = new JTextPane();
            inputArea.setPreferredSize(d);
            docInputArea = (StyledDocument) inputArea.getDocument();
            style = docInputArea.addStyle("StyleName", null);
            JScrollPane scrollPane = new JScrollPane(inputArea);
            inputAreaPanel.add(scrollPane);
             
            // the send button enables user to send a text message
            sendButtonPanel = new JPanel();
            sendButtonPanel.setBackground(Color.WHITE);
            bHandler = new ButtonHandler();
            sendButton = new JButton("send");
            sendButton.addActionListener(bHandler);
            sendButtonPanel.add(sendButton);
            inputAreaPanel.add(sendButtonPanel);
            rightPanel.add(inputAreaPanel);
            
            //Save chat log
            saveChatlog = new JButton("Download Chat Log");
            saveHandler = new ButtonHandler();
            saveChatlog.addActionListener(saveHandler);
            sendButtonPanel.add(saveChatlog);
 
            c.add(rightPanel, BorderLayout.CENTER);
            c.add(leftPanel, BorderLayout.WEST);
            setSize(425, 375);
            setLocation(400,200);
        }
        setResizable(false);
        setVisible(true);
    }
    public static synchronized void play(final String fileName) 
    {
        new Thread(new Runnable() { 
            @Override
			public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(fileName));
                    clip.open(inputStream);
                    clip.start(); 
                } catch (Exception e) {
                    System.out.println("play sound error: " + e.getMessage() + " for " + fileName);
                }
            }
        }).start();
    }
    void changeNameButton(int i, Color c)
    {   /* change the colour of the text on a name 
           button - red indicates that this friend
           is a recipient of next message */
        nameButtons[i].setForeground(c);
    }
 
    void changeNameButtons(Color c)
    {   /* change the colour of the text on all the 
           name buttons */
        for(int r = 0; r < nameButtons.length; r++)
           changeNameButton(r, c);
    }
    void changeNameButtons()
    {   /* disable or enable each name button - a
           button is enabled if that friend is online,
           otherwise it is disabled */
        for(int i = 0; i < loggedOn.length; i++)
        {
         
                if(loggedOn[i] == true)
                {
                    nameButtons[i].setEnabled(true);
                }
                else
                    loggedOn[i] = false;
        }
    }
    void changeFriends(String n, boolean b)
    {   // change a friend's "online" status
        for(int i = 0; i < names.length; i++)
        {
            //If the value at loggedIn[i] is equal to true set the boolean to true, else set it to false
            if(n.equals(names[i])) 
            {
            	System.out.println(i);
            	loggedOn[i] = b;
            }
        }
 
        // call method to update buttons
        changeNameButtons();
    }
    
    void addOutput(String str)
    {   /* this will split the message string into words using the space
           character as a delimiter, the words will be stored in consecutive 
           elements of array "words" */
        String[] words = str.split(" \\s*");
        try
        {   // travese array, taking each word in turn
            for(int i = 0; i < words.length; i++)
            {   /* if the first character of this word is $ this indicates that
                    this string represents an image in the text message */
                if(words[i].charAt(0) == '$')
                {   /* the remainder of this word will be a number indicating the 
                       array element in which the image is stored - retrieve this 
                       number from the string */
                    String position = words[i].substring(1, words[i].length());                 
                    int pos = Integer.parseInt(position);
                    // retrieve the appropriate image from the array
                    StyleConstants.setIcon(style, images[pos]);
                    // add the image to the text output area
                    docOutputArea.insertString(docOutputArea.getLength(), " $" + pos + " ", style);
                }
                else
                    // otherwise add the next text word to the text output area
                    docOutputArea.insertString(docOutputArea.getLength(), words[i] + " ", null);
            }
            // add a newline character to the text output area
            docOutputArea.insertString(docOutputArea.getLength(), " \n", null);
            // set the caret position in the text output area
            outputArea.setCaretPosition(docOutputArea.getLength());
        }
        catch(BadLocationException ee)
        {   System.out.println(ee);
            System.exit(1);
        }
    }
     
    void closeChatClient()
    {   // user has quit the message service - disable GUI components
        // disable send message button
        sendButton.setEnabled(false);
        // disable all name buttons
        for(int i = 0; i < names.length; i++)
        {
            nameButtons[i].setEnabled(false);
            loggedOn[i] = false;
        }
        // disable all image buttons
        for(int i = 0; i < imageButtons.length; i++ )
        {
            imageButtons[i].setEnabled(false);
        }
        // set input area to prevent text entry
        inputArea.setEditable(false);
    }
 
    void sendLoginDetails()
    {   try
        {   // get username from text field and encrypt
            EncryptedMessage uname = new EncryptedMessage(username.getText());
            uname.encrypt();
            
            // get password from password field and encrypt     
            EncryptedMessage pword = new EncryptedMessage(new String(password.getPassword()));
            pword.encrypt();
             
            // send encrypted username to server
            clientOutputStream.writeObject(uname);
            // send encrypted password to server
            clientOutputStream.writeObject(pword);
             
        }
        catch(IOException e) // thrown by methods writeObject
        {   System.out.println(e);
            System.exit(1);
        }
    }
 
    void getConnections()
    {   try
        {   // initialise a socket and get a connection to server
            socket = new Socket(InetAddress.getLocalHost(), 7500);
            // get input & output object streams
             
            // get output stream
            clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
             
            // get input stream
            clientInputStream = new ObjectInputStream(socket.getInputStream());
             
 
            /* create a new thread of Chat_ClientThread, sending input
               stream variable as a parameter */
            Chat_ClientThread thread = new Chat_ClientThread(clientInputStream);
            // start thread - execution will begin at method run
            thread.start();
        }
        catch(UnknownHostException e) // thrown by method getLocalHost
        {   System.out.println(e);
            System.exit(1);
        }
        catch(IOException e) // thrown by methods ObjectOutputStream, ObjectInputStream
        {   System.out.println(e);
            System.exit(1);
        }
    }
    void sendMessage(String str)
    {   try
        {   /* if you have not chosen any recipients this message will be 
               sent to all friends by default */
            if(recipients.equals(""))
                recipients = names[names.length - 1] + ",";
 
            // separate recipients and the message by inserting # character  
            str = recipients + "#" + str;
            // compress message
            CompressedMessage cm = new CompressedMessage(str);
            cm.compress();
            // send message to server
            clientOutputStream.writeObject(cm);
            // clear recipients for next message
            recipients = "";
            // clear the input area
            inputArea.setText("");
            // change the colour of text on name buttons of friends online
            changeNameButtons(Color.BLACK);
        }
        catch(IOException e) // thrown by method writeObject
        {   System.out.println(e);
            System.exit(1);
        }
    }
    void SaveLog(){
        BufferedWriter writer = null;
        try {
            //create a temporary file
            String stamp= new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            File log= new File(stamp + ".txt");

            // This will output the full path where the file will be written to...
            System.out.println(log.getCanonicalPath());

            writer = new BufferedWriter(new FileWriter(log));
            writer.write(outputArea.getText());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
    }
    void closeStreams()
    {           
        try{
        //close Streams
        clientInputStream.close();
        clientOutputStream.close();
         
        //close socket
        socket.close();
        }
        catch(IOException e) // thrown by method close
        {   System.out.println(e);
            System.exit(1);
        }
    }
 
    public static void main(String args[])
    {   Chat_Client gameClient = new Chat_Client();
        gameClient.getConnections();
    }
 
 
    private class Chat_ClientThread extends Thread
    {   ObjectInputStream threadInputStream;
 
        public Chat_ClientThread(ObjectInputStream in)
        {   // initialise input stream
            threadInputStream = in;
        }
 
        // when method start() is called thread execution will begin in this method
        @Override
		public void run()
        {   try
            {   /* read Boolean value sent by server - it is converted to
                   a primitive boolean value */
                boolean chatting = (Boolean)threadInputStream.readObject();
                // call method to change the client GUI
                setUpChatClient(chatting);
                if(!chatting)
                    // call method to close input & output streams & socket
                    closeStreams();
                else
                {   // this loop will continue until this client quits the chat service
                    while(chatting)
                    {   // read next compressed message from server
                        CompressedMessage nextMessage1 = (CompressedMessage)clientInputStream.readObject();
                        // decompressed message
                        nextMessage1.decompress();
                        // retrieve decompressed message
                        //clientInputStream.readObject();
                        String message = nextMessage1.getMessage();
                        // if this client has quit the server will send this last message
                        System.out.println(message);
                        if(message.equals("goodbye"))
                        {   // chatClient should be closed
                             
                            closeChatClient();
                            closeStreams();
                            chatting = false; 
                        }
                             
                        else
                        {   if(message.substring(0,4).equals("join"))
                            {   /* if the first word in the message is "join" then another friend
                                   has joined the message service, retrieve the name of friend
                                   and enable their name button in GUI */
                                System.out.println(message+" "+message.length()+" "+message.substring(4,message.length()));
                                for(int i = 0; i < loggedOn.length;i++)
                                {
                                	
                                	if(message.substring(4,message.length()-1).equalsIgnoreCase(names[i]))
                                	{
                                		loggedOn[i]=true;
                                		nameButtons[i].setEnabled(true);

                                	}
                                }
                                
                                
                                // output message in output area
                                addOutput(message.substring(4,message.length()) + " has joined");
                                
                            }
                            else
                            {   if(message.substring(0,4).equals("quit "))
                                {   /* if the first word in the message is "quit" then a friend
                                       has quit the message service, retrieve the name of friend
                                       and disable their name button in GUI */
                                    changeFriends(message.substring(4,message.length()), false);
                                    // output message in output area
                                    addOutput(message.substring(4,message.length()) + " has quit");
                                    for(int i = 0; i < loggedOn.length;i++)
                                    {
                                    	
                                    	if(message.substring(4,message.length()-1).equalsIgnoreCase(names[i]))
                                    	{
                                    		loggedOn[i]=true;
                                    		nameButtons[i].setEnabled(true);

                                    	}
                                    }
                                    
                                }
                                else
                                {   if(message.substring(0,6).equals("online"))
                                    {   /* if the first word in the message is "online" then this client
                                           has just joined the chat service and this message lists
                                           the names of all other friends that are online */
                                        // split string to separate names of friends online
                                        String[] online = message.substring(6,message.length()).split(",\\s*");
                                        if(!online[0].equals("none"))
                                        {   for(int i = 0; i < online.length; i++)
                                                changeFriends(online[i], true);
                                        }
                                        // output message in output area
                                        addOutput("Your friends online : " + message.substring(6,message.length()-1));
                                    }
                                    else
                                        // output message in output area
                                        addOutput(message);
                                
                                		//Notifications
                                		String notification = "New Message from ";                         		
                                	    JFrame frame2 = new JFrame("TitleLessJFrame");
                                	    frame2.getContentPane().add(new JLabel("<HtMl>"+notification + "\n\n"+ message));
                                	    frame2.setUndecorated(true);
                                	    frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                                	    frame2.setSize(400, 50);
                                	    frame2.setVisible(true);
                                } // end else
                            } // end else
                        } // end else
                    } // end while
                } // end else
            } // end try
            catch(IOException e) // thrown by method readObject
            {   System.out.println(e);
                System.exit(1);
            }
            catch(ClassNotFoundException e) // thrown by method readObject
            {   System.out.println(e);
                System.exit(1);
            }
        } // end method run
    } // end of class Chat_ClientThread
 
    private class NameButtonHandler implements ActionListener
    {   // if any of the name buttons are clicked execution will continue in this method
        @Override
		public void actionPerformed(ActionEvent e)
        {   int pos = -1;
            // loop to identify which of the name buttons were clicked
            for(int r = 0; r < nameButtons.length; r++)
            {   if(e.getSource() == nameButtons[r])
                    pos = r;
            }
            // add this friend's name to recipients list
            recipients += names[pos] + ",";
            if(pos == names.length - 1)
               /* you have chosen to send the message to all 
                  friends - change the colour of all the name buttons */
               changeNameButtons(Color.RED);
            else
                /* you have chosen to send the message to an individual 
                   friend - change the colour of this friends name button */
                changeNameButton(pos, Color.RED);
            }
    }  // end of class NameButtonHandler
 
    private class ImageButtonHandler implements ActionListener
    {   // if any of the image buttons are clicked execution will continue in this method
        @Override
		public void actionPerformed(ActionEvent e)
        {   int pos = -1; 
            // loop to identify which of the buttons were clicked
            for(int r = 0; r < imageButtons.length; r++)
            {   if(e.getSource() == imageButtons[r])
                    pos = r;
            }
            try
            {   // retrieve the appropriate image from the array
                StyleConstants.setIcon(style, images[pos]);
                // add the image to the text input area
                docInputArea.insertString(docInputArea.getLength(), " $" + pos + " ", style);
            }
            catch(BadLocationException ee)
            {   System.out.println(ee);
                System.exit(1);
            }
        }
    }  // end of class ImageButtonHandler
 
    private class ButtonHandler implements ActionListener
    {   // if the logon or send buttons are clicked execution will continue in this method
        @Override
		public void actionPerformed(ActionEvent e)
        {   if(e.getSource() == logonButton)
                /* if the logon button is clicked call method to 
                   send the login details to the server */
                sendLoginDetails();
        else if(e.getSource() == saveChatlog)
        {
            //Will save chat log
        	SaveLog();
        	addOutput("Log Saved to workspace");
           
        }
        else
            {   if(e.getSource() == sendButton)
                    /* if the send button is clicked call method to 
                       send the message to the server */
                    sendMessage(inputArea.getText());
            		play("ping.wav");
            }
        }
    }  // end of class ButtonHandler
} // end of class Chat_Client

