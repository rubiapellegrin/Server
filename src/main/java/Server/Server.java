/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author rubia
 */
public class Server {

    private ServerSocket serverSocket;
    List lista = new ArrayList();
    String titulo;
    String descricao;
    int tamanho;
    String prioridade;
    ArrayList<TrataConexao> clientes;
    ArrayList<Thread> threads;
    String[][] dadosClassi = new String[20][7];
    String[][] dadosLances = new String[20][8];
    int cont = 0;
    int contLances = 0;
    int semaforo = 0;
    int auxiliarSema;
    int aceito = 0;

    public Server() {
        this.threads = new ArrayList<>();
        this.clientes = new ArrayList<>();

    }

    public void criaServerSocket(int porta) throws IOException {
        serverSocket = new ServerSocket(porta);
    }

    public Socket esperaConexao() throws IOException {
        Socket socket = serverSocket.accept();
        return socket;
    }

    public void Salvar(int id, String titulo, String descricao, int tamanho, String prioridade,
            String EstadoPubli, Double valor) {

        dadosClassi[cont][0] = String.valueOf(id);
        dadosClassi[cont][1] = titulo;
        dadosClassi[cont][2] = descricao;
        dadosClassi[cont][3] = String.valueOf(tamanho);
        dadosClassi[cont][4] = prioridade;
        dadosClassi[cont][5] = EstadoPubli;
        dadosClassi[cont][6] = String.valueOf(valor);
        cont++;
    }

    public String listar() {
        String dadosAuxiliar = "";
        for (int j = 0; j <= cont; j++) {

            for (int k = 0; k < 7; k++) {
                if (dadosClassi[j][k] != null) {
                    dadosAuxiliar += " | " + dadosClassi[j][k];
                }
            }
            dadosAuxiliar += "\n";
        }
        return dadosAuxiliar;
    }

    public String Buscar(String ti) {
        String dados = "";
        for (int j = 0; j <= cont; j++) {

            for (int k = 0; k < 7; k++) {
                if (dadosClassi[j][1] == null ? ti == null : dadosClassi[j][1].equals(ti)) {
                    dados += " | " + dadosClassi[j][k];
                }
            }
            dados += "\n";
        }
        return dados;

    }

    public String Excluir(String ti) {
        String auxExcluir = "";
        for (int j = 0; j <= cont; j++) {

            for (int k = 0; k < 7; k++) {
                if (dadosClassi[j][1] == null ? ti == null : dadosClassi[j][1].equals(ti)) {
                    dadosClassi[j][0] = null;
                    dadosClassi[j][1] = null;
                    dadosClassi[j][2] = null;
                    dadosClassi[j][3] = null;
                    dadosClassi[j][4] = null;
                    dadosClassi[j][5] = null;
                    dadosClassi[j][6] = null;
                }
            }

        }
        return auxExcluir;
    }

    public synchronized String Compra(String ti, Double valor) throws InterruptedException {
        semaforo = 1;

            
        for (int j = 0; j <= cont; j++) {

            for (int k = 0; k < 7; k++) {
                if (dadosClassi[j][1] == null ? ti == null : dadosClassi[j][1].equals(ti)) {
                    dadosLances[contLances][0] = dadosClassi[j][0];
                    dadosLances[contLances][1] = dadosClassi[j][1];
                    dadosLances[contLances][2] = dadosClassi[j][2];
                    dadosLances[contLances][3] = dadosClassi[j][3];
                    dadosLances[contLances][4] = dadosClassi[j][4];
                    dadosLances[contLances][5] = dadosClassi[j][5];
                    dadosLances[contLances][6] = dadosClassi[j][6];
                    dadosLances[contLances][7] = String.valueOf(valor);

                }
            }

        }
        auxiliarSema = contLances;
        contLances++;
        
        while (semaforo > 0) {

            wait();
            //wait();

        }
        if (aceito == 1) {
            return "\n Lance aceito!";

        } else {
            return "\n Lance recusado!";
        }
        
    }

    public String ListarLances(int id) {
        String dadosAuxiliar = "";
        for (int j = 0; j <= contLances; j++) {

            for (int k = 0; k < 8; k++) {
                if (dadosLances[j][0] == null ? null == (String.valueOf(id)) : dadosLances[j][0].equals(String.valueOf(id))) {
                    if (dadosLances[j][k] != null) {
                        dadosAuxiliar += " | " + dadosLances[j][k];
                    }
                }

            }
            dadosAuxiliar += "\n";
        }
        return dadosAuxiliar;

    }

    public synchronized void ConfirmarLance(String ti, Double valor) {
        for (int j = 0; j <= cont; j++) {

            for (int k = 0; k < 7; k++) {
                if (dadosLances[j][1] == null ? ti == null : dadosLances[j][1].equals(ti) && dadosLances[j][7].equals(String.valueOf(valor))) {
                    dadosLances[j][5] = "EFETIVADO";
                    semaforo = 0;
                    if (j == auxiliarSema) {
                        semaforo = 0;
                        aceito = 1;
                        notifyAll();
                    }
                }

            }

        }
        for (int j = 0; j <= cont; j++) {

            for (int k = 0; k < 7; k++) {
                if (dadosClassi[j][1] == null ? ti == null : dadosClassi[j][1].equals(ti)) {
                    dadosClassi[j][5] = "COMPRADO";
                }
            }

        }
    }

    public synchronized void NegarLance(String ti, Double valor) {
        for (int j = 0; j <= cont; j++) {

            for (int k = 0; k < 8; k++) {
                if (dadosLances[j][1] == null ? ti == null : dadosLances[j][1].equals(ti) && dadosLances[j][7].equals(String.valueOf(valor))) {
                    dadosLances[j][5] = "CANCELADO";
                    semaforo = 0;
                    
                    if (j == auxiliarSema) {
                        semaforo = 0;
                        aceito = 0;
                        notifyAll();
                    }
                }
            }

        }
    }

    public void connectionLoop() throws IOException {
        int id = 0;
        while (true) {
            System.out.println("Aguardando conexao..");
            Socket socket = esperaConexao();
            System.out.println("Cliente " + id + " conectado!");
            TrataConexao tc = new TrataConexao(this, socket, id++);
            Thread th = new Thread(tc);
            clientes.add(tc);
            threads.add(th);
            th.start();
            //server.tratarConexao(socket);
            //System.out.println("Cliente finalizado!");
        }

    }

    public static void main(String[] args) {

        try {

            Server server = new Server();
            server.criaServerSocket(5555);
            server.connectionLoop();

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
