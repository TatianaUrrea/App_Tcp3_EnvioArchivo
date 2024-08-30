package apptcp_envioarchivo;

import java.awt.*;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrincipalSrv extends javax.swing.JFrame {

    private final int PORT = 12345;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private final HashMap<String, ClientConnection> clientConnections = new HashMap<>();

    public PrincipalSrv() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        this.setTitle("Servidor ...");

        bIniciar = new javax.swing.JButton();
        bSalir = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        mensajesTxt = new JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE); // Configura la operación de cierre de la ventana.
        getContentPane().setLayout(null); // Establece el diseño del contenedor en nulo.

        //BOTON INICIAR SERVIDOR
        bIniciar.setFont(new java.awt.Font("Spectral", 1, 11));
        bIniciar.setText("INICIAR SERVIDOR");
        bIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bIniciarActionPerformed(evt);
            }
        });
        getContentPane().add(bIniciar);
        bIniciar.setBounds(20, 50, 410, 40);
        bIniciar.setBackground(Color.WHITE);
        bIniciar.setForeground(Color.BLACK);

        //BOTON SALIR
        bSalir.setFont(new java.awt.Font("Spectral", 1, 11));
        bSalir.setText("SALIR");
        bSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSalirActionPerformed(evt);
            }
        });
        getContentPane().add(bSalir);
        bSalir.setBounds(20, 220, 410, 40);
        bSalir.setBackground(Color.WHITE);
        bSalir.setForeground(Color.BLACK);

        //TITULO SERVIDOR
        jLabel1.setFont(new java.awt.Font("Spectral", 1, 12));
        jLabel1.setText("SERVIDOR TCP");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(200, 10, 160, 17);

        // ENTRADA DE MENSAJES
        mensajesTxt.setColumns(25);
        mensajesTxt.setRows(5);

        jScrollPane1.setViewportView(mensajesTxt); // Configura el scroll pane para mostrar el área de texto.
        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(20, 110, 410, 100);

        // CONFIGURACIONES VENTANA
        setSize(new java.awt.Dimension(465, 320));
        this.getContentPane().setBackground(Color.WHITE); // 
        setLocationRelativeTo(null); // Centra la ventana en la pantalla.
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PrincipalSrv().setVisible(true);
            }
        });
    }

    private void bIniciarActionPerformed(java.awt.event.ActionEvent evt) {
        iniciarServidor(); // Llama al método para iniciar el servidor.
    }

    private void bSalirActionPerformed(java.awt.event.ActionEvent evt) {

        System.exit(0); // Sale de la aplicación
    }

    private void iniciarServidor() {
        // Inicia el servidor en un nuevo hilo
        threadPool = Executors.newCachedThreadPool(); // Crea un pool de hilos para manejar conexiones concurrentes
        new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress addr = InetAddress.getLocalHost(); // Obtiene la dirección IP local del servidor
                    serverSocket = new ServerSocket(PORT); // Crea un ServerSocket para escuchar conexiones en el puerto especificado
                    mensajesTxt.append("Servidor TCP en ejecución: " + addr + " ,Puerto " + serverSocket.getLocalPort() + "\n");
                    log("Servidor TCP iniciado en: " + addr + ", Puerto: " + serverSocket.getLocalPort());

                    while (true) {
                        // Acepta una conexión entrante de un cliente
                        Socket clientSocket = serverSocket.accept();
                        log("Nueva conexión aceptada desde: " + clientSocket.getInetAddress());
                        // Asigna el manejo del cliente a un hilo del pool
                        threadPool.execute(new ClientHandler(clientSocket));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(); // Imprime la traza del error en caso de excepción
                    InetAddress addr;
                    log("Error en el servidor: " + ex.getMessage());

                    try {
                        addr = InetAddress.getLocalHost(); // Obtiene la dirección IP local del servidor
                        log("Servidor TCP desconectado: " + addr + ", Puerto: " + serverSocket.getLocalPort());
                        mensajesTxt.append("Servidor TCP desconectado: " + addr + " ,Puerto " + serverSocket.getLocalPort() + "\n"); // Muestra mensaje de error en la interfaz

                    } catch (UnknownHostException ex1) {
                        ex.printStackTrace(); // Imprime la traza del error en caso de excepción
                    }

                }
            }
        }).start(); // Inicia el hilo
    }

    // Método para imprimir mensajes de log
    private void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String timestamp = sdf.format(new Date());
        System.out.println("[SERVER " + timestamp + "] " + message);
        mensajesTxt.append(message + "\n");
    }

    private class ClientConnection {

        // Objeto para enviar texto al servidor
        PrintWriter textOut;
        // Objeto para enviar datos binarios al servidor
        DataOutputStream dataOut;

        // Constructor de la clase ClientConnection
        ClientConnection(PrintWriter textOut, DataOutputStream dataOut) {
            // Asigna el PrintWriter proporcionado al campo textOut
            this.textOut = textOut;
            // Asigna el DataOutputStream proporcionado al campo dataOut
            this.dataOut = dataOut;
            log("ClientConnection creada con PrintWriter y DataOutputStream.");
        }
    }

    private class ClientHandler implements Runnable {

        private final Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                // Inicializa los flujos de entrada y salida para la comunicación con el cliente
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                dataIn = new DataInputStream(clientSocket.getInputStream());
                dataOut = new DataOutputStream(clientSocket.getOutputStream());

                // Lee el nombre del cliente desde el flujo de entrada
                clientName = in.readLine();
                log("Cliente conectado: " + clientName);

                // Sincroniza el acceso al mapa de conexiones de clientes
                synchronized (clientConnections) {
                    // Añade la nueva conexión de cliente al mapa
                    clientConnections.put(clientName, new ClientConnection(out, dataOut));
                    // Envía la lista actualizada de usuarios a todos los clientes
                    enviarListaUsuarios();
                }

                // Notifica que el cliente se ha unido en el área de mensajes
                mensajesTxt.append(clientName + " se ha unido.\n");

                String line;
                // Lee y procesa los mensajes enviados por el cliente
                while ((line = in.readLine()) != null) {
                    log("Mensaje recibido de " + clientName + ": " + line);

                    if (line.startsWith("FILE:")) {
                        // Maneja la transferencia de archivo
                        String[] parts = line.split(":", 4);
                        if (parts.length == 4) {
                            String destinatario = parts[1];
                            String fileName = parts[2];
                            long fileSize = Long.parseLong(parts[3]);
                            log("Iniciando transferencia de archivo: " + fileName + " de " + clientName + " a " + destinatario);
                            recibirYEnviarArchivo(destinatario, fileName, fileSize);
                        }
                    } else if (line.startsWith("TO:")) {
                        // Maneja mensajes de texto privados
                        String[] parts = line.split(":", 3);
                        if (parts.length == 3) {
                            String destinatario = parts[1].trim();
                            String mensaje = parts[2].trim();
                            log("Enviando mensaje privado de " + clientName + " a " + destinatario);
                            enviarMensajePrivado(clientName, destinatario, mensaje);
                        }
                    }
                }
            } catch (IOException ex) {
                // Maneja errores de entrada/salida y registra el error
                log("Error en la conexión con " + clientName + ": " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                // Se ejecuta al finalizar el bloque try, ya sea por éxito o por excepción
                try {
                    // Cierra el socket del cliente
                    clientSocket.close();
                    log("Conexión cerrada para " + clientName);
                } catch (IOException e) {
                    // Maneja errores al cerrar el socket y registra el error
                    log("Error al cerrar la conexión de " + clientName + ": " + e.getMessage());
                    e.printStackTrace();
                }
                // Sincroniza el acceso al mapa de conexiones de clientes
                synchronized (clientConnections) {
                    // Elimina la conexión del cliente del mapa
                    clientConnections.remove(clientName);
                    if (clientName != null) {
                        // Notifica que el cliente se ha desconectado en el área de mensajes
                        mensajesTxt.append(clientName + " se ha desconectado.\n");
                        log(clientName + " se ha desconectado.");
                        // Envía la lista actualizada de usuarios a todos los clientes
                        enviarListaUsuarios();
                    }
                }
            }
        }

        private void recibirYEnviarArchivo(String destinatario, String fileName, long fileSize) throws IOException {
            // Buffer para leer el archivo en bloques de 4096 bytes
            byte[] buffer = new byte[4096];
            // Variable para llevar el control de los bytes recibidos
            long bytesReceived = 0;
            // Obtiene la conexión del destinatario desde el mapa de conexiones
            ClientConnection destinatarioConn = clientConnections.get(destinatario);

            if (destinatarioConn != null) {
                // Notifica al destinatario sobre el archivo entrante
                destinatarioConn.textOut.println("INCOMING_FILE:" + clientName + ":" + fileName + ":" + fileSize);
                log("Notificación de archivo entrante enviada a " + destinatario + ": " + fileName + ", tamaño: " + fileSize + " bytes");

                // Enviar información del archivo al destinatario
                //destinatarioConn.dataOut.writeUTF(fileName);
                //destinatarioConn.dataOut.writeLong(fileSize);
                log("Información del archivo enviada a " + destinatario + ": " + fileName + ", tamaño: " + fileSize + " bytes");

                // Transferir el archivo al destinatario
                while (bytesReceived < fileSize) {
                    // Lee el archivo desde el flujo de entrada del cliente
                    int bytesRead = dataIn.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesReceived));
                    if (bytesRead == -1) {
                        // Fin de archivo, salir del bucle
                        log("Proceso de esritura vdsvggvdf<gvdfgvsdf");
                        break;
                    }
                    // Escribe los bytes leídos en el flujo de salida del destinatario
                    destinatarioConn.dataOut.write(buffer, 0, bytesRead);
                    // Actualiza el total de bytes recibidos
                    bytesReceived += bytesRead;
                    log("Progreso de transferencia: " + (bytesReceived * 100 / fileSize) + "%");
                }
                // Asegura que todos los datos se envíen al destinatario
                destinatarioConn.dataOut.flush();
                log("Archivo " + fileName + " transferido de " + clientName + " a " + destinatario + ". Tamaño: " + fileSize + " bytes");
                mensajesTxt.append("Archivo " + fileName + " transferido de " + clientName + " a " + destinatario + "\n");
            } else {
                // Si el destinatario no está conectado, informa al remitente
                out.println("SYSTEM:El destinatario " + destinatario + " no está conectado. No se pudo enviar el archivo.");
                log("El destinatario " + destinatario + " no está conectado. No se pudo enviar el archivo.");
            }
        }

        private void enviarMensajePrivado(String remitente, String destinatario, String mensaje) {
            // Obtiene la conexión del destinatario desde el mapa de conexiones
            ClientConnection destinatarioConn = clientConnections.get(destinatario);

            if (destinatarioConn != null) {
                // Envía el mensaje privado al destinatario en el formato "FROM:remitente:mensaje"
                destinatarioConn.textOut.println("FROM:" + remitente + ":" + mensaje);
                log("Mensaje privado enviado de " + remitente + " a " + destinatario + ": " + mensaje);
            } else {
                // Si el destinatario no está conectado, informa al remitente
                out.println("SYSTEM:El destinatario " + destinatario + " no está conectado. No se pudo enviar el mensaje.");
                log("No se pudo enviar mensaje privado de " + remitente + " a " + destinatario + ": destinatario no conectado.");
            }

            // Actualiza el área de mensajes con el mensaje enviado
            mensajesTxt.append(remitente + " a " + destinatario + ": " + mensaje + "\n");
        }

        private void enviarListaUsuarios() {
            // Crea un StringBuilder para construir la lista de usuarios
            StringBuilder userList = new StringBuilder("USERLIST:");

            // Itera sobre las claves del mapa de conexiones de clientes (nombres de usuarios)
            for (String user : clientConnections.keySet()) {
                // Añade cada nombre de usuario a la lista, seguido de una coma
                userList.append(user).append(",");
                // Registra la actualización de la lista de usuarios
                log("Lista de usuarios actualizada: " + user);
            }

            // Elimina la última coma del StringBuilder, si es necesario
            if (userList.length() > 9) { // 9 es la longitud de "USERLIST:"
                userList.setLength(userList.length() - 1);
            }

            // Envía la lista de usuarios a todos los clientes conectados
            for (ClientConnection conn : clientConnections.values()) {
                conn.textOut.println(userList.toString());
                // Registra el envío de la lista de usuarios a cada cliente
                log("Lista de usuarios enviada a cliente.");
            }
        }

    }

    private javax.swing.JButton bIniciar;
    private javax.swing.JButton bSalir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextArea mensajesTxt;
    private javax.swing.JScrollPane jScrollPane1;
}
