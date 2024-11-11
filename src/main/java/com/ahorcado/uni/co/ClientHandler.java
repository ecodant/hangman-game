package com.ahorcado.uni.co;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable{
  private Socket socket;
  private ClientSide cliente;
  private ServerSide server;
  private BufferedReader reader;
  private PrintWriter writter;

    public ClientHandler(Socket socket, ServerSide server) {
        this.socket = socket;
        this.server = server;
    }

    public ClientHandler(Socket socket, ClientSide cliente) {
        this.socket = socket;
        this.cliente = cliente;
    }

    @Override
    public void run() {
        String mesajeAEnviar;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writter = new PrintWriter(socket.getOutputStream(), true);

            while ((mesajeAEnviar = reader.readLine()) != null) {
                // procesarMsg(mesajeAEnviar);
                System.out.println("Mensaje: " + mesajeAEnviar);
                if(server != null){
                    server.procesarRespuestaDelCliente(mesajeAEnviar);
                }
                else if (cliente != null){
                    cliente.procesarRespuestaDelServer(mesajeAEnviar);
                }
            }
        } catch (IOException ex) {
            cerrarTodo();
        }
    }

    public void enviarACliente(String contenido){
        if (writter != null) {
            writter.println(contenido);
        }
    }
    private void cerrarTodo(){
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
