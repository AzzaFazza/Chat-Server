//Student Name:		Adam Fallon
//Student Number: 	40080046
//Module Code:		CSC 2008
//Practical Day:	Friday
//Email:			afallon02@qub.ac.uk

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
 
@SuppressWarnings("serial")
public class Chat_Server extends JFrame
{   // total number of clients that can be logged on to the message service
    final int NO_OF_CLIENTS = 4; 
     
    // this ArrayList will store the client threads
    ArrayList<Chat_ServerThread> chatClients = new ArrayList<Chat_ServerThread>();
     
    // arrays containing valid names and passwords
    String rules = "Welcome to the CSC2008 Chat Server. Only users who are older than your country’s age of maturity have permission to use the service. All interactions between clients are monitored and recorded on this server. Any messages containing defamatory content will be referred to the authorities. You have been warned! Questions? Contact us at no.one@cares.com. Please enjoy and remember there is a world outside without chat clients and servers… ";
    String passwords[] = new String[5];
    String[] names = new String[5];
    // array containing valid chat tags
    String [] chattag = new String[5];
     
    // this array indicates whether a client is currently logged on
    boolean loggedOn[] = new boolean[NO_OF_CLIENTS];
 
    // GUI components
    JTextArea outputArea;
     
    // any other declaration
    ServerSocket serverSocket;
    DataInputStream serverInputStream;
    DataOutputStream serverOutputStream;    
    
    public Chat_Server()
    {   super("Chat_Server");
        addWindowListener
        (   new WindowAdapter()
            {   @Override
			public void windowClosing(WindowEvent e)
                {   System.exit(0);
                }
            }
        );
 
        try
        {   // get a serversocket
            serverSocket = new ServerSocket(7500);  
            
            
            //Read the usernames into an array from textfile
            String file = "usernames.txt";
            byte[] buffer = new byte[(int)new File(file).length()];  
            FileInputStream fis = new FileInputStream(file);  
            try {  
                fis.read(buffer);  
                names = new String(buffer).split(",");   
                for(int i = 0; i < names.length; i++)
                {
                    System.out.println(names[i]);
                } 
            } finally {  
                fis.close();  
            } 
            
            //now read passwords in
            String file2 = "passwords.txt";
            byte[] buffer2 = new byte[(int)new File(file2).length()];  
            FileInputStream fis2 = new FileInputStream(file2);  
            try {  
                fis2.read(buffer2);  
                passwords = new String(buffer2).split(",");   
                for(int i = 0; i < passwords.length; i++)
                {
                    System.out.println(passwords[i]);
                } 
            } finally {  
                fis2.close();  
            } 
            
            //Finally the chattags
            String file3 = "chattags.txt";
            byte[] buffer3 = new byte[(int)new File(file3).length()];  
            FileInputStream fis3 = new FileInputStream(file3);  
            try {  
                fis3.read(buffer3);  
                chattag = new String(buffer3).split(",");   
                for(int i = 0; i < chattag.length; i++)
                {
                    System.out.println(chattag[i]);
                } 
            } finally {  
                fis2.close();  
            } 
        }
        
        catch(IOException e) // thrown by ServerSocket
        {   System.out.println(e);
            System.exit(1);
        }
 
        // create and add GUI components
        Container c = getContentPane();
        c.setLayout(new FlowLayout());
        // add text output area
        outputArea = new JTextArea(17,30);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        c.add(outputArea);
        c.add(new JScrollPane(outputArea));
        setSize(360,310);
        setLocation(400,200);
        setResizable(false);
        setVisible(true);
    }
 
