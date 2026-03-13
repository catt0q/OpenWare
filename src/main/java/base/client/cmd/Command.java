package base.client.cmd;

@FunctionalInterface
public interface Command {
    void execute(String... strings);
}
