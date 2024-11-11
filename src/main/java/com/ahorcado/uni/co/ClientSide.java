package com.ahorcado.uni.co;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSide {
    private PrintWriter writter;
    private BufferedReader reader;
    private Socket socket;
    private ClientHandler connectionServer;
    private JFrame frame;
    private JLabel palabraJuego;
    private JButton buttonEntrada;
    private JTextField entradaCliente;

    public ClientSide(Socket socket) {
        this.socket = socket;
        try {
            writter = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame = new JFrame("Ventana Cliente");
        palabraJuego = new JLabel("", SwingConstants.CENTER);
        palabraJuego.setFont(new Font("Arial", Font.BOLD, 24));
        entradaCliente = new JTextField(30);
    }

    private void conectarAlServidor() {
        new Thread(() -> {
            try {
                mostrarGUI();
//                socket = new Socket("localhost", 9806);
                connectionServer = new ClientHandler(socket, this);
                new Thread(connectionServer).start();
                System.out.println("Conectado con el Server");
            } catch (Exception e) {
                cerrarTodo();
            }
        }).start();
    }

    public void procesarRespuestaDelServer(String responseDelServer) {
        palabraJuego.setText(responseDelServer);
        buttonEntrada.setEnabled(true);
    }

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 9806);
            ClientSide client = new ClientSide(socket);
            client.conectarAlServidor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        
        // Panel principal con BoxLayout vertical
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Configurar componentes
        palabraJuego.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        entradaCliente.setMaximumSize(new Dimension(400, 40));
        entradaCliente.setFont(new Font("Arial", Font.PLAIN, 16));
        entradaCliente.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        buttonEntrada = new JButton("Enviar Respuesta");
        buttonEntrada.setFont(new Font("Arial", Font.BOLD, 16));
        buttonEntrada.setMaximumSize(new Dimension(200, 50));
        buttonEntrada.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonEntrada.setEnabled(false);
        buttonEntrada.addActionListener(e -> {
            String caracter = entradaCliente.getText();
            connectionServer.enviarACliente(caracter);
            entradaCliente.setText("");
        });

        // Agregar componentes con espaciado
        panel.add(Box.createVerticalGlue());
        panel.add(palabraJuego);
        panel.add(Box.createVerticalStrut(30));
        panel.add(entradaCliente);
        panel.add(Box.createVerticalStrut(30));
        panel.add(buttonEntrada);
        panel.add(Box.createVerticalGlue());
        
        frame.add(panel);
        frame.setVisible(true);
    }

    private void cerrarTodo() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writter != null) {
                writter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