    void getClients()
    {   // add message to server output area
        addOutput("Chat Sever is up and waiting for a connection...");
        int userCount = 0;
        while(userCount < NO_OF_CLIENTS)
        {   try
            {   /* client has attempted to get a connection to server,
                   create a socket to communicate with this client */
                Socket client = serverSocket.accept();
 
                // get input & output streams
                ObjectInputStream serverInputStream = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream serverOutputStream = new ObjectOutputStream(client.getOutputStream());
                     
                // add message to server output area
                addOutput("Got a connection request from client");
                
                // read encrypted username from input stream & decrypt
                EncryptedMessage uname = (EncryptedMessage)serverInputStream.readObject();
                uname.decrypt();
                // read encrypted password from input stream & decrypt
                EncryptedMessage pword = (EncryptedMessage)serverInputStream.readObject();
                pword.decrypt();
                 
                // add messages to server output area
                addOutput("decrypted username : " + uname.getMessage());
                addOutput("decrypted password : " + pword.getMessage());
                 
                String un = uname.getMessage();
                String p = pword.getMessage();
                boolean valid = false;
                 
                int pos = -1;
                 // Arrays.binarySearch(names, u);
                for(int i = 0; i < names.length; ++i)
                {
                     
                    if(un.equals(names[i]) && p.equals(passwords[i]) && !loggedOn[i])
                    {
                        valid = true;
                        pos = i;
                    }                    
                }
                 
                addOutput("valid = " + valid + ", position = " + pos);
                 
                if(valid)
                {   if(passwords[pos].equals(p) && !loggedOn[pos])
                    {   addOutput("Login details received from client " + (userCount+1) + ", " + un + " are valid");
                        addOutput("Client " + un + " is known as " + chattag[pos]);
                        valid = true;
                        loggedOn[pos] = true;
                        // send Boolean value  true to client
                        serverOutputStream.writeObject(new Boolean(true));
                        //serverOutputStream.writeObject(u);
                         
                             
                        // add this new thread to the array list
                        Chat_ServerThread chatClient = new Chat_ServerThread (serverInputStream, serverOutputStream, uname.getMessage(), chattag[pos]);
                        chatClients.add(chatClient);
                             
                        // start thread - execution of the thread will begin at method run
                        chatClient.start();
                             
                        userCount++;
                    }
                }
                else
                {   /* user is not registered therefore write a Boolean value
                       false to the output stream */
                    serverOutputStream.writeObject(new Boolean(false));
                    addOutput("Login details received from client " + (userCount+1) + ", " + uname + " are invalid");
                }
            }
            catch(ClassNotFoundException e) // thrown by method readObject
            {   System.out.println(e);
                System.exit(1);
            }
            catch(IOException e) // thrown by Socket
            {   System.out.println(e);
                System.exit(1);
            }
        }
    }
 
    void addOutput(String s)
    {   // add message to text output area
        outputArea.append(s + "\n");
        outputArea.setCaretPosition(outputArea.getText().length());
    }
 
    // main method of class Chat_Server
    public static void main(String args[])
    {   Chat_Server chatServer = new Chat_Server();
        chatServer.getClients();
    }
 
    // beginning of class Chat_ServerThread
    private class Chat_ServerThread extends Thread
    {   // What to declare?
        ObjectInputStream threadInputStream;
        ObjectOutputStream threadOutputStream;
        String clientName;
        String chatName; 
             
 
        public Chat_ServerThread(ObjectInputStream in, ObjectOutputStream out, String cname, String ctName)
        {   // initialise input stream
            threadInputStream = in;
             
            // initialise output stream
            threadOutputStream = out;
             
            // initialise user name
            clientName = cname;
             
            // initialise chat name
            chatName = ctName;
        }
 
        CompressedMessage getCompressedMessage(String str)
        {   // create & return a compressed message
            CompressedMessage message = new CompressedMessage(str);
            message.compress();
             
return message;
        }
 
