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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class ChatFX extends Application implements MessageContainer {

    private TextField txtPortaLocal, txtIpRemoto, txtPortaRemota, txtMensagem;
    private RadioButton rbTCP, rbUDP;
    private VBox painelMensagens;
    private ScrollPane scrollPane;
    private Button btnIniciar, btnEnviar;
    private GridPane painelConfig;

    private Sender sender;

    // Paleta de Cores Oficial - Dracula Theme
    private final String DRACULA_BG = "#282a36";
    private final String DRACULA_CURRENT_LINE = "#44475a";
    private final String DRACULA_FG = "#f8f8f2";
    private final String DRACULA_COMMENT = "#6272a4";
    private final String DRACULA_PURPLE = "#bd93f9";
    private final String DRACULA_GREEN = "#50fa7b";
    private final String DRACULA_RED = "#ff5555";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Loop - Chat P2P");

        // --- CARREGAMENTO DAS IMAGENS ---
        Image iconeApp = null;
        Image logoApp = null;
        try {
            // Carrega o ícone (FAVICON_LOOP.png) e a Logo (Logo_Loop.png)
            iconeApp = new Image(getClass().getResourceAsStream("FAVICON_LOOP.png"));
            logoApp = new Image(getClass().getResourceAsStream("Logo_Loop.png"));
        } catch (Exception e) {
            System.err.println("Aviso: Imagens FAVICON_LOOP.png ou Logo_Loop.png não encontradas.");
        }

        // --- 1. DEFINIÇÃO DO ÍCONE DA JANELA ---
        if (iconeApp != null) {
            primaryStage.getIcons().add(iconeApp);
        }

        // --- CABEÇALHO ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setStyle("-fx-background-color: " + DRACULA_BG + "; -fx-border-color: " + DRACULA_CURRENT_LINE + "; -fx-border-width: 0 0 1 0;");

        // --- 2. DEFINIÇÃO DA LOGO NO APLICATIVO ---
        if (logoApp != null) {
            ImageView logoView = new ImageView(logoApp);
            // Ajusta a altura da logo horizontal para caber bem no cabeçalho
            logoView.setFitHeight(40);
            logoView.setPreserveRatio(true);
            header.getChildren().add(logoView);
        } else {
            Label lblTitulo = new Label("Loop");
            lblTitulo.setFont(Font.font("System", FontWeight.BOLD, 18));
            lblTitulo.setStyle("-fx-text-fill: " + DRACULA_PURPLE + ";");
            header.getChildren().add(lblTitulo);
        }

        // --- PAINEL DE CONFIGURAÇÕES ---
        painelConfig = new GridPane();
        painelConfig.setPadding(new Insets(15));
        painelConfig.setHgap(10);
        painelConfig.setVgap(10);
        painelConfig.setStyle("-fx-background-color: " + DRACULA_BG + "; -fx-border-color: " + DRACULA_CURRENT_LINE + "; -fx-border-width: 0 0 2 0;");

        painelConfig.add(criarLabelDracula("Sua Porta:"), 0, 0);
        txtPortaLocal = criarInputDracula("8080");
        painelConfig.add(txtPortaLocal, 1, 0);

        painelConfig.add(criarLabelDracula("IP Amigo:"), 0, 1);
        txtIpRemoto = criarInputDracula("127.0.0.1");
        painelConfig.add(txtIpRemoto, 1, 1);

        painelConfig.add(criarLabelDracula("Porta Amigo:"), 0, 2);
        txtPortaRemota = criarInputDracula("8081");
        painelConfig.add(txtPortaRemota, 1, 2);

        rbTCP = new RadioButton("TCP"); rbTCP.setStyle("-fx-text-fill: " + DRACULA_FG + ";"); rbTCP.setSelected(true);
        rbUDP = new RadioButton("UDP"); rbUDP.setStyle("-fx-text-fill: " + DRACULA_FG + ";");
        ToggleGroup tgProtocolo = new ToggleGroup();
        rbTCP.setToggleGroup(tgProtocolo);
        rbUDP.setToggleGroup(tgProtocolo);

        btnIniciar = new Button("Conectar");
        btnIniciar.setStyle("-fx-background-color: " + DRACULA_GREEN + "; -fx-text-fill: " + DRACULA_BG + "; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");

        HBox boxProtocolo = new HBox(10, rbTCP, rbUDP, btnIniciar);
        boxProtocolo.setAlignment(Pos.CENTER_LEFT);
        painelConfig.add(criarLabelDracula("Rede:"), 0, 3);
        painelConfig.add(boxProtocolo, 1, 3);

        VBox topContainer = new VBox(header, painelConfig);

        // --- ÁREA DE MENSAGENS ---
        painelMensagens = new VBox(10);
        painelMensagens.setPadding(new Insets(15));
        painelMensagens.setStyle("-fx-background-color: " + DRACULA_BG + ";");

        scrollPane = new ScrollPane(painelMensagens);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + DRACULA_BG + "; -fx-background-color: " + DRACULA_BG + "; -fx-border-color: transparent;");

        painelMensagens.heightProperty().addListener((obs, oldVal, newVal) -> scrollPane.setVvalue(1.0));

        // --- PAINEL INFERIOR: ENVIO DE MENSAGENS ---
        HBox painelEnvio = new HBox(10);
        painelEnvio.setAlignment(Pos.CENTER);
        painelEnvio.setPadding(new Insets(10, 15, 10, 15));
        painelEnvio.setStyle("-fx-background-color: " + DRACULA_BG + "; -fx-border-color: " + DRACULA_CURRENT_LINE + "; -fx-border-width: 1 0 0 0;");

        txtMensagem = new TextField();
        txtMensagem.setPromptText("Mensagem...");
        txtMensagem.setDisable(true);
        txtMensagem.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: " + DRACULA_CURRENT_LINE + "; -fx-background-color: " + DRACULA_CURRENT_LINE + "; -fx-text-fill: " + DRACULA_FG + "; -fx-padding: 8 15 8 15; -fx-prompt-text-fill: " + DRACULA_COMMENT + ";");
        HBox.setHgrow(txtMensagem, Priority.ALWAYS);

        btnEnviar = new Button("Enviar");
        btnEnviar.setDisable(true);
        btnEnviar.setStyle("-fx-background-color: transparent; -fx-text-fill: " + DRACULA_PURPLE + "; -fx-font-weight: bold; -fx-cursor: hand;");

        painelEnvio.getChildren().addAll(txtMensagem, btnEnviar);

        // Estrutura Principal
        BorderPane root = new BorderPane();
        root.setTop(topContainer);
        root.setCenter(scrollPane);
        root.setBottom(painelEnvio);

        configurarEventos();

        Scene scene = new Scene(root, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Label criarLabelDracula(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: " + DRACULA_FG + ";");
        return lbl;
    }

    private TextField criarInputDracula(String valor) {
        TextField tf = new TextField(valor);
        tf.setStyle("-fx-background-color: " + DRACULA_CURRENT_LINE + "; -fx-text-fill: " + DRACULA_FG + "; -fx-border-radius: 5;");
        return tf;
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

                        painelConfig.setVisible(false);
                        painelConfig.setManaged(false);

                        adicionarMensagem("Chat conectado com sucesso!", "Sistema");
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
                adicionarMensagem(msg, "Você");
                txtMensagem.clear();
            } catch (ChatException ex) {
                mostrarAlerta("Erro de Envio", ex.getMessage());
            }
        }
    }

    @Override
    public void newMessage(String message) {
        Platform.runLater(() -> {
            adicionarMensagem(message, "Amigo");
        });
    }

    private void adicionarMensagem(String texto, String remetente) {
        Label label = new Label(texto);
        label.setWrapText(true);
        label.setMaxWidth(250);
        label.setPadding(new Insets(10, 14, 10, 14));

        HBox linha = new HBox();

        if (remetente.equals("Você")) {
            label.setStyle("-fx-background-color: " + DRACULA_PURPLE + "; -fx-text-fill: " + DRACULA_BG + "; -fx-background-radius: 18 18 4 18;");
            linha.setAlignment(Pos.CENTER_RIGHT);
        } else if (remetente.equals("Amigo")) {
            label.setStyle("-fx-background-color: " + DRACULA_CURRENT_LINE + "; -fx-text-fill: " + DRACULA_FG + "; -fx-background-radius: 18 18 18 4;");
            linha.setAlignment(Pos.CENTER_LEFT);
        } else {
            label.setStyle("-fx-text-fill: " + DRACULA_COMMENT + "; -fx-font-size: 11px;");
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
        alert.getDialogPane().setStyle("-fx-background-color: " + DRACULA_BG + ";");
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: " + DRACULA_RED + ";");
        alert.showAndWait();
    }
}