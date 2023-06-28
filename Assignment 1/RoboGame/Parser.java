import java.util.*;
import java.util.function.BiPredicate;
import java.util.regex.*;

/**
 * See assignment handout for the grammar.
 * You need to implement the parse(..) method and all the rest of the parser.
 * There are several methods provided for you:
 * - several utility methods to help with the parsing
 * See also the TestParser class for testing your code.
 *
 *
 * @author xueleor 300607821
 *
 */
public class Parser {


    // Useful Patterns

    static final Pattern NUMPAT = Pattern.compile("-?[1-9][0-9]*|0");
    static final Pattern OPENPAREN = Pattern.compile("\\(");
    static final Pattern CLOSEPAREN = Pattern.compile("\\)");
    static final Pattern OPENBRACE = Pattern.compile("\\{");
    static final Pattern CLOSEBRACE = Pattern.compile("\\}");

    static final Pattern ACTPAT = Pattern.compile("move|turnL|turnR|takeFuel|wait|shieldOn|shieldOff|turnAround");
    static final Pattern LOOPPAT = Pattern.compile("loop");
    static final Pattern IFPAT = Pattern.compile("if");
    static final Pattern WHILEPAT = Pattern.compile("while");
    static final Pattern RELOPPAT = Pattern.compile("lt|gt|eq");
    static final Pattern SENSORPAT = Pattern.compile("fuelLeft|oppLR|oppFB|numBarrels|barrelLR|barrelFB|wallDist");
    static final Pattern OPPAT = Pattern.compile("add|sub|mul|div");
    static final Pattern VARPAT = Pattern.compile("\\$[A-Za-z][A-Za-z0-9]*");

    //Map for variables
    static Map<String, Integer> varMap = new HashMap<>();

    //----------------------------------------------------------------
    /**
     * The top of the parser, which is handed a scanner containing
     * the text of the program to parse.
     * Returns the parse tree.
     */
    ProgramNode parse(Scanner s) {
        // Set the delimiter for the scanner.
        s.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");
        // THE PARSER GOES HERE
        // Call the parseProg method for the first grammar rule (PROG) and return the node
        varMap.clear();
        ProgramNode tree = parseProg(s);
        return tree;
    }

    /**
     * PROG ::= [STMT]*
     */
    public ProgramNode parseProg(Scanner s){
        List<ProgramNode> statements = new ArrayList<>();
        while (s.hasNext()){                                    //while there are still tokens to parse
            ProgramNode stmt = parseStmt(s);
            statements.add(stmt);
        }
        return new StmtNode(statements);
    }

    /**
     * STMT ::= ACT ";" | LOOP | IF | WHILE | ASSIGN ";"
     */
    public ProgramNode parseStmt(Scanner s){
        if (!s.hasNext()) {fail("Expected a statement", s);}   //if there are no more tokens to parse, fail
        if (s.hasNext(ACTPAT)){
            ProgramNode action = parseAct(s);
            require(";", "Expected ';'", s);
            return action;
        }
        if (s.hasNext(LOOPPAT)) {return parseLoop(s);}
        if (s.hasNext(IFPAT)) {return parseIfStmt(s);}
        if (s.hasNext(WHILEPAT)) {return parseWhileStmt(s);}
        if (s.hasNext(VARPAT)) {return parseAssign(s);}
        fail("Expected a statement", s);
        return null;
    }

    /**
     * LOOP ::= "loop" BLOCK
     */
    public ProgramNode parseLoop(Scanner s){
        require(LOOPPAT, "Expected loop", s);
        return new LoopNode(parseBlock(s));
    }

    /**
     * IF ::= "if" "(" COND ")" BLOCK ["elif" "(" COND ")" BLOCK]* ["else" BLOCK]
     */
    public ProgramNode parseIfStmt(Scanner s){
        require(IFPAT, "Expected 'if'", s);
        require(OPENPAREN, "Expected '('", s);
        BooleanNode condition = parseCondition(s);
        require(CLOSEPAREN, "Expected ')'", s);
        ProgramNode block = parseBlock(s);
        if (s.hasNext("elif")){
            require("elif", "Expected 'elif'", s);
            ProgramNode elseIfBlock = parseElifStmt(s);
            return new IfElseNode(condition, block, elseIfBlock);
        }
        if (s.hasNext("else")){
            require("else", "Expected 'else'", s);
            ProgramNode elseBlock = parseBlock(s);
            return new IfElseNode(condition, block, elseBlock);
        }
        return new IfNode(condition, block);        //if the condition is true, execute the block
    }


