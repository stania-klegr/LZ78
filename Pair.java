//stania klegr 1339709
//JasonTollison 1319030
//generic pair for the phraseNum and byte
public class Pair<First, Second> {

    private First first;
    private Second second;

    Pair(First first, Second second) {
        this.first = first;
        this.second = second;
    }

    public First getPhraseNum() {
        return first;
    }

    public void setPhraseNum(First first) {
        this.first = first;
    }

    public Second getMissmatch() {
        return second;
    }

    public void setMissmatch(Second second) {
        this.second = second;
    }
}
