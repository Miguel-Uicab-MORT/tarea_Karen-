package chatG;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;

/**
* @author CHIN PECH JESSICA J. ESTEFANÍA, 57039, 8A, ISC
* @author PÉREZ PÉREZ KAREN E., 57569, 8A, ISC
* @author RODRIGUEZ CAB OMAR J., 56964, 8A, ISC
* @author UC VAZQUEZ D. CAROLINA, 57618, 8A, ISC
* @since FEBRERO 2023
 */

public class ServerGUI extends JFrame{

	private static final long serialVersionUID = 1L;
	private JFrame frameS;
    private JPanel contentPane, panelD;
    private JTextArea textAreaS;
	private JScrollPane scrollPaneS, scrollPaneD;
    private JLabel lblS, lblD;
    private Map<String, PrintWriter> clients;
    private ServerSocket serverSocket;
    private int port;
    static ArrayList<ArchivoE> misArchivos = new ArrayList<>();
	int idArchivo = 0;
    
	// CONSTRUCTOR
    public ServerGUI(int port) {
        this.port = port;
        clients = new HashMap<>();
        createGUI();
        startServer();
    }

    private void createGUI() {
    	
        // VENTANA PRINCIPAL DEL SERVER
        frameS = new JFrame("Chat Server");
        frameS.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameS.setBounds (400, 40, 800, 600);
        frameS.getContentPane().setLayout(null);
        frameS.setLocationRelativeTo(null);
        frameS.setResizable(false);
        
        contentPane = new JPanel();
        contentPane.setBounds(0, 0, 794, 561);
        contentPane.setBackground(new Color (32, 178, 170));
        contentPane.setLayout(null);
        frameS.getContentPane().add(contentPane);
        
        // FLUJO SERVIDOR
        textAreaS = new JTextArea();
        textAreaS.setEditable(false);
		textAreaS.setBackground(new Color(240, 248, 255));
		textAreaS.setLineWrap(true);
		scrollPaneS = new JScrollPane(textAreaS, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneS.setBounds(32, 27, 380, 460);
		contentPane.add(scrollPaneS);
		
		lblS = new JLabel("FLUJO DE DATOS EN EL SERVIDOR");
		lblS.setBounds(32, 510, 380, 26);
		lblS.setForeground(new Color(255, 250, 250));
		lblS.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblS.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblS);
		
		// DESCARGAS
		panelD = new JPanel();
		panelD.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panelD.setBounds(381, 465, 10, 10);
		panelD.setLayout(new GridLayout(0, 1));
		// panelD.setLayout(new BoxLayout(panelD, BoxLayout.X_AXIS));
		scrollPaneD = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPaneD.setBounds(449, 27, 300, 460);
		scrollPaneD.setViewportView(panelD);
		contentPane.add(scrollPaneD);
		
		lblD = new JLabel("DESCARGAS");
		lblD.setHorizontalAlignment(SwingConstants.CENTER);
		lblD.setForeground(new Color(255, 250, 250));
		lblD.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblD.setBounds(449, 510, 300, 26);
		contentPane.add(lblD);

        // MOSTRAR VENTANA
        frameS.setVisible(true);
    }

    // INICIA EL SERVIDOR
    private void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
            	Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ENVÍA MENSAJE
    private synchronized void broadcast(String message) {
        for (PrintWriter writer : clients.values()) {
            writer.println(message);
        }
    }
    
    // MANEJADOR DE CLIENTE
    private class ClientHandler extends Thread {