    public ProgramNode parseElifStmt(Scanner s){
        require(OPENPAREN, "Expected '('", s);
        BooleanNode condition = parseCondition(s);
        require(CLOSEPAREN, "Expected ')'", s);
        ProgramNode block = parseBlock(s);
        if (s.hasNext("elif")){
            require("elif", "Expected 'elif'", s);
            ProgramNode elseIfBlock = parseElifStmt(s);
            return new IfElseNode(condition, block, elseIfBlock);
        }
        if (s.hasNext("else")){
            require("else", "Expected 'else'", s);
            ProgramNode elseBlock = parseBlock(s);
            return new IfElseNode(condition, block, elseBlock);
        }
        return new IfNode(condition, block);
    }

    /**
     * WHILE ::= "while" "(" COND ")" BLOCK
     */
    public ProgramNode parseWhileStmt(Scanner s){
        require(WHILEPAT, "Expected 'while'", s);
        require(OPENPAREN, "Expected '('", s);
        BooleanNode condition = parseCondition(s);
        require(CLOSEPAREN, "Expected ')'", s);
        ProgramNode block = parseBlock(s);
        return new WhileNode(condition, block);     //while the condition is true, execute the block
    }

    /**
     * ASSIGN ::= VAR "=" EXPR ";"
     */
    public ProgramNode parseAssign(Scanner s){
        VarNode var = parseVar(s);
        require("=", "Expected '='", s);
        IntNode expr = parseExpr(s);
        require(";", "Expected ';'", s);
        return new AssignNode(var, expr);       //assigns the value of the expression to the variable
    }

    /**
     * BLOCK ::= "{" STMT+ "}"
     */
    public BlockNode parseBlock(Scanner s){
        List<ProgramNode> stmts = new ArrayList<>();
        require(OPENBRACE, "expecting '{'", s);

        while (!s.hasNext(CLOSEBRACE)){
            stmts.add(parseStmt(s));
        }
        require(CLOSEBRACE, "expecting '}'", s);
        if (!stmts.isEmpty()) {return new BlockNode(stmts);}
        fail("Needs actions", s);
        return null;
    }

    /**
     * ACT ::= "move" "(" EXPR ")" | "turnL" | "turnR" | "takeFuel" | "wait" "(" EXPR ")" | "turnAround" | "shieldOn" | "shieldOff"
     */
    public ProgramNode parseAct(Scanner s){             //can just call method if you don't know what token is, due to return null
        if (s.hasNext("move")){return parseMove(s);}
        if (s.hasNext("turnL")){return parseTurnL(s);}
        if (s.hasNext("turnR")){return parseTurnR(s);}
        if (s.hasNext("takeFuel")){return parseTakeFuel(s);}
        if (s.hasNext("wait")){return parseWait(s);}
        if (s.hasNext("turnAround")){return parseTurnAround(s);}
        if (s.hasNext("shieldOn")){return parseShieldOn(s);}
        if (s.hasNext("shieldOff")){return parseShieldOff(s);}
        return null;
    }

    /**
     * EXPR ::= NUM | SENSOR | OP "(" EXPR "," EXPR ")" | VAR
     */
    public IntNode parseExpr(Scanner s){
        if (s.hasNext(NUMPAT)) {return parseNum(s);}
        if (s.hasNext(SENSORPAT)) {return parseSensor(s);}
        if (s.hasNext(OPPAT)) {return parseOp(s);}
        if (s.hasNext(VARPAT)) {return parseVar(s);}
        fail("Expected an expression", s);
        return null;
    }

    public ProgramNode parseMove(Scanner s){
        require("move", "Expected 'move'", s);
        if (s.hasNext(OPENPAREN)){                  //if there is an optional argument
            require(OPENPAREN, "Expected '('", s);
            IntNode expr = parseExpr(s);
            require(CLOSEPAREN, "Expected ')'", s);
            return new MoveNode(expr);
        }
        return new MoveNode(null);                  //if there is no optional argument
    }

    public ProgramNode parseTurnL(Scanner s){
        require("turnL", "Expected 'turnL'", s);
        return new TurnLNode();
    }

    public ProgramNode parseTurnR(Scanner s){
        require("turnR", "Expected 'turnR'", s);
        return new TurnRNode();
    }

