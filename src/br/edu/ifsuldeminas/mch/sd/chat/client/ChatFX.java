package br.edu.ifsuldeminas.mch.sd.chat.client;

import br.edu.ifsuldeminas.mch.sd.chat.ChatException;
import br.edu.ifsuldeminas.mch.sd.chat.ChatFactory;
import br.edu.ifsuldeminas.mch.sd.chat.MessageContainer;
import br.edu.ifsuldeminas.mch.sd.chat.Sender;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ChatFX extends Application implements MessageContainer {

    private TextField txtPortaLocal, txtIpRemoto, txtPortaRemota, txtMensagem;
    private RadioButton rbTCP, rbUDP;
    private VBox painelMensagens; // Substitui o antigo TextArea
    private ScrollPane scrollPane;
    private Button btnIniciar, btnEnviar;
    private GridPane painelConfig;
    
    private Sender sender;

    // Cores base para a interface
    private final String COR_ROXA = "#7C3AED";
    private final String COR_CINZA = "#F3F4F6";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Loop");

        // --- CABEÇALHO (HEADER) ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(15));
        // Fundo roxo aplicado ao painel superior
        header.setStyle("-fx-background-color: " + COR_ROXA + ";");
        
        Label lblTitulo = new Label("Loop");
        lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));
        // Texto na cor branca
        lblTitulo.setStyle("-fx-text-fill: white;");
        header.getChildren().add(lblTitulo);

        // --- PAINEL DE CONFIGURAÇÕES ---
        painelConfig = new GridPane();
        painelConfig.setPadding(new Insets(15));
        painelConfig.setHgap(10);
        painelConfig.setVgap(10);
        painelConfig.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        painelConfig.add(new Label("Sua Porta:"), 0, 0);
        txtPortaLocal = new TextField("8080");
        painelConfig.add(txtPortaLocal, 1, 0);

        painelConfig.add(new Label("IP Amigo:"), 0, 1);
        txtIpRemoto = new TextField("127.0.0.1");
        painelConfig.add(txtIpRemoto, 1, 1);

        painelConfig.add(new Label("Porta Amigo:"), 0, 2);
        txtPortaRemota = new TextField("8081");
        painelConfig.add(txtPortaRemota, 1, 2);

        rbTCP = new RadioButton("TCP");
        rbTCP.setSelected(true);
        rbUDP = new RadioButton("UDP");
        ToggleGroup tgProtocolo = new ToggleGroup();
        rbTCP.setToggleGroup(tgProtocolo);
        rbUDP.setToggleGroup(tgProtocolo);

        btnIniciar = new Button("Conectar");
        btnIniciar.setStyle("-fx-background-color: " + COR_ROXA + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

        HBox boxProtocolo = new HBox(10, rbTCP, rbUDP, btnIniciar);
        boxProtocolo.setAlignment(Pos.CENTER_LEFT);
        painelConfig.add(new Label("Rede:"), 0, 3);
        painelConfig.add(boxProtocolo, 1, 3);

        VBox topContainer = new VBox(header, painelConfig);

        // --- ÁREA DE MENSAGENS (ESTILO DM) ---
        painelMensagens = new VBox(10);
        painelMensagens.setPadding(new Insets(15));
        painelMensagens.setStyle("-fx-background-color: white;");

        scrollPane = new ScrollPane(painelMensagens);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: white;");
        
        // Rolar automaticamente para a última mensagem
        painelMensagens.heightProperty().addListener((obs, oldVal, newVal) -> scrollPane.setVvalue(1.0));

        // --- PAINEL INFERIOR: ENVIO DE MENSAGENS ---
        HBox painelEnvio = new HBox(10);
        painelEnvio.setAlignment(Pos.CENTER);
        painelEnvio.setPadding(new Insets(10, 15, 10, 15));
        painelEnvio.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1 0 0 0;");
        
        txtMensagem = new TextField();
        txtMensagem.setPromptText("Mensagem...");
        txtMensagem.setDisable(true);
        txtMensagem.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #E5E7EB; -fx-background-color: " + COR_CINZA + "; -fx-padding: 8 15 8 15;");
        HBox.setHgrow(txtMensagem, Priority.ALWAYS);
        
        btnEnviar = new Button("Enviar");
        btnEnviar.setDisable(true);
        btnEnviar.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COR_ROXA + "; -fx-font-weight: bold;");

        painelEnvio.getChildren().addAll(txtMensagem, btnEnviar);

        // Estrutura Principal
        BorderPane root = new BorderPane();
        root.setTop(topContainer);
        root.setCenter(scrollPane);
        root.setBottom(painelEnvio);

        configurarEventos();

        Scene scene = new Scene(root, 400, 600); // Proporção mais parecida com celular
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void configurarEventos() {
        btnIniciar.setOnAction(e -> {
            int portaLocal;
            int portaRemota;
            String ipRemoto = txtIpRemoto.getText();
            boolean isTCP = rbTCP.isSelected();

            try {
                portaLocal = Integer.parseInt(txtPortaLocal.getText());
                portaRemota = Integer.parseInt(txtPortaRemota.getText());
            } catch (NumberFormatException ex) {
                mostrarAlerta("Erro de Entrada", "Verifique se as portas contêm apenas números válidos.");
                return;
            }

            btnIniciar.setDisable(true);
            adicionarMensagem("Aguardando a conexão da outra máquina...", "Sistema");

            new Thread(() -> {
                try {
                    Sender novoSender = ChatFactory.build(isTCP, ipRemoto, portaRemota, portaLocal, this);
                    
                    Platform.runLater(() -> {
                        sender = novoSender;
                        txtMensagem.setDisable(false);
                        btnEnviar.setDisable(false);
                        txtMensagem.requestFocus();
                        
                        // Esconde as configurações ao conectar para ficar com visual limpo
                        painelConfig.setVisible(false);
                        painelConfig.setManaged(false);
                        
                        adicionarMensagem("Chat conectado!", "Sistema");
                    });
                    
                } catch (ChatException ex) {
                    Platform.runLater(() -> {
                        mostrarAlerta("Erro de Conexão", "Erro ao iniciar chat: " + ex.getMessage());
                        btnIniciar.setDisable(false);
                        adicionarMensagem("Falha ao conectar.", "Sistema");
                    });
                }
            }).start();
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
                adicionarMensagem(msg, "Você"); // Adiciona seu balão roxo
                txtMensagem.clear();
            } catch (ChatException ex) {
                mostrarAlerta("Erro de Envio", ex.getMessage());
            }
        }
    }

    @Override
    public void newMessage(String message) {
        Platform.runLater(() -> {
            adicionarMensagem(message, "Amigo"); // Adiciona o balão cinza do amigo
        });
    }

    // --- LÓGICA DE CRIAÇÃO DOS BALÕES DE MENSAGEM ---
    private void adicionarMensagem(String texto, String remetente) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.setMaxWidth(250); // Limita a largura do balão
        label.setPadding(new Insets(10, 14, 10, 14));

        HBox linha = new HBox();

        if (remetente.equals("Você")) {
            // Estilo DM sua: Roxa, texto branco, alinhada à direita
            label.setStyle("-fx-background-color: " + COR_ROXA + "; -fx-text-fill: white; -fx-background-radius: 18 18 4 18;");
            linha.setAlignment(Pos.CENTER_RIGHT);
        } else if (remetente.equals("Amigo")) {
            // Estilo DM amigo: Cinza, texto preto, alinhada à esquerda
            label.setStyle("-fx-background-color: " + COR_CINZA + "; -fx-text-fill: black; -fx-background-radius: 18 18 18 4;");
            linha.setAlignment(Pos.CENTER_LEFT);
        } else {
            // Mensagens do Sistema
            label.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
            label.setPadding(new Insets(5, 0, 5, 0));
            linha.setAlignment(Pos.CENTER);
        }

        linha.getChildren().add(label);
        painelMensagens.getChildren().add(linha);
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}