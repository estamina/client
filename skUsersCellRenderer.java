package client;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * renders users present in chat different way that others: renders them dimmed
 */
public class skUsersCellRenderer extends JLabel implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        boolean found = false;
        for (int i = 0; i<chatusers.size(); i++){
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
    
    ArrayList chatusers = new ArrayList();
}