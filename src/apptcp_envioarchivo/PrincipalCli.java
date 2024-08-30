package apptcp_envioarchivo;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

public class PrincipalCli extends javax.swing.JFrame {

    private final int PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DataOutputStream dos;
    private DataInputStream dis;
    private File archivoSeleccionado;
    private final String clienteNombre;
    private final DefaultListModel<String> usuariosModel;
    private final HashMap<String, StringBuilder> conversaciones = new HashMap<>();

    public PrincipalCli(String clienteNombre) {
        this.clienteNombre = clienteNombre;
        log("Inicializando PrincipalCli para el cliente: " + clienteNombre);
        initComponents();
        Estado("DESCONECTADO");
        Nombre(clienteNombre);
        usuariosModel = new DefaultListModel<>();
        usuariosList.setModel(usuariosModel);
        jScrollPane2.setViewportView(usuariosList);
        log("Inicialización de PrincipalCli completada");
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        this.setTitle("Cliente");

        bConectar = new javax.swing.JButton();
        jLabelNombre = new javax.swing.JLabel();
        jLabelEstado = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mensajesTxt = new javax.swing.JTextArea();
        mensajeTxt = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btEnviar = new javax.swing.JButton();
        btArchivo = new javax.swing.JButton();
        bDesconectar = new javax.swing.JButton();
        bSalir = new javax.swing.JButton();
        usuariosList = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane(usuariosList);
        jLabelDestinatario = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        // BOTON CONECTAR
        bConectar.setFont(new java.awt.Font("Spectral", 1, 11));
        bConectar.setText("CONECTAR");
        bConectar.addActionListener((ActionEvent evt) -> bConectarActionPerformed(evt));
        getContentPane().add(bConectar);
        bConectar.setBounds(200, 40, 200, 30);
        bConectar.setBackground(Color.WHITE); // Fondo blanco
        bConectar.setForeground(Color.BLACK); // Texto negro

        // TITULO CLIENTE CON ESTADO
        jLabelNombre.setFont(new java.awt.Font("Spectral", 2, 10));
        getContentPane().add(jLabelNombre);
        jLabelNombre.setBounds(45, 10, 300, 17);
        jLabelEstado.setFont(new java.awt.Font("Spectral", 2, 10));
        getContentPane().add(jLabelEstado);
        jLabelEstado.setBounds(420, 10, 300, 17);

        // LISTA DE USUARIOS CONECTADOS
        usuariosList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        usuariosList.addListSelectionListener((ListSelectionEvent evt) -> usuariosListValueChanged(evt));
        jScrollPane2.setViewportView(usuariosList);
        getContentPane().add(jScrollPane2);
        jScrollPane2.setBounds(20, 90, 150, 170);

        // NOMBRE DEL DESTINATARIO
        jLabelDestinatario.setFont(new java.awt.Font("Spectral", 1, 11));
        jLabelDestinatario.setText("Destinatario: ");
        getContentPane().add(jLabelDestinatario);
        jLabelDestinatario.setBounds(20, 65, 200, 30);

        // MENSAJE RECIBIDO
        mensajesTxt.setColumns(20);
        mensajesTxt.setRows(5);
        mensajesTxt.setEnabled(false);
        jScrollPane1.setViewportView(mensajesTxt);
        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(180, 90, 350, 170);

        // MENSAJE A ENVIAR
        jLabel2.setFont(new java.awt.Font("Spectral", 1, 11));
        jLabel2.setText("Mensaje:");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(20, 270, 120, 30);

        mensajeTxt.setFont(new java.awt.Font("Spectral", 0, 11));
        getContentPane().add(mensajeTxt);
        mensajeTxt.setBounds(20, 300, 320, 30);

        // BOTON ENVIAR
        btEnviar.setFont(new java.awt.Font("Spectral", 1, 11));
        btEnviar.setText("ENVIAR");
        btEnviar.addActionListener((ActionEvent evt) -> btEnviarActionPerformed(evt));
        getContentPane().add(btEnviar);
        btEnviar.setBounds(440, 300, 80, 30);
        btEnviar.setBackground(Color.WHITE); // Fondo blanco
        btEnviar.setForeground(Color.BLACK); // Texto negro

        // BOTON CARGAR ARCHIVO
        btArchivo.setFont(new java.awt.Font("Spectral", 1, 11));
        btArchivo.setText("ARCHIVO");
        btArchivo.addActionListener((ActionEvent evt) -> bCargarArchivoActionPerformed(evt));
        getContentPane().add(btArchivo);
        btArchivo.setBounds(345, 300, 90, 30);
        btArchivo.setBackground(Color.WHITE); // Fondo blanco
        btArchivo.setForeground(Color.BLACK); // Texto negro

        // BOTON DESCONECTAR
        bDesconectar.setFont(new java.awt.Font("Spectral", 1, 11));
        bDesconectar.setText("DESCONECTAR");
        bDesconectar.addActionListener((ActionEvent evt) -> bDesconectarActionPerformed(evt));
        getContentPane().add(bDesconectar);
        bDesconectar.setBounds(120, 350, 150, 30);
        bDesconectar.setBackground(Color.WHITE); // Fondo blanco
        bDesconectar.setForeground(Color.BLACK); // Texto negro

        // BOTON SALIR
        bSalir.setFont(new java.awt.Font("Spectral", 1, 11));
        bSalir.setText("SALIR");
        bSalir.addActionListener((ActionEvent evt) -> bSalirActionPerformed(evt));
        getContentPane().add(bSalir);
        bSalir.setBounds(280, 350, 150, 30);
        bSalir.setBackground(Color.WHITE); // Fondo blanco
        bSalir.setForeground(Color.BLACK); // Texto negro

        // DIMENSIONES VENTANA
        setSize(new java.awt.Dimension(560, 435));
        setLocationRelativeTo(null);
        this.getContentPane().setBackground(Color.WHITE);
    }

