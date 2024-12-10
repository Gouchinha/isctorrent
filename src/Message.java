import java.io.Serializable;

public abstract class Message implements Serializable {
    private static final long serialVersionUID = 1L;
}

class NewConnectionRequest extends Message {
    @Override
    public String toString() {
        return "Pedido de Nova Conexão";
    }
}

class TextMessage extends Message {
    private String content;

    public TextMessage(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Mensagem: " + content;
    }
}

