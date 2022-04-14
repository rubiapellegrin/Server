/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Estados;
import util.Mensagem;
import util.Status;

/**
 *
 * @author rubia
 */
public class TrataConexao implements Runnable {

    private Server server;
    private Socket socket;
    private int id;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Estados estado;
    int lance = 0;
    Double valorPago;

    public TrataConexao(Server server, Socket socket, int id) {
        this.server = server;
        this.socket = socket;
        this.id = id;
        this.estado = Estados.DESCONECTADO;
    }

    @Override
    public void run() {
        try {
            tratarConexao();
        } catch (IOException ex) {
            System.out.println("Erro no cliente " + id + " : " + ex.getMessage());
            Logger.getLogger(TrataConexao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TrataConexao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(TrataConexao.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void fechaSocket(Socket socket) throws IOException {
        socket.close();

    }

    private void tratarConexao() throws IOException, ClassNotFoundException, InterruptedException {
        Mensagem reply = null;
        int i = 0;
        String[] string = new String[9];
        String EstadoPubli = "", aux = "";
        Double valor;

        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            ArrayList<String> dados = new ArrayList<>();

            String operacao = null;
            System.out.println("Tratando..");
            estado = Estados.CONECTADO;

            while (!"SAIR".equals(operacao)) {

                Mensagem m = (Mensagem) input.readObject();
                System.out.println("Mensagem do cliente " + id + " :\n" + m);

                operacao = (String) m.getOperacao();
                reply = new Mensagem(operacao + "REPLY");

                switch (estado) {
                    case CONECTADO:
                        switch (operacao) {
                            case "LOGIN":

                                String user = (String) m.getParam("user");
                                String pass = (String) m.getParam("pass");

                                if (pass.equals("senha")) {
                                    reply.setStatus(Status.OK);
                                    estado = Estados.AUTENTICADO;
                                    reply.setParam("msg", " Autenticado  ");

                                } else {
                                    reply.setStatus(Status.ERROR);
                                    reply.setParam("msg", " Erro na senha!  ");
                                }
                                break;
                            case "BUSCA":
                                String titulo = (String) m.getParam("titulo");
                                reply.setStatus(Status.OK);

                                reply.setParam("msg", "\nTitulo encontrado:\n| Cliente | Titulo | "
                                        + "Descricao | Tamanho | Prioridade | Estado | Valor |\n" + server.Buscar(titulo) + "\n");
                                break;


                            case "SAIR":
                                reply.setStatus(Status.OK);
                                estado = Estados.SAIR;
                                break;
                            default:
                                reply.setStatus(Status.ERROR);
                                reply.setParam("msg", "Mensagem não autorizada ou invalida!");
                                break;
                        }
                        break;
                    case AUTENTICADO:
                        switch (operacao) {
                            case "ENVIAR":  
                                try {
                                String titulo = (String) m.getParam("titulo");
                                String descricao = (String) m.getParam("descricao");
                                String prioridade = (String) m.getParam("prioridade");
                                EstadoPubli = "aguardando confirmação";
                                i++;
                                int tamanho = titulo.length() + descricao.length();
                                if (prioridade.equals("alta") && tamanho >= 20) {
                                    valor = 100.2;
                                } else {
                                    valor = 52.0;
                                }
                                string[i] = "\n| " + id + " |" + titulo + " | " + descricao + " | " + tamanho + " | " + prioridade
                                        + " | " + EstadoPubli + " | " + valor + " ";
                                dados.add(string[i]);
                                server.Salvar(id, titulo, descricao, tamanho, prioridade, EstadoPubli, valor);

                                reply.setStatus(Status.OK);
                                reply.setParam("msg", " Titulo cadastrado:\n| Cliente | Titulo | "
                                        + "Descricao | Tamanho | Prioridade | Estado | Valor |\n" + string[i]);
                            } catch (Exception e) {
                                reply.setStatus(Status.PARAMERROR);
                                reply.setParam("msg", "Erro no parametro.");
                            }
                            break;
                            case "CONFIRMAR":

                                String titulo = (String) m.getParam("titulo");
                                for (int j = 0; j < dados.size(); j++) {
                                    if (dados.get(j).contains(titulo)) {
                                        aux = dados.get(j);
                                        aux = aux.replaceAll("aguardando confirmação", "PUBLICADO");
                                        dados.remove(j);
                                        dados.add(aux);
                                    }
                                }
                                reply.setStatus(Status.OK);
                                reply.setParam("msg", "\nTitulo encontrado:\n" + aux);

                                break;

                            case "EXCLUIR":

                                titulo = (String) m.getParam("titulo");
                                server.Excluir(titulo);
                                reply.setStatus(Status.OK);
                                reply.setParam("msg", " Titulo Removido!\n");

                                break;
                            case "CONFIRMLANCE":
                                titulo = (String) m.getParam("titulo");
                                valorPago = (Double) m.getParam("valorPago");

                                server.ConfirmarLance(titulo, valorPago);
                                reply.setStatus(Status.OK);
                                reply.setParam("msg", " Lance confirmado!\n");

                                break;
                            case "CANCELANCE":
                                titulo = (String) m.getParam("titulo");
                                valorPago = (Double) m.getParam("valorPago");

                                server.NegarLance(titulo, valorPago);
                                reply.setStatus(Status.OK);
                                reply.setParam("msg", " Lance cancelado!\n");

                                break;
                            case "BUSCA":

                                titulo = (String) m.getParam("titulo");
                                reply.setStatus(Status.OK);

                                reply.setParam("msg", "\nTitulo encontrado:\n| Cliente | Titulo | "
                                        + "Descricao | Tamanho | Prioridade | Estado | Valor |\n" + server.Buscar(titulo) + "\n");
                                break;
                            case "COMPRA":
                                titulo = (String) m.getParam("titulo");
                                valorPago = (Double) m.getParam("valorPago");
                                lance++;
                                reply.setParam("msg", "\nLance respondido:" + server.Compra(titulo, valorPago));

                                break;
                            case "LANCES":
                                reply.setStatus(Status.OK);
                                reply.setParam("msg", "\nLances:\n| Cliente | Titulo | "
                                        + "Descricao | Tamanho | Prioridade | Estado | Valor | Lance |\n" + server.ListarLances(id) + "\n");

                                break;
                            case "LISTAR":
                                reply.setStatus(Status.OK);
                                reply.setParam("msg", "\nTitulos:\n| Cliente | Titulo | "
                                        + "Descricao | Tamanho | Prioridade | Estado | Valor |\n" + server.listar() + "\n");

                                break;
                            case "LOGOUT":
                                reply.setStatus(Status.OK);
                                estado = Estados.CONECTADO;
                                break;
                            case "SAIR":
                                reply.setStatus(Status.OK);
                                estado = Estados.SAIR;
                                break;
                            default:
                                reply.setStatus(Status.ERROR);
                                reply.setParam("msg", "Mensagem não autorizada ou invalida!");
                                break;
                        }
                        break;

                }

                output.writeObject(reply);

            }

            output.flush();
            input.close();
            output.close();
        } catch (IOException ex) {
            System.out.println("Problema no tratamento da conexao com o cliente.. " + socket.getInetAddress());
            System.out.println("Erro: " + ex.getMessage());
        } finally {
            fechaSocket(socket);
        }
    }

}