    public ProgramNode parseTakeFuel(Scanner s){
        require("takeFuel", "Expected 'takeFuel'", s);
        return new TakeFuelNode();
    }

    public ProgramNode parseWait(Scanner s){
        require("wait", "Expected 'wait'", s);
        if (s.hasNext(OPENPAREN)){                  //if there is an optional argument
            require(OPENPAREN, "Expected '('", s);
            IntNode expr = parseExpr(s);
            require(CLOSEPAREN, "Expected ')'", s);
            return new WaitNode(expr);
        }
        return new WaitNode(null);                  //if there is no optional argument
    }

    public ProgramNode parseTurnAround(Scanner s){
        require("turnAround", "Expected 'turnAround'", s);
        return new TurnAroundNode();
    }

    public ProgramNode parseShieldOn(Scanner s){
        require("shieldOn", "Expected 'shieldOn'", s);
        return new ShieldOnNode();
    }

    public ProgramNode parseShieldOff(Scanner s){
        require("shieldOff", "Expected 'shieldOff'", s);
        return new ShieldOffNode();
    }

    /**
     * VAR   ::= "\\$[A-Za-z][A-Za-z0-9]*"
     */
    public VarNode parseVar(Scanner s){
        String var = require(VARPAT, "Expected a $variable", s);
        if (!varMap.containsKey(var)) {
            varMap.put(var, 0);
        }
        return new VarNode(var);
    }

    /**
     * OP   ::= "add" | "sub" | "mul" | "div"
     */
    public IntNode parseOp(Scanner s){
        if (s.hasNext("add")) {return parseAdd(s);}
        if (s.hasNext("sub")) {return parseSub(s);}
        if (s.hasNext("mul")) {return parseMul(s);}
        if (s.hasNext("div")) {return parseDiv(s);}
        fail("Expected an operator", s);
        return null;
    }

    public IntNode parseAdd(Scanner s){
        require("add", "Expected 'add'", s);
        require(OPENPAREN, "Expected '('", s);
        IntNode left = parseExpr(s);
        require(",", "Expected ','", s);
        IntNode right = parseExpr(s);
        require(CLOSEPAREN, "Expected ')'", s);
        return new AddNode(left, right);
    }

    public IntNode parseSub(Scanner s){
        require("sub", "Expected 'sub'", s);
        require(OPENPAREN, "Expected '('", s);
        IntNode left = parseExpr(s);
        require(",", "Expected ','", s);
        IntNode right = parseExpr(s);
        require(CLOSEPAREN, "Expected ')'", s);
        return new SubNode(left, right);
    }

    public IntNode parseMul(Scanner s){
        require("mul", "Expected 'mul'", s);
        require(OPENPAREN, "Expected '('", s);
        IntNode left = parseExpr(s);
        require(",", "Expected ','", s);
        IntNode right = parseExpr(s);
        require(CLOSEPAREN, "Expected ')'", s);
        return new MulNode(left, right);
    }

    public IntNode parseDiv(Scanner s){
        require("div", "Expected 'div'", s);
        require(OPENPAREN, "Expected '('", s);
        IntNode left = parseExpr(s);
        require(",", "Expected ','", s);
        IntNode right = parseExpr(s);
        require(CLOSEPAREN, "Expected ')'", s);
        return new DivNode(left, right);
    }


    public IntNode parseNum(Scanner s){
        return new NumNode(requireInt(NUMPAT, "Expected a number", s));
    }

    /**
     * COND  ::= RELOP "(" EXPR "," EXPR ")"  | and ( COND, COND ) | or ( COND, COND )  | not ( COND )
     */
    public BooleanNode parseCondition(Scanner s){
        if (s.hasNext("and")) {return parseAnd(s);}
        if (s.hasNext("or")) {return parseOr(s);}
        if (s.hasNext("not")) {return parseNot(s);}
        if (s.hasNext(RELOPPAT)) {return parseRelop(s);}
        fail("Expected a condition", s);
        return null;
    }

    public BooleanNode parseAnd(Scanner s){
        require("and", "Expected 'and'", s);
        require(OPENPAREN, "Expected '('", s);
        BooleanNode left = parseCondition(s);
        require(",", "Expected ','", s);
        BooleanNode right = parseCondition(s);
        require(CLOSEPAREN, "Expected ')'", s);
        return new AndNode(left, right);
    }