    private void Estado(String status) {
        // Actualiza el estado del cliente en la interfaz gráfica
        jLabelEstado.setText(status);
    }

    private void Nombre(String clienteNombre) {
        // Actualiza el nombre del cliente en la interfaz gráfica
        jLabelNombre.setText("CLIENTE: " + clienteNombre);
    }

    private void bConectarActionPerformed(java.awt.event.ActionEvent evt) {
        // Acción cuando se presiona el botón "Conectar"
        if (clienteNombre.isEmpty()) { // Verificar si el nombre del cliente está vacío
            JOptionPane.showMessageDialog(this, "Por favor, ingrese su nombre."); // Mostrar mensaje de advertencia
            log("Intento de conexión fallido: nombre de cliente vacío");
            return;
        }
        log("Iniciando conexión al servidor");
        conectar(); // Llamar al método para conectar al servidor
    }

    private void btEnviarActionPerformed(java.awt.event.ActionEvent evt) {
        log("Intentando enviar mensaje");
        // Acción cuando se presiona el botón "Enviar"
        enviarMensaje(); // Llamar al método para enviar un mensaje
    }

    private void bDesconectarActionPerformed(java.awt.event.ActionEvent evt) {
        log("Iniciando desconexión del servidor");
        closeConnection();
        Estado("DESCONECTADO");
        bConectar.setEnabled(true);
        bDesconectar.setEnabled(false);
        mensajeTxt.setEnabled(false);
        btEnviar.setEnabled(false);
        btArchivo.setEnabled(false);
        JOptionPane.showMessageDialog(this, "Desconectado del servidor.");
        log("Desconexión completada");
    }

    private void bSalirActionPerformed(java.awt.event.ActionEvent evt) {
        // Acción cuando se presiona el botón "Salir"
        closeConnection(); // Cerrar la conexión si está abierta
        log("Aplicación cerrándose");
        System.exit(0); // Salir de la aplicación
    }

