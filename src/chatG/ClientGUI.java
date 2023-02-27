package chatG;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputListener;
import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat;
import javax.swing.UIManager;

public class ClientGUI {
    
    private static String username = "", serverAddress, serverPort;
	private JPanel contentPane, panelDescargas;
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket socket;
    private DatagramSocket socketvoz;
    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;
    private SourceDataLine sourceDataLine;
    private boolean isAudioRunning = false;
    private JFrame frameC;
	private JLabel lblC, lblUsuario, lblServidor, lblPuerto, titleDownloads;
	private JTextArea textAreaMsj, textAreaChat;
	private JScrollPane scrollAreaMsj, scrollAreaChat, scrollPaneDescargas; // scrollDescargas
	private JButton btnEnviar, chooseFile, sendFile;
	final File[] archivoAEnviar = new File[1]; 
    static ArrayList<ArchivoE> misArchivos = new ArrayList<>();
	int idArchivo = 0;
	
	// CONSTRUCTOR
    public ClientGUI(String serverAddress, int serverPort) {
        createGUI();
        connectToServer(serverAddress, serverPort);
        lblUsuario.setText("USUARIO: " + username);
        startReceivingMessages();
    }

    private void createGUI() {
    	
        // VENTANA PRINCIPAL DEL CLIENTE
        frameC = new JFrame("Chat Client");
        frameC.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameC.setBounds(50, 50, 700, 600);
        frameC.getContentPane().setLayout(null);
        frameC.setLocationRelativeTo(null);
        frameC.setResizable(false);
        
        contentPane = new JPanel();
        contentPane.setBounds(0, 0, 684, 561);
        contentPane.setBackground(new Color(147, 112, 219));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null); 
        frameC.getContentPane().add(contentPane);
        
        // ETIQUETAS
        lblC = new JLabel("CHAT CLIENTE");
		lblC.setForeground(new Color(255, 255, 255));
		lblC.setHorizontalAlignment(SwingConstants.CENTER);
		lblC.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblC.setBounds(31, 21, 620, 20);
		contentPane.add(lblC);
		
