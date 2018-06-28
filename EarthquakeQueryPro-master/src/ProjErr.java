
public class ProjErr extends Exception {
    public static final String updateSUCCESS = "success";
    public static final String updateFAIL = "fail";
    public ProjErr() {
        super("An exception!");
    }

    public ProjErr(String msg) {
        super(msg);
    }

    public String toString() {
        return super.toString();
    }
}