        private Socket socket;
        private String username;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
        	String ipRemota = "", nombrePC = "";
            try {
            	
                // CREA FLUJOS DE ENTRADA Y SALIDA
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                // OBTIENE NOMBRE DE USUARIO DEL CLIENTE Y LO AÑADE A LA LISTA DE CLIENTES
                username = reader.readLine();
                clients.put(username, writer);
                InetAddress online = socket.getInetAddress(); 
    			ipRemota = online.getHostAddress(); // PROBAR
    			nombrePC = online.getHostName(); 
                textAreaS.append(username + " (" + nombrePC + " - " + ipRemota + ") se ha unido al chat.\n");

                // EMITE MENSAJE A LOS CLIENTES SOBRE UN NUEVO USUARIO
                broadcast(username + " (" + nombrePC + " - " + ipRemota + ") se ha unido al chat.");

                // LEE MENSAJES DEL CLIENTE Y LOS EMITE A LOS DEMÁS CLIENTES
                String message;
                String nombreArchivo = "";
                while ((message = reader.readLine()) != null) {
                    if (message.startsWith("archivo")) {
                        // String filename = message.substring(7);
                		DataInputStream dataIS = new DataInputStream (socket.getInputStream()); 
                		int nombreFLong = dataIS.readInt();
                		if (nombreFLong > 0) {
                			byte [] nombreArchivoBytes = new byte [nombreFLong];
                			dataIS.readFully(nombreArchivoBytes, 0, nombreArchivoBytes.length);
                			nombreArchivo = new String (nombreArchivoBytes);
                			int contenidoFLong = dataIS.readInt();
                			if (contenidoFLong > 0) {
                				byte [] contenidoArchivoBytes = new byte [contenidoFLong];
                				dataIS.readFully(contenidoArchivoBytes, 0, contenidoArchivoBytes.length);
                				JPanel jpRenglonArchivo = new JPanel();
                				jpRenglonArchivo.setLayout(new BoxLayout(jpRenglonArchivo, BoxLayout.Y_AXIS));
                				JLabel jlNombreArchivo = new JLabel(nombreArchivo);
                				jlNombreArchivo.setFont(new Font ("Arial", Font.BOLD, 16));
                				jlNombreArchivo.setBorder(new EmptyBorder (10, 0, 10, 0));
                				if (getExtensionArchivo(nombreArchivo).equalsIgnoreCase("txt")) {
                					jpRenglonArchivo.setName(String.valueOf(idArchivo));
                					jpRenglonArchivo.addMouseListener(getMyMouseListener());
                					jpRenglonArchivo.add(jlNombreArchivo);
                					panelD.add(jpRenglonArchivo);
                					frameS.validate();
                				} else {
                					jpRenglonArchivo.setName(String.valueOf(idArchivo));
                					jpRenglonArchivo.addMouseListener (getMyMouseListener());
                					jpRenglonArchivo.add(jlNombreArchivo);
                					panelD.add(jpRenglonArchivo);
                					frameS.validate();
                				} 
                				misArchivos.add(new ArchivoE(idArchivo, nombreArchivo, contenidoArchivoBytes, getExtensionArchivo (nombreArchivo)));
                				idArchivo++;
                			}
                		}
                        textAreaS.append(username + " envió un archivo: " + nombreArchivo + "\n");
                        broadcast("archivo" + username + " Senvió un archivo: " + nombreArchivo + "\n");
                    } else {
                        textAreaS.append(username + ": " + message + "\n");
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                // ELIMINA AL CLIENTE DE LA LISTA SI ESTE DE DESCONECTA Y EMITE MENSAJE A LOS DEMÁS CLIENTES
                clients.remove(username);
                textAreaS.append(username + " (" + nombrePC + " - " + ipRemota + ") ha salido del chat.\n");
                broadcast(username + " (" + nombrePC + " - " + ipRemota + ") ha salido del chat.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // VENTANA ARCHIVOS
    public static JFrame createFrameA(String nombreArchivo, byte[] datosArchivo, String extension) {
		
		JFrame frameA = new JFrame ("Descarga de Archivos.");
		frameA.setSize(400,400);
		frameA.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameA.setLocationRelativeTo(null);
        frameA.setResizable(false);
		JPanel panelA = new JPanel ();
		panelA.setLayout (new BoxLayout (panelA, BoxLayout.Y_AXIS));
		
		JLabel lblTitle = new JLabel ("Descarga de Archivos.");
		lblTitle.setAlignmentX(SwingConstants.CENTER);
		lblTitle.setFont(new Font ("Arial", Font.BOLD, 25));
		lblTitle.setBorder(new EmptyBorder(20,0,10,0));
		
		JLabel lblPrompt  = new JLabel("¿Está seguro de que desea descargar el archivo: " + nombreArchivo + "?");
		lblPrompt.setFont(new Font ("Arial", Font.BOLD, 20));
		lblPrompt.setBorder(new EmptyBorder(20,0,10,0));
		lblPrompt.setAlignmentX(SwingConstants.CENTER);
		
		JButton btnYes = new JButton ("Sí");
		btnYes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File archivoDescarga = new File (nombreArchivo);
				FileOutputStream fileOS;
				try {
					fileOS = new FileOutputStream (archivoDescarga);
					fileOS.write(datosArchivo);
					fileOS.close();
					frameA.dispose();
				} catch (IOException e1) {
					e1.printStackTrace();
					System.out.println("FALLA EN DESCARGA."); //BORRAR
				}
			}
		});
		btnYes.setPreferredSize(new Dimension (150,75));
		btnYes.setFont(new Font ("Arial", Font.BOLD, 20));
		
		JButton btnNo = new JButton ("No");
		btnNo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frameA.dispose();
			}
		});
		btnNo.setPreferredSize(new Dimension (150,75));
		btnNo.setFont(new Font ("Arial", Font.BOLD, 20));
		