        // when method start() is called thread execution will begin in this method
        @Override
		public void run()
        {   try
            {   //send greeting to this client
                threadOutputStream.writeObject((getCompressedMessage("Welcome to the chat server " + clientName)));
                // inform this client of other clients online
                threadOutputStream.writeObject(getCompressedMessage("online" + getChatClients()));
                //sendMessage("online"+getChatClients());
                // output to server window
                addOutput(clientName + " known as " + chatName + " has joined");
                // inform other clients that this client has joined
                sendMessage("join" + chatName);
 
                boolean quit = false, broadcast = false;
                // this loop will continue until the client quits the chat service
                while(!quit)
                {   // read next compressed message from client
                    CompressedMessage nextMessage = (CompressedMessage)threadInputStream.readObject();
                     
                    // decompress message
                    nextMessage.decompress();
                     
                    // retrieve decompressed message
                    String fromClient = nextMessage.getMessage();
                     
                    // find position of separating character
                    int foundPos = fromClient.indexOf('#');
                     
                    // list of recipients for message
                    String sendTo = fromClient.substring(0,foundPos);
                     
                    // message to be sent to recipients
                    String message = fromClient.substring(foundPos+1);
 
                    // if the message is "quit" then this client wishes to leave the chat service
                    if(message.equals("quit "))
                    {   // add message to server output area
                        addOutput(clientName + " has " + message);
                         
                        // inform other clients that this client has quit
                        sendMessage(chatName + " has quit.");
                         
                        //send "goodbye" message to this client
                        threadOutputStream.writeObject(getCompressedMessage("Goodbye"));
                         
                        // remove this client from the list of clients
                        remove(chatName);    
                    }
                    else
                    {   // add message to server output area
                        addOutput(clientName + ": " + message);
                        // split string to separate recipients names
                        String[] recipients = sendTo.split(",\\s*");
                        // sort this array to use binarySearch
                        Arrays.sort(recipients);
                        // identify if this message is to be sent to all other clients
                        foundPos = Arrays.binarySearch(recipients, chattag[chattag.length-1]);
                        if(foundPos >= 0)
                           // send this message to all other clients
                            sendMessage(chatName + ">> " + message);
                        else
                            // send this message to all clients in recipients array
                            sendMessage(chatName + ">> " + message, recipients);
                    }
                } // end while
 
                // close input stream
                threadInputStream.close();
                // close output stream
                threadOutputStream.close();
            }
            catch(IOException e) // thrown by method readObject, writeObject, close
            {   System.out.println(e);
                System.exit(1);
            }
            catch(ClassNotFoundException e) // thrown by method readObject
            {   System.out.println(e);
                System.exit(1);
            }
        }
 
        /* this method returns a list of the names of all the 
           clients currently joined excluding this client */
        String getChatClients()
        {   String allClients = "";
            synchronized(chatClients)
            {   /* traverse list of threads & add value of 
                   instance variable name of each thread */
                for (Chat_ServerThread sThread : chatClients)
                {   if(!sThread.chatName.equals(chatName))
                        allClients += sThread.chatName + ",";
                }
            }
            if(allClients.equals(""))
                allClients = "none";
            return allClients;
        }
 
        /* this method sends the current message to all the 
           clients currently joined excluding this client */
        void sendMessage(String str)
        {   synchronized(chatClients)
            {   /* traverse list of threads & send message 
                   to all clients */
                for (Chat_ServerThread sThread : chatClients)
                {   if(!sThread.chatName.equals(chatName))
                    {   try
                        {   sThread.threadOutputStream.writeObject(getCompressedMessage(str));
                        }
                        catch(IOException e) // thrown by method writeObject
                        {   System.out.println(e);
                            System.exit(1);
                        }
                    }
                }
            }
        }
 
        /* this method sends the current message to all the 
           clients in the recipients array */
        void sendMessage(String str, String[] rec)
        {   synchronized(chatClients)
            {   /* traverse list of threads - if a match is found
                   with recipients name send message to that client */
                for(Chat_ServerThread sThread : chatClients)
                {   if(Arrays.binarySearch(rec, sThread.chatName) >= 0)
                    {   try
                        {   sThread.threadOutputStream.writeObject(getCompressedMessage(str));
                        }
                        catch(IOException e) // thrown by method writeObject
                        {   System.out.println(e);
                            System.exit(1);
                        }
                    }
                }
            }
        }
 
        /* this method removes this client's thread 
           from the list */
        void remove(String name)
        {   synchronized(chatClients)
            {   int pos = -1;
                /* traverse list of threads & find position of 
                   this client's thread in the list */
                for(int i = 0; i < chatClients.size(); i++)
                {   if(chatClients.get(i).chatName.equals(name))
                        pos = i;
                }
                if(pos != -1)
                    // remove this client's thread
                    chatClients.remove(pos);
            }
    }
    } // end of class Chat_ServerThread
} // end of class Chat_Server