    private void bCargarArchivoActionPerformed(java.awt.event.ActionEvent evt) {
        // Crea un nuevo objeto JFileChooser para permitir al usuario seleccionar un archivo.
        JFileChooser fileChooser = new JFileChooser();

        // Muestra el cuadro de diálogo para abrir un archivo y captura el resultado de la acción del usuario.
        int result = fileChooser.showOpenDialog(this);

        // Verifica si el usuario seleccionó un archivo (opción "Aprobar").
        if (result == JFileChooser.APPROVE_OPTION) {
            // Obtiene el archivo seleccionado por el usuario.
            archivoSeleccionado = fileChooser.getSelectedFile();

            // Define una lista de extensiones válidas para archivos planos.
            String[] extensionesValidas = {".txt", ".csv", ".log", ".dat"};
            String archivoNombre = archivoSeleccionado.getName().toLowerCase();

            // Verifica si el archivo tiene una de las extensiones válidas.
            boolean esArchivoPlano = false;
            for (String extension : extensionesValidas) {
                if (archivoNombre.endsWith(extension) && archivoNombre.equals(archivoNombre.substring(0, archivoNombre.length() - extension.length()) + extension)) {
                    esArchivoPlano = true;
                    break;
                }
            }

            if (!esArchivoPlano) {
                // Muestra un mensaje de advertencia si el archivo no es válido.
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un archivo plano válido. (Extensiones válidas: .txt, .csv, .log, .dat)", "Archivo no válido", JOptionPane.ERROR_MESSAGE);
                // Registra un mensaje en el log indicando que el archivo no es válido.
                log("Intento de carga fallido: el archivo seleccionado no es un archivo plano válido.");
                // Sale del método si el archivo no es válido.
                return;
            }

            // Obtiene el usuario seleccionado en la lista de usuarios.
            String destinatario = usuariosList.getSelectedValue();

            // Verifica si se ha seleccionado un destinatario válido.
            if (destinatario == null || destinatario.isEmpty()) {
                // Muestra un mensaje de advertencia si no se ha seleccionado un destinatario.
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un destinatario.");
                // Registra un mensaje en el log indicando que el intento de envío de archivo falló.
                log("Intento de envío de archivo fallido: destinatario no seleccionado");
                // Sale del método si no se ha seleccionado un destinatario.
                return;
            }

            // Registra un mensaje en el log indicando que el envío de archivo está a punto de comenzar.
            log("Iniciando envío de archivo: " + archivoSeleccionado.getName() + " a " + destinatario);

            // Llama al método para enviar el archivo al destinatario seleccionado.
            enviarArchivo(destinatario, archivoSeleccionado);
        }
    }