		JLabel lblContenido = new JLabel ();
		lblContenido.setAlignmentX(SwingConstants.CENTER);
		
		JPanel panelBtns = new JPanel ();
		panelBtns.setBorder(new EmptyBorder(20,0,10,0));
		panelBtns.add(btnYes);
		panelBtns.add(btnNo);
		
		if (extension.equalsIgnoreCase("txt")) {
			lblContenido.setText("<html>" + new String(datosArchivo) + "</html>");
		} else {
			lblContenido.setIcon(new ImageIcon(datosArchivo));
		}
		
		panelA.add(lblTitle);
		panelA.add(lblPrompt);
		panelA.add(lblContenido);
		panelA.add(panelBtns);
		frameA.add(panelA);
		
		return frameA;
	}
	
    // ARCHIVOS
	public void metodoArchivo(Socket socket) throws IOException {
		// PASAR CÓDIGO PARA MOSTRAR ARCHIVOS EN EL SERVIDOR **********
	}
	
	// EXTENSIÓN DEL ARCHIVO A ENVIAR
	public static String getExtensionArchivo(String nombreArchivo) {
			int i = nombreArchivo.lastIndexOf('.');
			if (i > 0) {
				return nombreArchivo.substring(i + 1);
			} else {
				return "No se reconoce la extensión del archivo.";
			}
	}
    
    // CREA LA VENTANA CUANDO SE HACE CLICK EN EL PANEL
	public static MouseListener getMyMouseListener() {
	    return new MouseInputListener() {
	        @Override
	        public void mouseClicked(MouseEvent e) {
	            JPanel panelDescargas = (JPanel) e.getSource();
	            int idArchivo = Integer.parseInt(panelDescargas.getName());
	            // ASUMIENDO QUE misArchivos ES UNA VARIABLE DE INSTANCIA DE LA CLASE
	            for (ArchivoE miArchivo : misArchivos) {
	                if (miArchivo.getId() == idArchivo) {
	                    JFrame vPrevia = createFrameA(miArchivo.getNombre(), miArchivo.getData(), miArchivo.getExtension());
	                    vPrevia.setVisible(true);
	                }
	            }
	        }
	        @Override
	        public void mousePressed(MouseEvent e) {}
	        @Override
	        public void mouseReleased(MouseEvent e) {}
	        @Override
	        public void mouseEntered(MouseEvent e) {}
	        @Override
	        public void mouseExited(MouseEvent e) {}
	        @Override
	        public void mouseDragged(MouseEvent e) {}
	        @Override
	        public void mouseMoved(MouseEvent e) {}
	    };
	}

	// MAIN QUE SOLICITA PUERTO E INICIALIZA EL SERVIDOR Y SU INTERFAZ
    public static void main(String[] args) {
        int port = Integer.parseInt(JOptionPane.showInputDialog("Ingresa el Número de Puerto que deseas emplear: "));
        new ServerGUI(port);
    }
}