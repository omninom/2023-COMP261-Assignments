/**
 * Interface for all nodes that can be executed,
 * including the top level program node
 */

interface ProgramNode {
    public void execute(Robot robot);
}

interface BooleanNode {
    public boolean evaluate(Robot robot);
}

interface IntNode {
    public int evaluate(Robot robot);
}