    private void conectar() {
        try {
            // Verifica si el socket es nulo o está cerrado, lo que significa que no está conectado.
            if (socket == null || socket.isClosed()) {
                // Registra en el log el intento de conexión al servidor en la dirección y puerto especificados.
                log("Conectando al servidor en localhost:" + PORT);

                // Crea un nuevo socket para conectarse al servidor en "localhost" en el puerto especificado.
                socket = new Socket("localhost", PORT);

                // Inicializa los flujos de entrada y salida para comunicarse con el servidor.
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());

                // Envía el nombre del cliente al servidor para identificarse.
                out.println(clienteNombre);

                // Registra en el log que la conexión se ha establecido y que se ha enviado el nombre del cliente.
                log("Conexión establecida, enviando nombre de cliente: " + clienteNombre);

                // Inicia un nuevo hilo para escuchar mensajes del servidor.
                new Thread(() -> {
                    try {
                        String fromServer;
                        // Lee mensajes del servidor mientras la conexión esté abierta.
                        while ((fromServer = in.readLine()) != null) {
                            // Registra en el log el mensaje recibido del servidor.
                            log("Mensaje recibido del servidor: " + fromServer);

                            // Procesa el mensaje recibido según su tipo.
                            if (fromServer.startsWith("USERLIST:")) {
                                // Actualiza la lista de usuarios conectados.
                                actualizarListaUsuarios(fromServer);
                            } else if (fromServer.startsWith("INCOMING_FILE:")) {
                                // Maneja un archivo entrante.
                                manejarArchivoEntrante(fromServer);
                            } else {
                                final String mensajeFinal = fromServer;
                                // Usa SwingUtilities para actualizar la interfaz gráfica en el hilo de eventos.
                                SwingUtilities.invokeLater(() -> procesarMensajeEntrante(mensajeFinal));
                            }
                        }
                    } catch (IOException ex) {
                        // Registra en el log cualquier error de entrada/salida durante la lectura de mensajes.
                        log("Error en la conexión con el servidor: " + ex.getMessage());
                        // Llama al método para intentar reconectar.
                        reconectar();
                    }
                }).start();

                // Actualiza el estado del cliente a "CONECTADO" en la interfaz gráfica.
                Estado("CONECTADO");
                // Registra en el log que el cliente se ha conectado exitosamente.
                log("Cliente conectado exitosamente");
                // Actualiza el estado de los botones y campos de texto en la interfaz gráfica.
                bConectar.setEnabled(false);
                bDesconectar.setEnabled(true);
                mensajeTxt.setEnabled(true);
                btEnviar.setEnabled(true);
                btArchivo.setEnabled(true);
                usuariosList.setEnabled(true);
            }
        } catch (IOException e) {
            // Registra en el log cualquier error que ocurra al intentar conectar con el servidor.
            log("Error al conectar con el servidor: " + e.getMessage());
            // Muestra un mensaje de advertencia al usuario si la conexión falla.
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor. Verifique la conexión e intente nuevamente.");
            // Llama al método para intentar reconectar.
            reconectar();
        }
    }

    private void enviarMensaje() {
        // Método para enviar un mensaje al servidor
        log("Intentando enviar mensaje"); // Registra el intento de enviar un mensaje.

        if (out != null) { // Verifica si el flujo de salida no es nulo (es decir, si está conectado al servidor).
            String destinatario = usuariosList.getSelectedValue(); // Obtiene el destinatario seleccionado de la lista de usuarios.

            if (destinatario == null || destinatario.isEmpty()) { // Verifica si se ha seleccionado un destinatario.
                log("Envío de mensaje fallido: destinatario no seleccionado"); // Registra el fallo en el envío del mensaje.
                JOptionPane.showMessageDialog(this, "Por favor, seleccione un destinatario."); // Muestra un mensaje de advertencia.
                return; // Sale del método si no hay destinatario.
            }

            String mensaje = mensajeTxt.getText(); // Obtiene el mensaje del campo de texto.

            if (mensaje.isEmpty()) { // Verifica si el mensaje está vacío.
                log("Envío de mensaje fallido: mensaje vacío"); // Registra el fallo en el envío del mensaje.
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un mensaje."); // Muestra un mensaje de advertencia.
                return; // Sale del método si el mensaje está vacío.
            }

            log("Enviando mensaje a " + destinatario + ": " + mensaje); // Registra el mensaje que se está enviando.

            // Envía el mensaje al servidor con el formato "TO:DESTINATARIO:mensaje".
            out.println("TO:" + destinatario + ":" + mensaje);

            // Actualiza el área de mensajes con el mensaje enviado.
            // Usa un StringBuilder para construir la conversación con el destinatario.
            StringBuilder conversacion = conversaciones.computeIfAbsent(destinatario, k -> new StringBuilder());
            conversacion.append(clienteNombre + ": " + mensaje + "\n");

            // Agrega el mensaje enviado al área de mensajes del cliente.
            mensajesTxt.append("Tú: " + mensaje + "\n");

            // Limpia el campo de texto después de enviar el mensaje.
            mensajeTxt.setText("");

            log("Mensaje enviado exitosamente"); // Registra que el mensaje se ha enviado exitosamente.
        } else {
            log("Envío de mensaje fallido: no conectado al servidor"); // Registra el fallo en el envío del mensaje si no está conectado.
            JOptionPane.showMessageDialog(this, "No estás conectado al servidor."); // Muestra un mensaje de advertencia si no está conectado.
        }
    }

    private void enviarArchivo(String destinatario, File archivo) {
        try {
            long fileSize = archivo.length(); // Obtiene el tamaño del archivo en bytes.
            String fileName = archivo.getName(); // Obtiene el nombre del archivo.

            // Registra el inicio del proceso de envío del archivo.
            log("Enviando información del archivo al servidor: " + fileName + ", tamaño: " + fileSize + " bytes");

            // Enviar información del archivo al servidor en el formato "FILE:DESTINATARIO:NOMBRE_ARCHIVO:TAMAÑO".
            out.println("FILE:" + destinatario + ":" + fileName + ":" + fileSize);

            // Abre un flujo de entrada para leer el archivo.
            FileInputStream fis = new FileInputStream(archivo);
            byte[] buffer = new byte[4096]; // Buffer para leer los datos del archivo en bloques de 4096 bytes.
            int bytesRead; // Variable para almacenar el número de bytes leídos en cada iteración.
            long totalBytesRead = 0; // Variable para llevar el conteo total de bytes leídos.

            // Lee y envía el archivo en bloques hasta el final.
            while ((bytesRead = fis.read(buffer)) != -1) { // Lee el archivo en bloques.
                dos.write(buffer, 0, bytesRead); // Escribe los datos leídos en el DataOutputStream.
                totalBytesRead += bytesRead; // Actualiza el total de bytes leídos.
                // Registra el progreso del envío en porcentaje.
                log("Progreso de envío: " + (totalBytesRead * 100 / fileSize) + "%");
            }

            // Asegura que todos los datos se envíen al servidor.
            dos.flush();
            // Cierra el flujo de entrada del archivo.
            fis.close();

            // Registra que el archivo se ha enviado exitosamente.
            log("Archivo " + fileName + " enviado a " + destinatario);
            // Actualiza el área de mensajes con la notificación del archivo enviado.
            mensajesTxt.append("Archivo " + fileName + " enviado a " + destinatario + "\n");

            // Actualiza el área de mensajes con el mensaje enviado.
            // Usa un StringBuilder para construir la conversación con el destinatario.
            StringBuilder conversacion = conversaciones.computeIfAbsent(destinatario, k -> new StringBuilder());
            conversacion.append(clienteNombre + ": Archivo: " + fileName + "\n");
        } catch (IOException e) {
            // Registra cualquier error que ocurra durante el envío del archivo.
            log("Error al enviar el archivo: " + e.getMessage());
            e.printStackTrace(); // Imprime el rastreo de la excepción para depuración.
            // Muestra un mensaje de error al usuario.
            JOptionPane.showMessageDialog(this, "Error al enviar el archivo: " + e.getMessage());
        }
    }

    private void procesarMensajeEntrante(String mensaje) {
        // Procesar un mensaje entrante del servidor
        log("Procesando mensaje entrante: " + mensaje);

        if (mensaje.startsWith("FROM:")) {
            // Mensaje de tipo "FROM:REMITENTE:CONTENIDO"
            String[] parts = mensaje.split(":", 3); // Divide el mensaje en partes usando ':' como delimitador
            if (parts.length == 3) { // Verifica que el mensaje se haya dividido correctamente
                String remitente = parts[1]; // Extrae el remitente
                String contenido = parts[2]; // Extrae el contenido del mensaje

                // Registra la recepción del mensaje del remitente
                log("Mensaje recibido de " + remitente + ": " + contenido);

                // Actualiza la conversación del remitente
                StringBuilder conversacion = conversaciones.computeIfAbsent(remitente, k -> new StringBuilder());
                conversacion.append(remitente + ": " + contenido + "\n");

                // Actualiza el área de mensajes si el remitente es el destinatario actual
                String destinatarioActual = jLabelDestinatario.getText().replace("Destinatario: ", "").trim();
                if (remitente.equals(destinatarioActual)) {
                    mensajesTxt.append(remitente + ": " + contenido + "\n");
                } else {
                    // Notifica al usuario sobre el nuevo mensaje si no es el destinatario actual
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "Nuevo mensaje de " + remitente + ":\n" + contenido,
                                "Nuevo Mensaje",
                                JOptionPane.INFORMATION_MESSAGE);
                    });
                }
            }
        } else if (mensaje.startsWith("SYSTEM:")) {
            // Mensaje de tipo "SYSTEM:CONTENIDO_DEL_SISTEMA"
            log("Mensaje del sistema recibido: " + mensaje.substring(7)); // Registra el mensaje del sistema sin el prefijo "SYSTEM:"
            // Muestra mensajes del sistema (como notificaciones de no entrega)
            mensajesTxt.append(mensaje.substring(7) + "\n"); // Actualiza el área de mensajes con el mensaje del sistema
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        mensaje.substring(7),
                        "Notificación del Sistema",
                        JOptionPane.INFORMATION_MESSAGE);
            });
        } else {
            // Mensaje de formato desconocido o genérico
            log("Mensaje generico recibido: " + mensaje);
            mensajesTxt.append(""); // Muestra el mensaje genérico en el área de mensajes
        }
    }

    private void manejarArchivoEntrante(String mensaje) {
        // Divide el mensaje en partes usando ':' como delimitador
        String[] parts = mensaje.split(":", 4);
        if (parts.length == 4) { // Verifica que el mensaje se haya dividido correctamente
            String remitente = parts[1]; // Extrae el remitente del mensaje
            String fileName = parts[2]; // Extrae el nombre del archivo
            long fileSize = Long.parseLong(parts[3]); // Extrae el tamaño del archivo

            // Registra la recepción de la información del archivo
            log("Archivo entrante de " + remitente + ": " + fileName + ", tamaño: " + fileSize + " bytes");

            // Solicita al usuario confirmación para recibir el archivo
            int option = JOptionPane.showConfirmDialog(this,
                    "¿Desea recibir el archivo " + fileName + " de " + remitente + "?",
                    "Archivo entrante",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                // Si el usuario acepta, muestra un cuadro de diálogo para seleccionar la ubicación donde guardar el archivo
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileName));
                int result = fileChooser.showSaveDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    // Si el usuario elige una ubicación, inicia la recepción del archivo
                    File file = fileChooser.getSelectedFile();
                    log("Iniciando recepción del archivo: " + file.getAbsolutePath());
                    recibirArchivo(file, fileSize);
                } else {
                    // Si el usuario cancela la selección, rechaza el archivo
                    log("Recepción de archivo cancelada por el usuario");
                    rechazarArchivo(fileSize);
                    try {
                        // Descartar los bytes del archivo que se habían recibido pero no se van a guardar
                        dis.skipBytes((int) fileSize);
                    } catch (IOException e) {
                        e.printStackTrace(); // Registra el error si ocurre
                    }
                    log("Archivo rechazado por el usuario");
                    mensajesTxt.append("Archivo " + fileName + " de " + remitente + " rechazado.\n");
                }
            } else {
                // Si el usuario cancela la recepción del archivo
                log("Recepción de archivo cancelada por el usuario");
                rechazarArchivo(fileSize);
                try {
                    // Descartar los bytes del archivo que se habían recibido pero no se van a guardar
                    dis.skipBytes((int) fileSize);
                } catch (IOException e) {
                    e.printStackTrace(); // Registra el error si ocurre
                }
                mensajesTxt.append("Archivo " + fileName + " de " + remitente + " rechazado.\n");
            }
        }
    }

    private void recibirArchivo(File file, long fileSize) {
        try {
            // Abre un flujo de salida para escribir en el archivo
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[4096]; // Buffer para almacenar datos leídos
            long bytesReceived = 0; // Total de bytes recibidos hasta ahora
            int bytesRead; // Bytes leídos en cada operación

            // Mientras no se haya recibido todo el archivo
            while (bytesReceived < fileSize) {
                // Lee del flujo de entrada del servidor
                bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesReceived));
                if (bytesRead == -1) {
                    // Si no se lee ningún byte, se detiene el proceso
                    log("Fin del flujo de entrada alcanzado inesperadamente.");
                    break;
                }
                // Escribe los bytes leídos en el archivo
                fos.write(buffer, 0, bytesRead);
                bytesReceived += bytesRead; // Actualiza el total de bytes recibidos

                // Registra el progreso de recepción del archivo
                log("Progreso de recepción: " + (bytesReceived * 100 / fileSize) + "%");
            }
            // Registra que el archivo se ha recibido completamente
            log("Archivo " + file.getName() + " recibido. Tamaño: " + fileSize + " bytes");
            fos.close(); // Cierra el flujo de salida

            // Actualiza el área de mensajes con la información del archivo recibido
            mensajesTxt.append("Archivo " + file.getName() + " recibido y guardado.\n");

        } catch (IOException e) {
            // Registra cualquier excepción que ocurra durante la recepción del archivo
            e.printStackTrace();
            log("Error al recibir archivo: " + e.getMessage());
            // Muestra un mensaje de error al usuario
            JOptionPane.showMessageDialog(this, "Error al recibir el archivo: " + e.getMessage());
        }
    }

    private void rechazarArchivo(long fileSize) {
        try {
            log("Saltando " + fileSize + " bytes del archivo rechazado");
            dis.skipBytes((int) fileSize);
        } catch (IOException e) {
            log("Error al rechazar el archivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void actualizarListaUsuarios(String userListMessage) {
        log("Actualizando lista de usuarios"); // Registra el inicio del proceso de actualización de la lista de usuarios

        // Usa SwingUtilities.invokeLater para asegurar que la actualización de la interfaz de usuario se ejecute en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            // Limpia la lista de usuarios actual para preparar la actualización
            usuariosModel.clear();

            // Extrae la lista de usuarios del mensaje, omitiendo el prefijo "USERLIST:"
            String[] users = userListMessage.substring(9).split(",");

            // Recorre cada usuario en la lista extraída
            for (String user : users) {
                // Asegura que no se añada el nombre del cliente ni cadenas vacías a la lista
                if (!user.equals(clienteNombre) && !user.isEmpty()) {
                    usuariosModel.addElement(user); // Añade el usuario a la lista de usuarios conectados
                    log("Usuario añadido a la lista: " + user); // Registra el usuario añadido a la lista
                }
            }
        });
    }

    private void usuariosListValueChanged(javax.swing.event.ListSelectionEvent evt) {
        // Acción cuando cambia la selección en la lista de usuarios
        if (!evt.getValueIsAdjusting()) { // Verificar si la selección ha cambiado completamente
            // Obtiene el usuario seleccionado en la lista de usuarios
            String selectedUser = usuariosList.getSelectedValue();

            if (selectedUser != null) { // Verificar si se ha seleccionado un usuario válido
                log("Usuario seleccionado: " + selectedUser); // Registra el usuario seleccionado para depuración

                // Actualiza el texto del JLabel con el destinatario seleccionado
                jLabelDestinatario.setText("Destinatario: " + selectedUser);

                // Cargar la conversación previa con el usuario seleccionado
                StringBuilder conversacion = conversaciones.get(selectedUser);
                if (conversacion != null) { // Verificar si existe una conversación previa con el usuario
                    mensajesTxt.setText(conversacion.toString()); // Muestra la conversación previa en el área de mensajes
                    log("Cargada conversación previa con " + selectedUser); // Registra la carga de conversación para depuración
                } else {
                    mensajesTxt.setText(""); // Limpiar el área de mensajes si no hay conversación previa
                    log("No hay conversación previa con " + selectedUser); // Registra la ausencia de conversación previa
                }
            }
        }
    }

    private void reconectar() {
        log("Iniciando proceso de reconexión");
        Estado("DESCONECTADO"); // Actualizar estado a desconectado
        bConectar.setEnabled(true);
        bDesconectar.setEnabled(false);
        mensajeTxt.setEnabled(false);
        btEnviar.setEnabled(false);
        btArchivo.setEnabled(false);
    }

    private void closeConnection() {
        log("Cerrando proceso de conexión");
        // Método para cerrar la conexión al servidor
        try {
            if (socket != null) {
                //out.close(); // Cerrar el escritor
                //in.close(); // Cerrar el lector
                out.println(clienteNombre + " se ha desconectado.");
                log(clienteNombre + " se ha desconectado.");
                socket.close(); // Cerrar el socket
                Estado("DESCONECTADO"); // Actualizar el estado a desconectado
                bConectar.setEnabled(true); // Habilitar el botón de conectar
                bDesconectar.setEnabled(false); // Deshabilitar el botón de desconectar
                mensajeTxt.setEnabled(false); // Deshabilitar el campo de texto del mensaje
                btEnviar.setEnabled(false); // Deshabilitar el botón de enviar
                btArchivo.setEnabled(false); // Deshabilitar el botón de archivo
                usuariosList.setEnabled(false); // Deshabilitar la lista de usuarios
                SwingUtilities.invokeLater(() -> usuariosModel.clear()); // Limpiar la lista de usuarios
            }
        } catch (IOException ex) {
            log("Error en el proceso de cierre");
            ex.printStackTrace(); // Imprimir la traza de la excepción en caso de error
        }
    }

    // Método para imprimir mensajes de log
    private void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        System.out.println("[CLIENT " + clienteNombre + " " + timestamp + "] " + message);
    }

    // Componentes de la interfaz gráfica
    private javax.swing.JButton bConectar;
    private javax.swing.JButton bDesconectar;
    private javax.swing.JButton bSalir;
    private javax.swing.JButton btEnviar;
    private javax.swing.JButton btArchivo;
    private javax.swing.JLabel jLabelNombre;
    private javax.swing.JLabel jLabelEstado;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabelDestinatario;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea mensajesTxt;
    private javax.swing.JTextField mensajeTxt;
    private javax.swing.JList<String> usuariosList;

}