    public BooleanNode parseOr(Scanner s){
        require("or", "Expected 'or'", s);
        require(OPENPAREN, "Expected '('", s);
        BooleanNode left = parseCondition(s);
        require(",", "Expected ','", s);
        BooleanNode right = parseCondition(s);
        require(CLOSEPAREN, "Expected ')'", s);
        return new OrNode(left, right);
    }

    public BooleanNode parseNot(Scanner s){
        require("not", "Expected 'not'", s);
        require(OPENPAREN, "Expected '('", s);
        BooleanNode condition = parseCondition(s);
        require(CLOSEPAREN, "Expected ')'", s);
        return new NotNode(condition);
    }

    /**
     * RELOP ::= "lt" | "gt" | "eq"
     */
    public BooleanNode parseRelop(Scanner s){
        if (s.hasNext("lt"))    {return parseLt(s);}
        if (s.hasNext("gt"))    {return parseGt(s);}
        if (s.hasNext("eq"))    {return parseEq(s);}
        fail("Expected a relop", s);
        return null;
    }

    public BooleanNode parseLt(Scanner s){
        require("lt", "Expected 'lt'", s);
        require(OPENPAREN, "Expected '('", s);
        IntNode expr1 = parseExpr(s);
        require(",", "Expected ','", s);
        IntNode expr2 = parseExpr(s);
        require(CLOSEPAREN, "Expected ')'", s);
        return new LtNode(expr1, expr2);
    }

    public BooleanNode parseGt(Scanner s){
        require("gt", "Expected 'gt'", s);
        require(OPENPAREN, "Expected '('", s);
        IntNode expr1 = parseExpr(s);
        require(",", "Expected ','", s);
        IntNode expr2= parseExpr(s);
        require(CLOSEPAREN, "Expected ')'", s);
        return new GtNode(expr1, expr2);
    }

    public BooleanNode parseEq(Scanner s){
        require("eq", "Expected 'eq'", s);
        require(OPENPAREN, "Expected '('", s);
        IntNode expr1 = parseExpr(s);
        require(",", "Expected ','", s);
        IntNode expr2 = parseExpr(s);
        require(CLOSEPAREN, "Expected ')'", s);
        return new EqNode(expr1, expr2);
    }

    /**
     * SENSOR ::= "fuelLeft" | "oppLR" | "oppFB" | "numBarrels" |
     *           "barrelLR" [ "(" EXPR ")" ] | "barrelFB" [ "(" EXPR ")" ] | "wallDist"
     */
    public IntNode parseSensor(Scanner s){
        if (s.hasNext("fuelLeft"))  {return parseFuelLeft(s);}
        if (s.hasNext("oppLR"))     {return parseOppLR(s);}
        if (s.hasNext("oppFB"))     {return parseOppFB(s);}
        if (s.hasNext("numBarrels")){return parseNumBarrels(s);}
        if (s.hasNext("barrelLR"))  {return parseBarrelLR(s);}
        if (s.hasNext("barrelFB"))  {return parseBarrelFB(s);}
        if (s.hasNext("wallDist"))  {return parseWallDist(s);}
        fail("Expected a sensor", s);
        return null;
    }

    public IntNode parseFuelLeft(Scanner s){
        require("fuelLeft", "Expected 'fuelLeft'", s);
        return new FuelLeftNode();
    }

    public IntNode parseOppLR(Scanner s){
        require("oppLR", "Expected 'oppLR'", s);
        return new OppLRNode();
    }

    public IntNode parseOppFB(Scanner s){
        require("oppFB", "Expected 'oppFB'", s);
        return new OppFBNode();
    }

    public IntNode parseNumBarrels(Scanner s){
        require("numBarrels", "Expected 'numBarrels'", s);
        return new NumBarrelsNode();
    }

    public IntNode parseBarrelLR(Scanner s){
        require("barrelLR", "Expected 'barrelLR'", s);
        if (s.hasNext(OPENPAREN)){                      //if there is an optional argument
            require(OPENPAREN, "Expected '('", s);
            IntNode expr = parseExpr(s);
            require(CLOSEPAREN, "Expected ')'", s);
            return new BarrelLRNode(expr);
        }
        return new BarrelLRNode(null);              //if there is no optional argument
    }

    public IntNode parseBarrelFB(Scanner s){
        require("barrelFB", "Expected 'barrelFB'", s);
        if (s.hasNext(OPENPAREN)){                          //if there is an optional argument
            require(OPENPAREN, "Expected '('", s);
            IntNode expr = parseExpr(s);
            require(CLOSEPAREN, "Expected ')'", s);
            return new BarrelFBNode(expr);
        }
        return new BarrelFBNode(null);                  //if there is no optional argument
    }

