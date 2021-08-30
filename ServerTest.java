import javax.swing.JFrame;
public class ServerTest {

	public static void main(String[] args) {
		Server domaci = new Server();
		domaci.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		domaci.StartRunning();
		

	}

}
