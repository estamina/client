/*
 * skClient.java
 *
 * Created on Pondelok, 2007, apríl 16, 14:44
 */

package client;
import chat.skUser;
import chat.skCode;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.io.*;
/**
 *
 * @author  sk1u06w4
 */
public class skClient extends javax.swing.JFrame {
    //public static final int CHAT_PORT=12345;
    public static String HOST="localhost";
    public static int CHAT_PORT=12345;
    //public static String HOST="sk16356c.siemens-pse.sk";
    
    //Pomocou tohto socketu komunikujem so serverom
    Socket socket;
    
    /**
     * tab UI
     */
    class skTab{
        javax.swing.JScrollPane scroll1, scroll2, scroll3;
        javax.swing.JTextArea text;
        javax.swing.JSplitPane split1, split2, split3;
        javax.swing.JTextField field;
        javax.swing.JList userlist, chatlist;
        
        private String chatname="";
        
        /**
         * initially by -1 server is informed that chatid was not assigned yet
         */
        private int chatid=-1;
        
        /**
         * for private 1 to 1 chat server is informed about participant
         */
        private String chattobe;
        
        /**
         * text message input is sent to server from here
         */
        public void fieldActionPerformed(java.awt.event.ActionEvent evt) {
            try{
                out.write(skCode.MSGINTRO+"\n"+skCode.CLIENT_TEXT+"\n");
                out.write(new Integer(chatid).toString());
                out.write("\n");
                if (chatid==-1){
                    out.write("1\n");
                    out.write(chattobe+"\n");
                }else{
                    if (selectedusers!=null){
                        out.write(selectedusers.size()+"\n");
                        for (int i=0;i<selectedusers.size();i++){
                            out.write(selectedusers.get(i).toString()+"\n");
                        }
                        selectedusers=null;
                    } else out.write("0\n");
                }
                out.write(chatname+"\n");
                
                out.write("1\n"+skMyNick+"> "+field.getText()+"\n");
                out.flush();
                
                //clear this input field after the text message was sent
                field.setText("");
            }catch (Exception e){
                System.out.println("Chyba1 "+e.getMessage());
                System.exit(1);
            }
        }
        
