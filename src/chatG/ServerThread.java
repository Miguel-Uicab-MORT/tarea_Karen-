package chatG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
* @author CHIN PECH JESSICA J. ESTEFANÍA, 57039, 8A, ISC
* @author PÉREZ PÉREZ KAREN E., 57569, 8A, ISC
* @author RODRIGUEZ CAB OMAR J., 56964, 8A, ISC
* @author UC VAZQUEZ D. CAROLINA, 57618, 8A, ISC
* @since FEBRERO 2023
 */

public class ServerThread extends Thread {
	
    private Socket socket;
    private Server server;
    private PrintWriter writer;

    // CONSTRUCTOR
    public ServerThread(Socket socket, Server serverT) {
        this.socket = socket;
        this.server = serverT;
    }

    // ENVÍA MENSAJE
    public void sendMessage(String message) {
        writer.println(message);
    }

    // ENTRADA Y SALIDA DE MENSAJES A MOSTRAR
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            String username = reader.readLine();
            server.broadcast(username + " ha llegado al chat.", this);
            String message;
            do {
            	// RECIBE MENSAJES Y LOS MUESTRA
                message = reader.readLine();
                if (message != null) {
                    server.broadcast("[" + username + "]: " + message, this);
                }
            } while (message != null);
            server.removeClient(this);
            server.broadcast(username + " ha dejado el chat.", this);
            socket.close();
        } catch (IOException e) {
            System.out.println("ServerThread exception: " + e.getMessage());
        }
    }
}