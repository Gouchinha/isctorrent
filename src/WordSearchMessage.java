import java.io.Serializable;

public class WordSearchMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String searchWord;

    public WordSearchMessage(String searchWord) {
        this.searchWord = searchWord;
    }

    public String getSearchWord() {
        return searchWord;
    }

    @Override
    public String toString() {
        return "Procura pela palavra: " + searchWord;
    }
}

