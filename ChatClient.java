import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.*;

public class ChatClient extends JFrame {
    public static final int CHAT_PORT=12345;
	public static String HOST="localhost";

    //Pomocou tohto socketu komunikujem so serverom
    Socket socket;

    //Streamy na komunikaciu
    BufferedReader in;
    OutputStreamWriter out;

    //Graficke prvky
    private JTextArea text;
    private javax.swing.JPanel panel1;
    private JTextField autor;
    private JTextField sprava;
    private JButton poslat;

    //Sluzi na prijem sprav zo servera a vypis do okna
    class ReceiveThread extends Thread {
        //Metoda vykonavaneho vlakna
        public void run(){
            try{
                String line;
                //Precitam riadok zo standardneho vstupu
                while ((line = in.readLine()) != null) {
                    //Vypisem riadok
                    text.append(line+"\n");
                }

                //Uzavriem streamy
                in.close();
                out.close();
            }catch (Exception e){
                System.out.println("Chyba "+e.getMessage());
            }
            System.exit(0);
        }
    }

    public ChatClient() {
        try{
            //Vytvorenie spojenia so serverom
            socket = new Socket(HOST, CHAT_PORT);

            //Streamy na komunikaciu
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new OutputStreamWriter(socket.getOutputStream());

			out.write(System.getProperty("user.name","someone") +" appeared\n");
            out.flush();
        }catch (Exception e){
            System.out.println("Chyba "+e.getMessage());
            System.exit(1);
        }

        //Inicializacia grafickych komponentov
        panel1 = new JPanel();
        autor = new JTextField();
        sprava = new JTextField();
        poslat = new JButton();

        setTitle("Chatroom");

        //Vytvorim textove pole a nepovolim editovat vypisovaciu plochu
        text = new JTextArea();
        text.setEditable(false);

        //Podpora skrolovania pre textove pole
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(text);
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

        //Spodny panel
        panel1.setLayout(new javax.swing.BoxLayout(panel1, javax.swing.BoxLayout.Y_AXIS));

        //Panel pre autora
        JPanel panel1a = new JPanel();
        panel1a.setLayout(new javax.swing.BoxLayout(panel1a, javax.swing.BoxLayout.X_AXIS));
        panel1a.add(new JLabel("Meno: "));
        panel1a.add(autor);

        //Panel pre spravu
        JPanel panel1b = new JPanel();
        panel1b.setLayout(new javax.swing.BoxLayout(panel1b, javax.swing.BoxLayout.X_AXIS));
        panel1b.add(new JLabel("Spr\u00e1va: "));
        panel1b.add(sprava);

        //Talcidlo poslat spravu


        poslat.setText("Posla\u0165");
        poslat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try{
					//java.util.Date dat=new java.util.Date();
					//java.sql.Date datt=new java.sql.Date(dat.getTime());
					Calendar beforeDate=Calendar.getInstance();
					int hour=beforeDate.get(Calendar.HOUR_OF_DAY);
					int min=beforeDate.get(Calendar.MINUTE);
					int sec=beforeDate.get(Calendar.SECOND);

                    //Odoslanie spravy
//			out.write(System.getProperty("USERNAME",".")+" appeared\n");
//                    out.write(System.getenv("USERNAME")+"("+autor.getText()+")> "+sprava.getText()+"\n");
                    out.write(System.getProperty("user.name","someone")+" "+"("+autor.getText()+") "+hour+":"+min+":"+sec+"> "+sprava.getText()+"\n");
                    out.flush();
                    //Vymazanie obsahu pola sprava
                    sprava.setText("");
                }catch (Exception e){
                    System.out.println("Chyba "+e.getMessage());
                    System.exit(1);
                }
            }
        });
        //Nastavim predvolene tlacidlo na posielanie sprav
        getRootPane().setDefaultButton(poslat);
        panel1b.add(poslat);

        panel1.add(panel1a);
        panel1.add(panel1b);

        getContentPane().add(panel1, java.awt.BorderLayout.SOUTH);

        //Pri zatvoreni okna aplikaciu ukoncit
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Velkost okna
        setSize(500,400);

        //Vytvorim si vlakno na prijmanie sprav
        new ReceiveThread().start();
    }

    public static void main(String args[]) {
		//Ak je druhy parameter tak je povazovany za adresu hosta
		if(args.length==1)
			HOST=args[0];

        new ChatClient().show();
    }
}