		lblUsuario = new JLabel("USUARIO: " + username);
		lblUsuario.setForeground(new Color(255, 255, 255));
		lblUsuario.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 14));
		lblUsuario.setBounds(31, 63, 240, 20);
		contentPane.add(lblUsuario);
		
		lblServidor = new JLabel("SERVIDOR: " + serverAddress);
		lblServidor.setForeground(new Color(255, 255, 255));
		lblServidor.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 14));
		lblServidor.setBounds(31, 94, 200, 22);
		contentPane.add(lblServidor);
		
		lblPuerto = new JLabel("PUERTO: " + serverPort);
		lblPuerto.setForeground(new Color(255, 255, 255));
		lblPuerto.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 14));
		lblPuerto.setBounds(31, 127, 200, 22);
		contentPane.add(lblPuerto);
		
		// ÁREA QUE MUESTRA EL CHAT
		textAreaChat = new JTextArea();
		textAreaChat.setBackground(new Color(216, 191, 216));
		textAreaChat.setWrapStyleWord(true);
		textAreaChat.setLineWrap(true);		
		textAreaChat.setEditable(false);
		scrollAreaChat = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollAreaChat.setViewportView(textAreaChat);
		scrollAreaChat.setEnabled(true);
		scrollAreaChat.setBounds(31, 172, 316, 288);
		contentPane.add(scrollAreaChat);
		
		// ÁREA PARA ESCRIBIR MENSAJE A ENVIAR
		textAreaMsj = new JTextArea();
		textAreaMsj.setBackground(new Color(250, 235, 215));
		textAreaMsj.setEditable(true); // CHECAR
		textAreaMsj.setLineWrap(true);
		scrollAreaMsj = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollAreaMsj.setViewportView(textAreaMsj);
		scrollAreaMsj.setBounds(31, 482, 260, 55);
		contentPane.add(scrollAreaMsj);
		
		// BOTÓN PARA ENVIAR MENSAJE
		btnEnviar = new JButton("");
		btnEnviar.setBackground(new Color(255, 204, 255));
		btnEnviar.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnEnviar.setBounds(307, 494, 30, 30);
		contentPane.add(btnEnviar);
		btnEnviar.setIcon(new ImageIcon(ClientGUI.class.getResource("/img/enviar.jpg")));
		btnEnviar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = textAreaMsj.getText();
                writer.println(message);
                textAreaMsj.setText("");
			}
		});
		
		// BOTONES PARA ENVIAR ARCHIVOS
		chooseFile = new JButton("");
		chooseFile.setIcon(new ImageIcon(ClientGUI.class.getResource("/img/choose.png")));
		chooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser selector = new JFileChooser ();
				selector.setDialogTitle("Seleccione el archivo a enviar.");
				if (selector.showOpenDialog(null) == selector.APPROVE_OPTION) {
					archivoAEnviar[0] = selector.getSelectedFile();
					JOptionPane.showConfirmDialog(null, "El nombre del archivo que desea enviar es: " + archivoAEnviar[0].getName());
					// NOMBRE DE ARCHIVO A ENVIAR EN UN LABEL *******
				}
			}
		});
		chooseFile.setBounds(398, 494, 40, 30);
		chooseFile.setBorder(new redondearBotones(10));
		contentPane.add(chooseFile);

		sendFile = new JButton("");
		sendFile.setIcon(new ImageIcon(ClientGUI.class.getResource("/img/send.png")));
		sendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (archivoAEnviar[0] == null) {
					JOptionPane.showMessageDialog(null, "No se ha seleccionado ningún archivo.");
				} else {
					try {
						FileInputStream fileIS = new FileInputStream (archivoAEnviar[0].getAbsolutePath());
						DataOutputStream dataOS = new DataOutputStream (socket.getOutputStream());
						String nombreArchivo = archivoAEnviar[0].getName();
						System.out.println(nombreArchivo); //BORRAR
						byte [] nombreArchivoBytes = nombreArchivo.getBytes();
						byte [] contenidoArchivoBytes = new byte [(int)archivoAEnviar[0].length()];
		                writer.println("archivo");
						fileIS.read(contenidoArchivoBytes);
						dataOS.writeInt(nombreArchivoBytes.length);
						dataOS.write(nombreArchivoBytes);
						dataOS.writeInt(contenidoArchivoBytes.length);
						dataOS.write(contenidoArchivoBytes);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		sendFile.setBounds(457, 494, 40, 30);
		sendFile.setBorder(new redondearBotones(10));
		contentPane.add(sendFile);
		
		// ETIQUETA Y PANEL DE ARCHIVOS
		titleDownloads = new JLabel("ARCHIVOS RECIBIDOS");
		titleDownloads.setForeground(Color.WHITE);
		titleDownloads.setHorizontalAlignment(SwingConstants.CENTER);
		titleDownloads.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 14));
		titleDownloads.setBounds(374, 131, 277, 14);
		contentPane.add(titleDownloads);
		
		panelDescargas = new JPanel();
		panelDescargas.setBounds(381, 465, 10, 10);
		scrollPaneDescargas = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPaneDescargas.setEnabled(false);
		scrollPaneDescargas.setBounds(374, 172, 277, 290);
		scrollPaneDescargas.setViewportView(panelDescargas);
		contentPane.add(scrollPaneDescargas);
		
		// BOTÓN PARA AUDIO 
		JButton btnStartVoice = new JButton("INICIAR");
		btnStartVoice.setBackground(new Color(204, 204, 255));
		btnStartVoice.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnStartVoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                if (!isAudioRunning) {
                    // startAudio();
                    btnStartVoice.setText("Stop");
                } else {
                    // stopAudio();
                    btnStartVoice.setText("Voice");
                }
			}
		});
		btnStartVoice.setBounds(532, 494, 100, 30);
		contentPane.add(btnStartVoice);

        // MUESTRA LA VENTANA
        frameC.setVisible(true);
    }

    // CONECTA AL CLIENTE CON EL SERVIDOR
    private void connectToServer(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            username = JOptionPane.showInputDialog("Ingresa el Nombre de Usuario que deseas emplear:");
            writer.println(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // COMIENZA A RECIBIR LOS MENSAJES
    private void startReceivingMessages() {
        try {
            String message;
            String nombreArchivo = "";
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("archivo")) {
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
            					panelDescargas.add(jpRenglonArchivo);
            					frameC.validate(); 
            				} else {
            					jpRenglonArchivo.setName(String.valueOf(idArchivo));
            					jpRenglonArchivo.addMouseListener (getMyMouseListener());
            					jpRenglonArchivo.add(jlNombreArchivo);
            					panelDescargas.add(jpRenglonArchivo);
            					frameC.validate();
            				} 
            				misArchivos.add(new ArchivoE(idArchivo, nombreArchivo, contenidoArchivoBytes, getExtensionArchivo (nombreArchivo)));
            				idArchivo++; 
            			} 
            		}
                    textAreaChat.append(username + " LISTOenvió un archivo: " + nombreArchivo + "\n");
                } else {
                    // Si el mensaje no comienza con "#archivo:", entonces es un mensaje de texto normal
                	textAreaChat.append(message + "CM\n");
                }
            }
            /* while ((message = reader.readLine()) != null) {
                if (message.startsWith("archivo")) {
                    // Si el mensaje comienza con "#archivo:", entonces se trata de un archivo adjunto
                    String filename = message.substring(9);
                    textAreaChat.append(username + " envió un archivo: " + filename + "\n");
                } else {
                    // Si el mensaje no comienza con "#archivo:", entonces es un mensaje de texto normal
                    textAreaChat.append(message + "\n");
                }
            } */
        } catch (IOException e) {
        	e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void recibirArchivo() { // NO SIRVE
        try {
            //Socket socket = new Socket("127.0.0.1", 2);

            DataInputStream dis = new DataInputStream(socket.getInputStream());

            // Leer el nombre del archivo
            int nombreLength = dis.readInt();
            byte[] nombreBytes = new byte[nombreLength];
            dis.readFully(nombreBytes, 0, nombreLength);
            String nombreArchivo = new String(nombreBytes);

            // Leer el contenido del archivo
            int contenidoLength = dis.readInt();
            byte[] contenidoBytes = new byte[contenidoLength];
            dis.readFully(contenidoBytes, 0, contenidoLength);

            // Escribir el archivo en disco
            FileOutputStream fos = new FileOutputStream("C:\\Users\\Carolina\\eclipse-workspace\\ChatG\\src\\img" + nombreArchivo);
            fos.write(contenidoBytes);
            // fos.close();

            System.out.println("El archivo " + nombreArchivo + " se ha descargado exitosamente.");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    // VENTANA ARCHIVOS
    public static JFrame createFrameA(String nombreArchivo, byte[] datosArchivo, String extension) {
		
		JFrame frameA = new JFrame ("Descarga de Archivos.");
		frameA.setSize(400,400);
		frameA.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameA.setLocationRelativeTo(null);
        frameA.setResizable(true);
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
    
    // HACE REDONDOS LOS BOTONES
    class redondearBotones implements Border {
    	private int radio;
    	redondearBotones (int radio){
    		this.radio = radio;
    	}
    	public Insets getBorderInsets (Component c) {
    		return new Insets (this.radio + 1, this.radio + 1, this.radio + 2, this.radio);
    	}
    	public boolean isBorderOpaque() {
    		return true;
    	}
    	public void paintBorder (Component c, Graphics g, int x, int y, int width, int height) {
    		g.drawRoundRect(x, y, width - 1, height -  1, radio, radio);
    	}
    }

    // MAIN QUE SOLICITA IP DEL SERVIDOR Y PUERTO E INICIALIZA EL CLIENTE Y SU INTERFAZ
    public static void main(String[] args) {
		serverAddress = JOptionPane.showInputDialog("Ingrese la dirección IP del Servidor al que desea conectarse."); 
		serverPort = JOptionPane.showInputDialog("Ingrese el Número de Puerto al que desea conectarse."); 
    	new ClientGUI(serverAddress, Integer.parseInt(serverPort));
    }
    
}