package client;

import chat.skCode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * tab UI
 */
class skTab{
    private final skClient skClient;

    private JScrollPane scroll1;
    private JScrollPane scroll2;
    private JScrollPane scroll3;
    JTextArea text;
    JSplitPane split1;
    private JSplitPane split2;
    private JSplitPane split3;
    private JTextField field;
    JList userlist;
    private JList chatlist;
    
    String chatname = "";
    
    /**
     * initially by -1 server is informed that chatid was not assigned yet
     */
    int chatid = -1;
    
    /**
     * for private 1 to 1 chat server is informed about participant
     */
    String chattobe;
    
    /**
     * text message input is sent to server from here
     */
    private void fieldActionPerformed(ActionEvent evt) {
        try {
            out.write(skCode.MSGINTRO + "\n" + skCode.CLIENT_TEXT + "\n");
            out.write(new Integer(chatid).toString());
            out.write("\n");
            int lines = 1;
            if (chatid==-1){
                out.write(lines+"\n");
                out.write(chattobe+"\n");
            }else{
                if (selectedusers!=null){
                    out.write(selectedusers.size()+"\n");
                    for (int i = 0; i<selectedusers.size(); i++){
                        out.write(selectedusers.get(i).toString()+"\n");
                    }
                    selectedusers=null;
                }  else out.write("0\n");
            }
            out.write(chatname+"\n");
            
            out.write(lines+"\n"+skMyNick+"> "+field.getText()+"\n");
            out.flush();
            
            //clear this input field after the text message was sent
            field.setText("");
        }  catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Chyba1 "+ex.getMessage());
            System.exit(1);
        }
    }
    
    private void initComponents() {
        split1 = new JSplitPane();
        split2 = new JSplitPane();
        scroll1 = new JScrollPane();
        text = new JTextArea();
        split3 = new JSplitPane();
        scroll2 = new JScrollPane();
        userlist = new JList();
        scroll3 = new JScrollPane();
        chatlist = new JList();
        field = new JTextField();
        userscellrenderer = new skUsersCellRenderer();
        //tb.text.setText("daco");
        
        split1.setDividerLocation(375);
        split1.setDividerSize(15);
        split1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        
        split2.setDividerLocation(500);
        text.setColumns(20);
        text.setRows(5);
        text.setEditable(false);
        scroll1.setViewportView(text);
        
        split2.setLeftComponent(scroll1);
        
        split3.setDividerLocation(180);
        split3.setOrientation(JSplitPane.VERTICAL_SPLIT);
        userlist.setModel(skUserListModel);
        
        userlist.setCellRenderer(userscellrenderer);
        userlist.setFixedCellHeight(10);
        userlist.setFixedCellWidth(10);
        
        userlist.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
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
        field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fieldActionPerformed(evt);
            }
            });
        
        split1.setRightComponent(field);
    }
    
    /**
     * selection of users from user list
     */
    private void userlistMouseClicked(MouseEvent evt) {
        
        // doubleclick to initiate private 1 to 1 chat
        if (evt.getClickCount() == 2) {
            int index = userlist.locationToIndex(evt.getPoint());
            String anick = skUserListModel.getElementAt(index).toString();
            if (anick.compareTo(skMyNick+"\n")!=0)
                if (this.skClient.findTab(this.skClient.getUser(anick)) == null){
                    skTab atb = skClient.addTab(anick,-1);
                    //alternative way: skTabbedPane.setSelectedIndex(skTabbedPane.getTabCount()-1);
                    skTabbedPane.setSelectedComponent(atb.split1);
                }
        }//chatid is -1 while it is not assigned from server yet
        
        //rightclick to select users>=1 to participate in already started chat
        selectedusers = new ArrayList();
        if (evt.getButton()==3){
            Object[] indicies = userlist.getSelectedValues();
            StringBuffer usersline = new StringBuffer();
            for (int i = 0; i<indicies.length; i++){
                //debug//System.out.println("selected " + indicies[i].toString());
                String nickname = indicies[i].toString();
                usersline.append(nickname.trim()+" ");
                selectedusers.add(this.skClient.getUser(nickname));
            }
            usersline.append("added by "+skMyNick+"\n");
            this.skClient.dialog();
            try {
                out.write(skCode.MSGINTRO + "\n" + skCode.CLIENT_TEXT + "\n");
                out.write(new Integer(chatid).toString());
                out.write("\n");
                if (selectedusers!=null){
                    out.write(selectedusers.size()+"\n");
                    for (int i = 0; i<selectedusers.size(); i++){
                        out.write(selectedusers.get(i).toString()+"\n");
                    }
                    selectedusers=null;
                }  else {
                    int users = 0;
                    out.write(users+"\n");
                }
                out.write(chatname+"\n");
                int lines = 1;
                out.write(lines+"\n"+usersline.toString());
                out.flush();
            }  catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }
    }
    
    /**
     * selection of users to be added into chat (by rightclick)
     */
    private ArrayList selectedusers = null;
    
    /**
     * highlights/dims users present in chat
     */
    skUsersCellRenderer userscellrenderer;
    
    skTab(skClient skClient, OutputStreamWriter out, final String skMyNick, final JTabbedPane skTabbedPane, final DefaultListModel skUserListModel, DefaultListModel skChatListModel) {
        this.skClient = skClient;
        this.out=out;
        this.skTabbedPane=skTabbedPane;
        this.skUserListModel=skUserListModel;
        this.skChatListModel=skChatListModel;
        this.skMyNick=skMyNick;
        initComponents();
    }
    
    private final JTabbedPane skTabbedPane;
    
    private DefaultListModel skUserListModel;
    
    private OutputStreamWriter out;
    
    private DefaultListModel skChatListModel;

    private final String skMyNick;
    
}