    public IntNode parseWallDist(Scanner s){
        require("wallDist", "Expected 'wallDist'", s);
        return new WallDistNode();
    }

        //----------------------------------------------------------------
        // utility methods for the parser
        // - fail(..) reports a failure and throws exception
        // - require(..) consumes and returns the next token as long as it matches the pattern
        // - requireInt(..) consumes and returns the next token as an int as long as it matches the pattern
        // - checkFor(..) peeks at the next token and only consumes it if it matches the pattern

        /**
         * Report a failure in the parser.
         */
    static void fail(String message, Scanner s) {
        String msg = message + "\n   @ ...";
        for (int i = 0; i < 5 && s.hasNext(); i++) {
            msg += " " + s.next();
        }
        throw new ParserFailureException(msg + "...");
    }

    /**
     * Requires that the next token matches a pattern if it matches, it consumes
     * and returns the token, if not, it throws an exception with an error
     * message
     */
    static String require(String p, String message, Scanner s) {
        if (s.hasNext(p)) {return s.next();}
        fail(message, s);
        return null;
    }

    static String require(Pattern p, String message, Scanner s) {
        if (s.hasNext(p)) {return s.next();}
        fail(message, s);
        return null;
    }

    /**
     * Requires that the next token matches a pattern (which should only match a
     * number) if it matches, it consumes and returns the token as an integer
     * if not, it throws an exception with an error message
     */
    static int requireInt(String p, String message, Scanner s) {
        if (s.hasNext(p) && s.hasNextInt()) {return s.nextInt();}
        fail(message, s);
        return -1;
    }

    static int requireInt(Pattern p, String message, Scanner s) {
        if (s.hasNext(p) && s.hasNextInt()) {return s.nextInt();}
        fail(message, s);
        return -1;
    }

    /**
     * Checks whether the next token in the scanner matches the specified
     * pattern, if so, consumes the token and return true. Otherwise returns
     * false without consuming anything.
     */
    static boolean checkFor(String p, Scanner s) {
        if (s.hasNext(p)) {s.next(); return true;}
        return false;
    }

    static boolean checkFor(Pattern p, Scanner s) {
        if (s.hasNext(p)) {s.next(); return true;}
        return false;
    }

}

// You could add the node classes here or as separate java files.
// (if added here, they must not be declared public or private)
// For example:
//  class BlockNode implements ProgramNode {.....
//     with fields, a toString() method and an execute() method
//

/**
 * This class represents a program in the language. It is a list of statements.
 */
class StmtNode implements ProgramNode{
    List<ProgramNode> stmts;
    public StmtNode(List<ProgramNode> stmts){
        this.stmts = stmts;
    }
    public void execute (Robot robot){
        for (ProgramNode stmt : stmts){
            stmt.execute(robot);
        }
    }
    public String toString(){
        StringBuilder result = new StringBuilder();
        for (ProgramNode stmt : stmts){
            result.append(stmt.toString()).append(" ");
        }
        return result.toString();
    }
}

/**
 * This class represents a loop statement in the language. It is a list of statements that is executed repeatedly.
 */
class LoopNode implements ProgramNode{
    BlockNode block;
    LoopNode(BlockNode block){
        this.block = block;
    }
    public void execute(Robot robot) {
        while (true){
            block.execute(robot);
        }
    }
    public String toString() {
        return "loop";
    }
}

/**
 * This class represents a block statement in the language. It is a list of statements (essentially a block of code).
 */
class BlockNode implements ProgramNode{
    List<ProgramNode> stmts;
    public BlockNode(List<ProgramNode> stmts){
        this.stmts = stmts;
    }
    public void execute (Robot robot){
        for (ProgramNode stmt : stmts){
            stmt.execute(robot);
        }
    }
    public String toString(){
        StringBuilder result = new StringBuilder();
        for (ProgramNode stmt : stmts){
            result.append(stmt.toString()).append(" ");
        }
        return "{"+result+"}";
    }
}

/**
 * This class represents an assignment, where a variable is set to an integer.
 */
class AssignNode implements ProgramNode{
    VarNode variable;
    IntNode expr;
    public AssignNode(VarNode var, IntNode expr){
        this.variable = var;
        this.expr = expr;
    }
    public void execute (Robot robot){
        Parser.varMap.put(variable.var, expr.evaluate(robot));      //put the variable and its value into the map
    }
    public String toString(){
        return variable + " = " + expr.toString();
    }
}

