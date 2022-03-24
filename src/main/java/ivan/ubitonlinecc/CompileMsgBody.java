package ivan.ubitonlinecc;

import java.util.HashMap;
import java.util.Map;

public class CompileMsgBody {
    private String filePath;
    private final Map<Integer, String> errorLines = new HashMap<>();

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Map<Integer, String> getErrorLines() {
        return errorLines;
    }
}