        public void initComponents() {
            split1 = new javax.swing.JSplitPane();
            split2 = new javax.swing.JSplitPane();
            scroll1=new javax.swing.JScrollPane();
            text=new javax.swing.JTextArea();
            split3 = new javax.swing.JSplitPane();
            scroll2=new javax.swing.JScrollPane();
            userlist = new javax.swing.JList();
            scroll3=new javax.swing.JScrollPane();
            chatlist = new javax.swing.JList();
            field= new javax.swing.JTextField();
            userscellrenderer=new skUsersCellRenderer();
            //tb.text.setText("daco");
            
            split1.setDividerLocation(375);
            split1.setDividerSize(15);
            split1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            
            split2.setDividerLocation(500);
            text.setColumns(20);
            text.setRows(5);
            text.setEditable(false);
            scroll1.setViewportView(text);
            
            split2.setLeftComponent(scroll1);
            
            split3.setDividerLocation(180);
            split3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            userlist.setModel(skUserListModel);
            
            userlist.setCellRenderer(userscellrenderer);
            userlist.setFixedCellHeight(10);
            userlist.setFixedCellWidth(10);
            
            userlist.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    userlistMouseClicked(evt);
                }
            });
            
            scroll2.setViewportView(userlist);
            
            split3.setLeftComponent(scroll2);
            
            chatlist.setModel(skChatListModel);
            scroll3.setViewportView(chatlist);
            
            split3.setRightComponent(scroll3);
            split2.setRightComponent(split3);
            split1.setLeftComponent(split2);
            
            //tb.field.setText("tbfield");
            field.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    fieldActionPerformed(evt);
                }
            });
            
            split1.setRightComponent(field);
        }
        
        /**
         * selection of users from user list
         */
        public void userlistMouseClicked(java.awt.event.MouseEvent evt) {
            
            // doubleclick to initiate private 1 to 1 chat
            if (evt.getClickCount() == 2) {
                int index = userlist.locationToIndex(evt.getPoint());
                String anick=skUserListModel.getElementAt(index).toString();
                if (anick.compareTo(skMyNick+"\n")!=0)
                    if (findTab(getUser(anick))==null){
                    skTab atb=addTab(anick,-1);
                    //alternative way: skTabbedPane.setSelectedIndex(jTabbedPane1.getTabCount()-1);
                    skTabbedPane.setSelectedComponent(atb.split1);
                    }
            }//chatid is -1 while it is not assigned from server yet
            
            //rightclick to select users>=1 to participate in already started chat
            selectedusers=new ArrayList();
            if(evt.getButton()==3){
                java.lang.Object[] indicies=userlist.getSelectedValues();
                StringBuffer usersline=new StringBuffer();
                for (int i=0;i<indicies.length;i++){
                    //debug//System.out.println("selected " + indicies[i].toString());
                    String nickname=indicies[i].toString();
                    usersline.append(nickname.trim()+" ");
                    selectedusers.add(getUser(nickname));
                }
                usersline.append("added by "+skMyNick+"\n");
                dialog();
                try {
                    out.write(skCode.MSGINTRO+"\n"+skCode.CLIENT_TEXT+"\n");
                    out.write(new Integer(chatid).toString());
                    out.write("\n");
                    if (selectedusers!=null){
                        out.write(selectedusers.size()+"\n");
                        for (int i=0;i<selectedusers.size();i++){
                            out.write(selectedusers.get(i).toString()+"\n");
                        }
                        selectedusers=null;
                    } else out.write("0\n");
                    out.write(chatname+"\n");
                    out.write("1\n"+usersline.toString());
                    out.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
            }
        }
        
        /**
         * selection of users to be added into chat (by rightclick)
         */
        private ArrayList selectedusers=null;
        
        /**
         * highlights/dims users present in chat
         */
        private skUsersCellRenderer userscellrenderer;
        
    }
    
    //Streamy na komunikaciu
    BufferedReader in;
    OutputStreamWriter out;
    
    public skClient(String login) {
        if (login.length()>0){skMyNick=skMyLogin=login;}else{
            skMyNick=skMyLogin=System.getProperty("user.name","someone");}
        try{
            BufferedReader setfile=new BufferedReader(new FileReader("setup.ini"));;
            CHAT_PORT=new Integer(setfile.readLine()).intValue();
            setfile.close();
            //Vytvorenie spojenia so serverom
            socket = new Socket(HOST, CHAT_PORT);
            
            //Streamy na komunikaciu
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new OutputStreamWriter(socket.getOutputStream());
            //out.write(System.getProperty("user.name","someone")+" appeared\n");
            out.write(skCode.MSGINTRO+"\n"+skCode.ENTER+"\n1\n");
            out.write(skMyLogin+"\n");//enter
            out.flush();
        }catch (Exception e){
            System.out.println("Chyba3 "+e.getMessage());
            System.exit(1);
        }
        initComponents();
        tabList = new LinkedList();
        new skNetwork().start();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        skTabbedPane = new javax.swing.JTabbedPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        skTextArea = new javax.swing.JTextArea();
        jSplitPane3 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        skUserList = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        skChatList = new javax.swing.JList();
        skMsgField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(skMyNick);
        setResizable(false);
        skTabbedPane.setFocusCycleRoot(true);
        skTabbedPane.setVerifyInputWhenFocusTarget(false);
        skTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                skTabbedPaneStateChanged(evt);
            }
        });
        skTabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                skTabbedPaneMouseClicked(evt);
            }
        });

        jSplitPane1.setDividerLocation(375);
        jSplitPane1.setDividerSize(15);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setDividerLocation(500);
        skTextArea.setColumns(20);
        skTextArea.setEditable(false);
        skTextArea.setRows(5);
        skTextArea.setText("\n\n\n\t1. to start private chat  doubleclick on a user\n\n\t2. to add another users to chat select them and rightclick\n\n\t3. to leave a chat rightclick on chat tab\n");
        jScrollPane1.setViewportView(skTextArea);

        jSplitPane2.setLeftComponent(jScrollPane1);

        jSplitPane3.setDividerLocation(200);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        skUserListModel=new javax.swing.DefaultListModel();
        skUserList.setModel(skUserListModel
        );
        skUserList.setFixedCellHeight(10);
        skUserList.setFixedCellWidth(10);
        skUserList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                skUserListMouseClicked(evt);
            }
        });

        jScrollPane2.setViewportView(skUserList);

        jSplitPane3.setTopComponent(jScrollPane2);

        skChatListModel=new javax.swing.DefaultListModel();
        skChatList.setModel(skChatListModel);
        jScrollPane3.setViewportView(skChatList);

        jSplitPane3.setRightComponent(jScrollPane3);

        jSplitPane2.setRightComponent(jSplitPane3);

        jSplitPane1.setLeftComponent(jSplitPane2);

        skMsgField.setEnabled(false);
        jSplitPane1.setRightComponent(skMsgField);

        skTabbedPane.addTab("!", jSplitPane1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(skTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(skTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                .addContainerGap())
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void skTabbedPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_skTabbedPaneMouseClicked
        if(evt.getButton()==3){
            skTab atb=null;
            javax.swing.JOptionPane.showMessageDialog(this,"leaving this chat");
            try {
                out.write(skCode.MSGINTRO+"\n"+skCode.CHAT_EXIT+"\n");
                atb=findTab((javax.swing.JSplitPane )skTabbedPane.getSelectedComponent());
                out.write(atb.chatid+"\n");
                out.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            int itab=skTabbedPane.getSelectedIndex();
            if (itab>0) {
                skTabbedPane.remove(itab);
                if (atb!=null)tabList.remove(atb);
            }
        }
    }//GEN-LAST:event_skTabbedPaneMouseClicked
    
    private void skTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_skTabbedPaneStateChanged
        skTabbedPane.setBackgroundAt(skTabbedPane.getSelectedIndex(),java.awt.Color.gray);
    }//GEN-LAST:event_skTabbedPaneStateChanged
    
    private void skUserListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_skUserListMouseClicked
        
        if (evt.getClickCount() == 2) {
            int index = skUserList.locationToIndex(evt.getPoint());
            String anick=skUserListModel.getElementAt(index).toString();
            if (anick.compareTo(skMyNick+"\n")!=0)
                if (findTab(getUser(anick))==null){
                skTab atb=addTab(anick,-1);
                //jTabbedPane1.setSelectedIndex(jTabbedPane1.getTabCount()-1);
                skTabbedPane.setSelectedComponent(atb.split1);
                }
        }//chatid is -1 while it is not assigned from server yet
        
    }//GEN-LAST:event_skUserListMouseClicked
    
    private void updateUserList(){
        skUserListModel.clear();
        for (Iterator i=skGlobalUsers.iterator();i.hasNext();){
            String nick=((skUser)i.next()).nick;
            skUserListModel.addElement(nick+"\n");
            skUserList.ensureIndexIsVisible(skUserListModel.size());
        }
/*debug
        skTextArea.setText("");
        for (Enumeration e = skUserListModel.elements() ; e.hasMoreElements() ;) {
            skTextArea.append(e.nextElement()+"\n");
        }
 */
    }
    public static void main(String[] args)  throws Exception {
        try {
            
            if (args[0].length()>0){
                new skClient(args[0]).show();
            }else{
                new skClient("").show();
            }
        } catch(java.lang.ArrayIndexOutOfBoundsException e){
            new skClient("").show();
            
        }finally {
        }
    }
    
    /**
     * thread for decoding received messages from socket
     */
    class skNetwork extends Thread {
        
        public void run(){
            
            try{
                String line;
                
                while ((line = in.readLine()) != null) {
                    while (line.compareTo(skCode.MSGINTRO)!=0){line = in.readLine();}
                    decode();
                }
                
                in.close();
                out.close();
            }catch (Exception e){
                System.out.println("Chyba5 "+e.getMessage());
            }
            System.exit(0);
        }
        
        public synchronized void decode() {
            int id;
            skTab atb;
            String line;
            try {
                line = in.readLine();
                System.out.println("msgcode:"+line);
                
                int msgCode=new Integer(line).intValue();
                switch (msgCode){
                    case skCode.USERS:
                        skGlobalUsers.clear();
                        
                        int users=new Integer(in.readLine()).intValue();
                        for (int i=0; i<users;i++) {
                            skUser user=new skUser();
                            line=in.readLine();
                            user.user=line;
                            if (line.compareTo(skMyLogin)==0){skMyNick=line=in.readLine();setTitle(skMyNick);} else line=in.readLine();
                            user.nick=line;
                            System.out.println(user.nick+" "+user.user);
                            skGlobalUsers.add(user);
                        }
                        updateUserList();
                        break;
                    case skCode.CHAT_USERS:
                        id=new Integer(in.readLine()).intValue();
                        int iusers=new Integer(in.readLine()).intValue();
                        String otheruser=null,othernick=null;
                        ArrayList chatusers=new ArrayList();
                        for (int i=0;i<iusers;i++){
                            line=in.readLine();
                            if(line.compareTo(skMyLogin)!=0)otheruser=line;
                            line=in.readLine();
                            if(line.compareTo(skMyNick)!=0)othernick=line;
                            chatusers.add(line);
                        }
                        iusers--;
                        atb=findTab(id);
                        if (atb==null){
                            atb=findTab(otheruser);
                            if (atb==null) atb=addTab(othernick,id);
                            else atb.chatid=id;
                        }
                        atb.userscellrenderer.chatusers=chatusers;
                        break;
                    case skCode.SERVER_TEXT:
                        id=new Integer(in.readLine()).intValue();
                        int lines=new Integer(in.readLine()).intValue();
                        atb=findTab(id);
                        for (int i=0; i<lines;i++) {
                            line=in.readLine();
                            atb.text.append(line+"\n");
                        }
                        int itab=skTabbedPane.indexOfComponent(atb.split1);
                        if (skTabbedPane.getSelectedIndex()!=itab) skTabbedPane.setBackgroundAt(itab,Color.red);
                        atb.userlist.repaint();
                        break;
                    case skCode.CHATS:
                        //number of chats
                        line=in.readLine();
                        id=new Integer(in.readLine()).intValue();
                        atb=findTab(id);
                        atb.chatname=in.readLine();
                        atb.chattobe="";
                        
                        int ind=skTabbedPane.indexOfComponent(atb.split1);
                        skTabbedPane.setTitleAt(ind,atb.chatname);
                        
                        break;
                    default:
                        skTextArea.append("default  "+line+"\n");
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                System.out.println("Chyba7 "+ex.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Chyba8 "+ex.getMessage());
            }
        }
    }
    private String skMyLogin;
    private String skMyNick;
    private static LinkedList tabList;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JList skChatList;
    private javax.swing.JTextField skMsgField;
    private javax.swing.JTabbedPane skTabbedPane;
    private javax.swing.JTextArea skTextArea;
    private javax.swing.JList skUserList;
    // End of variables declaration//GEN-END:variables
    private javax.swing.DefaultListModel skUserListModel;
    private javax.swing.DefaultListModel skChatListModel;
    
    public skTab addTab(String name, int lid) {
        
        
        skTab tb=new skTab();
        
        tb.initComponents();
        tb.chattobe=getUser(name);
        tb.chatid=lid;
        System.out.println(tb.chattobe);
        
        skTabbedPane.addTab(name,tb.split1);
        
        tabList.add(tb);
        return tb;
        
    }
    
    private LinkedList skGlobalUsers=new LinkedList();
    
    /**
     * gets user on given nickname
     */
    public String getUser(String nick) {
        String user=null;
        for (Iterator i=skGlobalUsers.iterator();i.hasNext();){
            skUser needle=(skUser)i.next();
            System.out.println(needle.user+" "+needle.nick+" "+nick.trim());
            if ((needle).nick.compareTo(nick.trim())==0) {
                user=needle.user;
                System.out.println(needle.user+" found "+needle.nick);
                
                break;
            }
        }
        return user;
    }
    
    public skTab findTab(int lid) {
        skTab ltab=null;
        for (Iterator i=tabList.iterator();i.hasNext();){
            ltab=(skTab)i.next();
            if ((ltab).chatid==lid)break;
            else ltab=null;
        }
        
        return ltab;
    }
    
    public skTab findTab(String user) {
        skTab ltab=null;
        for (Iterator i=tabList.iterator();i.hasNext();){
            ltab=(skTab)i.next();
            if ((ltab).chattobe.compareTo(user)==0){
                System.out.println(user+" found "+ltab.chattobe);
                break;
            } else ltab=null;
        }
        
        return ltab;
    }
    
    /**
     * renders users present in chat different way that others: renders them dimmed
     */
    public class skUsersCellRenderer extends javax.swing.JLabel implements javax.swing.ListCellRenderer {
        public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            boolean found=false;
            for (int i=0;i<chatusers.size();i++){
                if (chatusers.get(i).toString().compareTo(value.toString().trim())==0) {found=true;break;}
            }
            if (found){
                setBackground(isSelected ? Color.blue : Color.white);
                setForeground(isSelected ? Color.white : Color.gray);
            }else{
                setBackground(isSelected ? Color.red : Color.white);
                setForeground(isSelected ? Color.white : Color.black);
            }
            setText(value.toString());
            return this;
        }
        
        public skUsersCellRenderer() {
            setOpaque(true);
        }
        
        private ArrayList chatusers=new ArrayList();
    }
    
    public void dialog() {
        javax.swing.JOptionPane.showMessageDialog(this,"selected users are\nto be added to this chat");
    }
    
    public skTab findTab(javax.swing.JSplitPane splitpane) {
        skTab ltab=null;
        for (Iterator i=tabList.iterator();i.hasNext();){
            ltab=(skTab)i.next();
            if ((ltab).split1.equals(splitpane)){
                System.out.println(" found "+ltab.chatid);
                break;
            } else ltab=null;
        }
        
        return ltab;
    }
    
}