class VarNode implements IntNode{
    String var;
    public VarNode(String var){
        this.var = var;
    }
    public int evaluate(Robot robot){
        if (Parser.varMap.containsKey(var)){            //if the variable is in the map, return its value
            return Parser.varMap.get(var);
        }
        else{
            return 0;
        }
    }
    public String toString(){
        return var;
    }
}

/**
 * This class represents a move statement in the language. It can either take an argument to specify steps, or it can be called without an argument.
 */
class MoveNode implements ProgramNode{
    IntNode expr;
    public MoveNode(IntNode expr){
        this.expr = expr;
    }
    public void execute (Robot robot){
        if (expr == null){
            robot.move();                   //if no argument is given, move 1 step
            return;
        }
        int n = expr.evaluate(robot);
        for (int i = 0; i < n; i++){        //if an argument is given, move n steps
            robot.move();
        }
    }
    public String toString() {return "move";}

}

/**
 * This class represents a turnL statement in the language.
 */
class TurnLNode implements ProgramNode{

    public TurnLNode(){}
    public void execute (Robot robot){
        robot.turnLeft();
    }
    public String toString(){
        return "turnL";
    }
}

/**
 * This class represents a turnR statement in the language.
 */
class TurnRNode implements ProgramNode{
    public TurnRNode(){}
    public void execute (Robot robot){
        robot.turnRight();
    }
    public String toString(){
        return "turnR";
    }
}

/**
 * This class represents a takeFuel statement in the language.
 */
class TakeFuelNode implements ProgramNode{
    public TakeFuelNode(){}
    public void execute (Robot robot){
        robot.takeFuel();
    }
    public String toString(){
        return "takeFuel";
    }
}

/**
 * This class represents a wait statement in the language. It can either take an argument to specify steps, or it can be called without an argument.
 */
class WaitNode implements ProgramNode{
    IntNode expr;
    public WaitNode(IntNode expr){
        this.expr = expr;
    }
    public void execute (Robot robot){
        if (expr == null){
            robot.idleWait();               //if no argument is given, wait 1 step
            return;
        }
        int n = expr.evaluate(robot);
        for (int i = 0; i < n; i++){        //if an argument is given, wait n steps
            robot.idleWait();
        }
    }
    public String toString(){
        return "wait";
    }
}

/**
 * This class represents a turnAround statement in the language.
 */
class TurnAroundNode implements ProgramNode{
    public TurnAroundNode(){}
    public void execute (Robot robot){
        robot.turnAround();
    }
    public String toString(){
        return "turnAround";
    }
}

/**
 * This class represents a shieldOn statement in the language.
 */
class ShieldOnNode implements ProgramNode{
    public ShieldOnNode(){}
    public void execute (Robot robot){
        robot.setShield(true);
    }
    public String toString(){
        return "shieldOn";
    }
}

/**
 * This class represents a shieldOff statement in the language.
 */
class ShieldOffNode implements ProgramNode{
    public ShieldOffNode(){}
    public void execute (Robot robot){
        robot.setShield(false);
    }
    public String toString(){
        return "shieldOff";
    }
}

/**
 * This class represents an if statement in the language. if the condition is true, the block will be executed.
 */
class IfNode implements ProgramNode{
    BooleanNode condition;
    ProgramNode block;
    public IfNode(BooleanNode condition, ProgramNode block){
        this.condition = condition;
        this.block = block;
    }
    public void execute (Robot robot){
        if (condition.evaluate(robot)){
            block.execute(robot);
        }
    }
    public String toString(){
        return "if(" + condition.toString() + "){" + block.toString() + "}";
    }
}

/**
 * This class represents an if-else statement in the language. if the condition is true, the block will be executed, otherwise the elseBlock will be executed.
 */
class IfElseNode implements ProgramNode{
    BooleanNode condition;
    ProgramNode block;
    ProgramNode elseBlock;

    public IfElseNode(BooleanNode condition, ProgramNode block, ProgramNode elseBlock){
        this.condition = condition;
        this.block = block;
        this.elseBlock = elseBlock;
    }
    public void execute (Robot robot){
        if (condition.evaluate(robot)){
            block.execute(robot);
        }
        else {elseBlock.execute(robot);}
    }
    public String toString(){
        return ("if(" + condition.toString() + "){" + block.toString() + "} else{" + elseBlock.toString()+"}");
    }
}

