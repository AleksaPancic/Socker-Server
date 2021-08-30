import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame { //ova klasa sedi na public serveru
	
	private JTextField userText;
	private JTextArea chatWindow; //konverzacijski prikaz
	private ObjectOutputStream output; //slanje podataka preko stream-a
	private ObjectInputStream input;
	private ServerSocket server; //konekcija klijenta i servera , konfiguracija
	private Socket connection; //konekcija je nazvana socketom. uspustavljanje konekcije
	
	//constructor
	public Server() {
		super("Instant soba");
		userText = new JTextField(); //set-up textfield
		userText.setEditable(false); //Ne moze da se kuca pre nego da se konektujes
		userText.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						sendMessage(event.getActionCommand()); //string koji smo uneli u text field
						userText.setText(""); //resetuje se i ceka da napises poruku
					}
				}
			);
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow));
		setSize(300,150);
		setVisible(true);
	}
	
	//set up i pokreni server
	public void StartRunning() {
		try {
			server = new ServerSocket(6789, 100); //6789 port number za konekciju, dockovanje, 100 koliko ljudi cekaju da chat ako je previse ljudi server moze da se crash
			while(true) {
				try {
					//konektuj se , poceta konverzacija
					waitForConnection();
					setupStreams(); //input i output
					whileChatting();
				}catch(EOFException eofException) {
					showMessage("\n Server je zavrsion konekciju");
				}finally {
					closeStuff();
				}
			}
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}
	}
	//cekaj za konekciju, onda prikazi informaciju konekcije
	private void waitForConnection() throws IOException{
		showMessage("Cekanje na konekciju... \n");
		connection = server.accept(); //ukoliko se neko konektovao prihvata konekciju kao socket, while petlja se loop i ceka na konekciju
		//ne kreira prazan socket nego kad je konektovan tek onda pravi socket
		showMessage(" Sada si konektovan sa"+connection.getInetAddress().getHostName()); //vraca adresu i konveruje u string
	}
	
	// stream salje i prima podatke
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream()); //kreiramo pathway da se poveze sa drugim kompjuterom
		output.flush(); //leftover data
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams su sada setup! \n");
	}
	
	//izvrsava se tokom konverzacije
	private void whileChatting() throws IOException{
		String message = " Sada si konektovan! ";
		sendMessage(message);
		ableToType(true); //iz false u true da mozes da kucas
		do {
			//konverzacija
			try {
				message = (String) input.readObject(); //input je socket gde se primaju poruke
				showMessage("\n" + message);
			}catch(ClassNotFoundException classNotFoundException) {
				showMessage("\n sta je poslato? Error!");
			}
		}while(!message.equals("CLIENT - END")); //ako klient unese END zavrsava se konverzacija
	}
	//zatvori strimove i soket kad zavrsis
	private void closeStuff() {
		showMessage("\n Zatvaram konekciju.. \n");
		ableToType(false); //ne moze user da kuca
		try {
			output.close();
			input.close();
			connection.close(); //zatvara konekciju, socket
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	//posalje poruku klijentu
	private void sendMessage(String message) {
		try {
			output.writeObject("SERVER -" + message);//posalji output streamu
			output.flush();
			showMessage("\n SERVER -" + message);
	}catch(IOException ioException) {
		chatWindow.append("\n ERROR: Ne mogu da posaljem poruku");
	}
  }
	//update chatWindow
	private void showMessage(final String text) {
		SwingUtilities.invokeLater( //update text u chat prozoru
				new Runnable() { //pravi thread/nit
					public void run() {
						chatWindow.append(text);
					}
				}
		);
	}
	//Pusti korisnika da output
	private void ableToType(final boolean tof) {
		SwingUtilities.invokeLater( 
				new Runnable() { //pravi thread/nit
					public void run() {
						userText.setEditable(tof);
					}
				}
		);
	}
}
