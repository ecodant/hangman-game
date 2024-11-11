package com.ahorcado.uni.co;

import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerSide {
    private ServerSocket serverSocket;
    private JFrame frame;
    private JPanel panel;
    private JLabel estadoConexion;
    private JLabel labelPalabraEnJuego;
    private JButton buttonComenzarJuego;
    private String palabraSeleccionada;
    private StringBuilder palabraDasheada;
    private List<ClientHandler> clientes;
    private List<String> listaPalabras;

    public ServerSide() {
        this.frame = new JFrame("Ahorcado - Servidor");
        this.panel = new JPanel();
        this.labelPalabraEnJuego = new JLabel("", SwingConstants.CENTER);
        this.listaPalabras = new ArrayList<>();
        this.clientes = new ArrayList<>();
        
        // Lista de palabras para el juego
        listaPalabras.add("perro");
        listaPalabras.add("gato");
        listaPalabras.add("ballena");
        listaPalabras.add("pajaro");
    }

    public static void main(String[] args) {
        ServerSide server = new ServerSide();
        server.arrancarServer();
    }

    private void mostrarGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Crear y configurar componentes
        buttonComenzarJuego = new JButton("Comenzar Juego");
        buttonComenzarJuego.setFont(new Font("Arial", Font.BOLD, 16));
        buttonComenzarJuego.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonComenzarJuego.setMaximumSize(new Dimension(200, 50));
        
        estadoConexion = new JLabel("Esperando por jugadores...", SwingConstants.CENTER);
        estadoConexion.setFont(new Font("Arial", Font.PLAIN, 12));
        estadoConexion.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        labelPalabraEnJuego = new JLabel("", SwingConstants.CENTER);
        labelPalabraEnJuego.setFont(new Font("Arial", Font.PLAIN, 22));
        labelPalabraEnJuego.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(estadoConexion);
        panel.add(labelPalabraEnJuego);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonComenzarJuego);
        panel.add(Box.createVerticalStrut(20));
        panel.add(Box.createVerticalGlue());

        buttonComenzarJuego.addActionListener(e -> {
            panel.remove(1);
            buttonComenzarJuego.setEnabled(false);
            comenzarJuego();
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private void arrancarServer() {
        try {
            mostrarGUI();
            serverSocket = new ServerSocket(9806);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("Un nuevo jugador se ha conectado");

                // Por cada socket del cliente crea un hilo para manejar su la conexion y los añade a la lista de clientes para luego enviar respuestas
                ClientHandler nuevoCliente = new ClientHandler(socket, this);
                clientes.add(nuevoCliente);
                Thread hiloConexionConCliente = new Thread(nuevoCliente);
                hiloConexionConCliente.start();
            }
        } catch (Exception e) {
            cerrarSocket();
        }
    }

    private void comenzarJuego() {
        palabraSeleccionada = listaPalabras.get(new Random().nextInt(listaPalabras.size()));
        palabraDasheada = new StringBuilder("*".repeat(palabraSeleccionada.length()));
        System.out.println("Palabra seleccionada para el juego: " + palabraSeleccionada);

        labelPalabraEnJuego.setText(palabraDasheada.toString());
        estadoConexion.setText("El juego comenzó");

        // Enviar la palabra "dasheada" a todos los clientes
        for (ClientHandler cliente : clientes) {
            cliente.enviarACliente(palabraDasheada.toString());
        }
    }

    public synchronized void procesarRespuestaDelCliente(String responseDelClient) {
        if (palabraSeleccionada == null || responseDelClient.isEmpty()) {
            return;
        }
        char caracterDelClient = responseDelClient.charAt(0);

        // Actualiza la palabra dasheada en los clientes
        for (int i = 0; i < palabraSeleccionada.length(); i++) {
            if (palabraSeleccionada.charAt(i) == caracterDelClient) {
                palabraDasheada.setCharAt(i, caracterDelClient);
            }
        }

        // Actualiza el label de la palabra en el Server
        labelPalabraEnJuego.setText(palabraDasheada.toString());

        if (palabraDasheada.toString().equals(palabraSeleccionada)) {
            // Si la palabra ha sido adivinada, se informa a todos los clientes
            for (ClientHandler cliente : clientes) {
                cliente.enviarACliente("¡Palabra adivinada, era: " + palabraSeleccionada + "!");
            }
            labelPalabraEnJuego.setText(palabraSeleccionada);
            cerrarSocket();
        } else {
                for (ClientHandler cliente : clientes) {
                    cliente.enviarACliente(palabraDasheada.toString());
                }
        }
    }

    public void cerrarSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