/**
 * This class represents a while statement in the language. if the condition is true, the block will be executed, while it is true.
 */
class WhileNode implements ProgramNode{
    BooleanNode condition;
    ProgramNode block;
    public WhileNode(BooleanNode condition, ProgramNode block){
        this.condition = condition;
        this.block = block;
    }
    public void execute (Robot robot){
        while (condition.evaluate(robot)){
            block.execute(robot);
        }
    }
    public String toString(){
        return "while(" + condition.toString() + "){" + block.toString() + "}";
    }
}

/**
 * This class represents a less than expression in the language. It tests if the integer value of expr1 is less than the integer value of expr2.
 */
class LtNode implements BooleanNode{
    IntNode expr1;
    IntNode expr2;
    public LtNode(IntNode expr1, IntNode expr2){
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    public boolean evaluate(Robot robot){
        return expr1.evaluate(robot) < expr2.evaluate(robot);
    }
    public String toString(){
        return "lt("+expr1.toString()+", "+expr1.toString()+")";
    }
}

/**
 * This class represents a greater than expression in the language. It tests if the integer value of expr1 is greater than the integer value of expr2.
 */
class GtNode implements BooleanNode{
    IntNode expr1;
    IntNode expr2;
    public GtNode(IntNode expr1, IntNode expr2){
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    public boolean evaluate(Robot robot){
        return expr1.evaluate(robot) > expr2.evaluate(robot);
    }
    public String toString(){
        return "gt("+expr1.toString()+", "+expr2.toString()+")";
    }
}

/**
 * This class represents an equal to expression in the language. It tests if the integer value of expr1 is equal to the integer value of expr2.
 */
class EqNode implements BooleanNode{
    IntNode expr1;
    IntNode expr2;
    public EqNode(IntNode expr1, IntNode expr2){
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    public boolean evaluate(Robot robot){
        return expr1.evaluate(robot) == expr2.evaluate(robot);
    }
    public String toString(){
        return "eq("+expr1.toString()+", "+expr2.toString()+")";
    }
}

/**
 * This class represents an and expression in the language. It tests if the boolean value of expr1 is true and the boolean value of expr2 is true.
 */
class AndNode implements BooleanNode{
    BooleanNode left;
    BooleanNode right;
    public AndNode(BooleanNode left, BooleanNode right){
        this.left = left;
        this.right = right;
    }
    public boolean evaluate(Robot robot){
        return left.evaluate(robot) && right.evaluate(robot);
    }
    public String toString(){
        return "and("+left.toString()+", "+right.toString()+")";
    }
}

/**
 * This class represents an or expression in the language. It tests if the boolean value of expr1 is true or the boolean value of expr2 is true.
 */
class OrNode implements BooleanNode{
    BooleanNode left;
    BooleanNode right;
    public OrNode(BooleanNode left, BooleanNode right){
        this.left = left;
        this.right = right;
    }
    public boolean evaluate(Robot robot){
        return left.evaluate(robot) || right.evaluate(robot);
    }
    public String toString(){
        return "or("+left.toString()+", "+right.toString()+")";
    }
}

/**
 * This class represents a not expression in the language. It tests if the boolean value of expr is false.
 */
class NotNode implements BooleanNode{
    BooleanNode expr;
    public NotNode(BooleanNode expr){
        this.expr = expr;
    }
    public boolean evaluate(Robot robot){
        return !expr.evaluate(robot);
    }
    public String toString(){
        return "not("+expr.toString()+")";
    }
}

/**
 * This class represents a number in the language. It simply returns an integer value.
 */
class NumNode implements IntNode{
    int value;
    public NumNode(int value){
        this.value = value;
    }
    public int evaluate(Robot robot){
        return value;
    }
    public String toString(){
        return "num: "+value;
    }
}

/**
 * This class represents a fuelLeft expression in the language. It returns the amount of fuel left in the robot.
 */
class FuelLeftNode implements IntNode{
    int value;
    public FuelLeftNode(){}
    public int evaluate(Robot robot){
        value = robot.getFuel();
        return value;
    }
    public String toString(){
        return "fuelLeft: "+value;
    }
}

/**
 * This class represents a OppLR expression in the language. It returns the distance between the robot and the opponent in the left or right direction.
 */
class OppLRNode implements IntNode{
    int value;
    public OppLRNode(){}
    public int evaluate(Robot robot){
        value = robot.getOpponentLR();
        return value;
    }
    public String toString(){
        return "oppLR: "+value;
    }
}

/**
 * This class represents a OppFB expression in the language. It returns the distance between the robot and the opponent in the forward or backward direction.
 */
class OppFBNode implements IntNode{
    int value;
    public OppFBNode(){}
    public int evaluate(Robot robot){
        value = robot.getOpponentFB();
        return value;
    }
    public String toString(){
        return "oppFB: "+value;
    }
}

/**
 * This class represents a numBarrels expression in the language. It returns the number of barrels left in the game.
 */
class NumBarrelsNode implements IntNode{
    int value;
    public NumBarrelsNode(){}
    public int evaluate(Robot robot){
        value = robot.numBarrels();
        return value;
    }
    public String toString(){
        return "numBarrels: "+value;
    }
}

/**
 * This class represents a BarrelLR expression in the language. It returns the distance between the robot and the barrel in the left or right direction.
 */
class BarrelLRNode implements IntNode{
    IntNode expr;
    public BarrelLRNode(IntNode expr)   {this.expr = expr;}
    public int evaluate(Robot robot){
        if (expr == null){
            return robot.getClosestBarrelLR();
        }
        return robot.getBarrelLR(expr.evaluate(robot));
    }
    public String toString(){
        if (expr == null){
            return "barrelLR: closest";
        }
        return "barrelLR: "+expr.toString();
    }
}

/**
 * This class represents a BarrelFB expression in the language. It returns the distance between the robot and the barrel in the forward or backward direction.
 */
class BarrelFBNode implements IntNode{
    IntNode expr;
    public BarrelFBNode(IntNode expr)   {this.expr = expr;}
    public int evaluate(Robot robot){
        if (expr == null){
            return robot.getClosestBarrelFB();
        }
        return robot.getBarrelFB(expr.evaluate(robot));
    }
    public String toString(){
        if (expr == null){
            return "barrelFB: closest";
        }
        return "barrelFB: "+expr.toString();

    }
}

/**
 * This class represents a wallDist expression in the language. It returns the distance between the robot and the wall in the direction it is facing.
 */
class WallDistNode implements IntNode{
    int value;
    public WallDistNode(){}
    public int evaluate(Robot robot){
        value = robot.getDistanceToWall();
        return value;
    }
    public String toString(){
        return "wallDist: "+value;
    }
}

/**
 * This class represents an add expression in the language. It returns the sum of the integer values of expr1 and expr2.
 */
class AddNode implements IntNode{
    IntNode left;
    IntNode right;
    int value;
    public AddNode(IntNode left, IntNode right){
        this.left = left;
        this.right = right;
    }
    public int evaluate(Robot robot){
        value = left.evaluate(robot) + right.evaluate(robot);
        return value;
    }
    public String toString(){
        return "add("+left.toString()+", "+right.toString()+")";
    }
}

/**
 * This class represents a sub expression in the language. It returns the difference of the integer values of expr1 and expr2.
 */
class SubNode implements IntNode{
    IntNode left;
    IntNode right;
    int value;
    public SubNode(IntNode left, IntNode right){
        this.left = left;
        this.right = right;
    }
    public int evaluate(Robot robot){
        value = left.evaluate(robot) - right.evaluate(robot);
        return value;
    }
    public String toString(){
        return "sub("+left.toString()+", "+right.toString()+")";
    }
}

/**
 * This class represents a multiply expression in the language. It returns the product of the integer values of expr1 and expr2.
 */
class MulNode implements IntNode{
    IntNode left;
    IntNode right;
    int value;
    public MulNode(IntNode left, IntNode right){
        this.left = left;
        this.right = right;
    }
    public int evaluate(Robot robot){
        value = left.evaluate(robot) * right.evaluate(robot);
        return value;
    }
    public String toString(){
        return "mul("+left.toString()+", "+right.toString()+")";
    }
}

/**
 * This class represents a divide expression in the language. It returns the quotient of the integer values of expr1 and expr2.
 */
class DivNode implements IntNode{
    IntNode left;
    IntNode right;
    int value;
    public DivNode(IntNode left, IntNode right){
        this.left = left;
        this.right = right;
    }
    public int evaluate(Robot robot){
        value = left.evaluate(robot) / right.evaluate(robot);
        return value;
    }
    public String toString(){
        return "div("+left.toString()+", "+right.toString()+")";
    }
}


