package chatG;

import java.io.IOException;
// import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.sound.sampled.*;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;

/**
* @author CHIN PECH JESSICA J. ESTEFANÍA, 57039, 8A, ISC
* @author PÉREZ PÉREZ KAREN E., 57569, 8A, ISC
* @author RODRIGUEZ CAB OMAR J., 56964, 8A, ISC
* @author UC VAZQUEZ D. CAROLINA, 57618, 8A, ISC
* @since FEBRERO 2023
 */

public class Server {
	
    private ServerSocket serverSocket;
    private ArrayList<ServerThread> clients;
    private SourceDataLine sourceDataLine;

    // CONSTRUCTOR
    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clients = new ArrayList<ServerThread>();
            
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(format);
            sourceDataLine.start();
            
            // Aquí debe de imprimirse en un TextArea
            System.out.println("El SERVIDOR se ha iniciado en el puerto: " + port);
            
            /*byte[] buffer = new byte[1024];
            String charsetName = "UTF-8";
		    String str = new String (buffer, charsetName);
		    char[] charBuffer = str.toCharArray();*/
            
            while (true) {
                Socket socket = serverSocket.accept();
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    sourceDataLine.write(buffer, 0, bytesRead);
                }
                ServerThread thread = new ServerThread(socket, this);
                clients.add(thread);
                thread.start();
            }
        } catch (IOException | LineUnavailableException e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }

    // EMITE A CLIENTES
    public void broadcast(String message, ServerThread sender) {
        for (ServerThread client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // ELIMINA AL CLIENTE
    public void removeClient(ServerThread client) {
        clients.remove(client);
    }

    // CREA SERVER
    public static void main(String[] args) {
		Server server = new Server(9000);
    }
}