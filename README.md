# ♾️ Loop - Chat P2P

**Loop** é um aplicativo de chat Peer-to-Peer (P2P) desenvolvido em Java. Ele foi construído como parte da disciplina de **Sistemas Distribuídos** do curso de Sistemas de Informação (IFSULDEMINAS - Campus Machado).

O projeto consome uma API de comunicação em Sockets e apresenta uma interface gráfica moderna construída com **JavaFX**, totalmente estilizada com a paleta de cores do **Dracula Theme**.

---

## ✨ Funcionalidades

* **Comunicação Direta (P2P):** Conecta dois computadores diretamente sem a necessidade de um servidor central.
* **Múltiplos Protocolos:** Suporte nativo para escolha entre os protocolos **TCP** (Orientado à conexão) e **UDP** (Não orientado à conexão).
* **Interface Moderna (Dracula Theme):** Design agradável aos olhos com cores escuras e contrastes em roxo (`#bd93f9`) e verde (`#50fa7b`).
* **Clean UI:** O painel de configurações de rede desaparece após a conexão ser estabelecida, proporcionando foco total nas mensagens.
* **Personalização:** Ícone de aplicativo e logotipo na interface customizados.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** Java 21
* **Interface Gráfica:** JavaFX (SDK 21)
* **Rede:** Java Sockets (`java.net.Socket`, `java.net.ServerSocket`, `java.net.DatagramSocket`)
* **IDE Recomendada:** IntelliJ IDEA ou Eclipse

---

## 🚀 Como Configurar e Rodar o Projeto

Como o JavaFX não vem mais embutido no JDK a partir do Java 11, é necessário configurá-lo manualmente.

### 1. Pré-requisitos
* Baixe e instale o [Java JDK 21](https://www.oracle.com/java/technologies/downloads/).
* Baixe e extraia o [JavaFX SDK 21](https://gluonhq.com/products/javafx/).

### 2. Configurando no IntelliJ IDEA (Recomendado)
1. Abra o projeto.
2. Vá em **File > Project Structure > Libraries**.
3. Clique em **+**, escolha **Java** e selecione a pasta `lib` de onde você extraiu o JavaFX SDK. Clique em OK.
4. Clique na setinha de **Run Configuration** (ao lado do botão de Play) e vá em **Edit Configurations**.
5. No campo **VM options** (se não estiver visível, clique em "Modify options" e marque "Add VM options"), adicione o seguinte comando alterando para o seu caminho exato do JavaFX:
   ```text
   --module-path "C:\caminho\para\seu\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml
