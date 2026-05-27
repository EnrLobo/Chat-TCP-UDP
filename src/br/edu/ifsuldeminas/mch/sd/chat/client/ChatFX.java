package br.edu.ifsuldeminas.mch.sd.chat.client;

import br.edu.ifsuldeminas.mch.sd.chat.ChatException;
import br.edu.ifsuldeminas.mch.sd.chat.ChatFactory;
import br.edu.ifsuldeminas.mch.sd.chat.MessageContainer;
import br.edu.ifsuldeminas.mch.sd.chat.Sender;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ChatFX extends Application implements MessageContainer {

    private TextField txtPortaLocal, txtIpRemoto, txtPortaRemota, txtMensagem;
    private RadioButton rbTCP, rbUDP;
    private TextArea areaMensagens;
    private Button btnIniciar, btnEnviar;
    
    private Sender sender;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat P2P - Sistemas Distribuídos");

        // Painel Superior: Configurações de Conexão
        GridPane painelConfig = new GridPane();
        painelConfig.setPadding(new Insets(10));
        painelConfig.setHgap(10);
        painelConfig.setVgap(10);

        painelConfig.add(new Label("Porta Local:"), 0, 0);
        txtPortaLocal = new TextField("8080");
        painelConfig.add(txtPortaLocal, 1, 0);

        painelConfig.add(new Label("IP Remoto:"), 0, 1);
        txtIpRemoto = new TextField("127.0.0.1");
        painelConfig.add(txtIpRemoto, 1, 1);

        painelConfig.add(new Label("Porta Remota:"), 0, 2);
        txtPortaRemota = new TextField("8081");
        painelConfig.add(txtPortaRemota, 1, 2);

        // Flag TCP/UDP
        rbTCP = new RadioButton("TCP");
        rbTCP.setSelected(true);
        rbUDP = new RadioButton("UDP");
        ToggleGroup tgProtocolo = new ToggleGroup();
        rbTCP.setToggleGroup(tgProtocolo);
        rbUDP.setToggleGroup(tgProtocolo);

        btnIniciar = new Button("Iniciar Chat");

        HBox boxProtocolo = new HBox(10, rbTCP, rbUDP, btnIniciar);
        painelConfig.add(new Label("Protocolo:"), 0, 3);
        painelConfig.add(boxProtocolo, 1, 3);

        // Centro: Área de Mensagens
        areaMensagens = new TextArea();
        areaMensagens.setEditable(false);
        areaMensagens.setWrapText(true);

        // Painel Inferior: Envio de Mensagens
        HBox painelEnvio = new HBox(10);
        painelEnvio.setPadding(new Insets(10));
        txtMensagem = new TextField();
        txtMensagem.setDisable(true);
        HBox.setHgrow(txtMensagem, Priority.ALWAYS);
        
        btnEnviar = new Button("Enviar");
        btnEnviar.setDisable(true);
        painelEnvio.getChildren().addAll(txtMensagem, btnEnviar);

        // Estrutura Principal
        BorderPane root = new BorderPane();
        root.setTop(painelConfig);
        root.setCenter(areaMensagens);
        root.setBottom(painelEnvio);

        configurarEventos();

        Scene scene = new Scene(root, 500, 450);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void configurarEventos() {
        btnIniciar.setOnAction(e -> {
            try {
                int portaLocal = Integer.parseInt(txtPortaLocal.getText());
                int portaRemota = Integer.parseInt(txtPortaRemota.getText());
                String ipRemoto = txtIpRemoto.getText();
                boolean isTCP = rbTCP.isSelected();

                sender = ChatFactory.build(isTCP, ipRemoto, portaRemota, portaLocal, this);
                
                btnIniciar.setDisable(true);
                txtMensagem.setDisable(false);
                btnEnviar.setDisable(false);
                txtMensagem.requestFocus();
                
                areaMensagens.appendText("Sistema: Chat iniciado (" + (isTCP ? "TCP" : "UDP") + ").\n");
                
            } catch (NumberFormatException ex) {
                mostrarAlerta("Erro de Entrada", "Verifique se as portas contêm apenas números válidos.");
            } catch (ChatException ex) {
                mostrarAlerta("Erro de Conexão", "Erro ao iniciar chat: " + ex.getMessage());
            }
        });

        btnEnviar.setOnAction(e -> enviarMensagem());

        txtMensagem.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                enviarMensagem();
            }
        });
    }

    private void enviarMensagem() {
        String msg = txtMensagem.getText().trim();
        if (!msg.isEmpty() && sender != null) {
            try {
                sender.send(msg);
                areaMensagens.appendText("Você: " + msg + "\n");
                txtMensagem.clear();
            } catch (ChatException ex) {
                mostrarAlerta("Erro de Envio", ex.getMessage());
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @Override
    public void newMessage(String message) {
        Platform.runLater(() -> {
            areaMensagens.appendText("Amigo: " + message + "\n");
        });
    }
}