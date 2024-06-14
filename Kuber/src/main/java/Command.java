import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Command {
    CREATE_TASK("create task --name=(?<taskName>\\S+)( --node=(?<workerName>\\S+))?"),
    WORKERS("get nodes"),
    TASKS("get tasks"),
    DELETE_TASK("create delete task --name=(?<taskName>\\S+)"),
    CORDON_NODE("cordon node (?<workerName>\\S+)"),
    UN_CORDON_NODE("uncordon node (?<workerName>\\S+)");
    private final String regex;

    Command(String regex) {
        this.regex = regex;
    }

    public static Matcher getMatcher(String input, Command mainRegex) {
        Matcher matcher = Pattern.compile(mainRegex.regex).matcher(input);
        if (matcher.matches())
            return matcher;
        return null;
    }
}
