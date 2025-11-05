import java.io.*;
import java.nio.file.*;
import java.util.*;


public class MiniInterpreter {
    enum Tok {
    END, INT, ID,
    KW_IF, KW_ELSE, KW_WHILE, KW_FUNC, KW_RETURN, KW_PRINT, KW_INT,
    PLUS, MINUS, MUL, DIV, MOD,
    ASSIGN, EQ, NEQ, LT, LTE, GT, GTE,
    LPAREN, RPAREN, LBRACE, RBRACE, COMMA, SEMI
}


    static class Token {
        Tok type;
        String text;
        long intVal;
        Token(Tok t, String txt) { type = t; text = (txt==null?"":txt); }
        Token(long v) { type = Tok.INT; intVal = v; text = Long.toString(v); }
        public String toString(){ return type + ":" + (text==null?"":text); }
    }

    // Token FIFO between lexer and parser
    static class TokenQueue {
        private final LinkedList<Token> q = new LinkedList<>();
        void push(Token t){ q.addLast(t); }
        Token pop(){ return q.isEmpty()? new Token(Tok.END, ""): q.removeFirst(); }
        Token peek(){ return q.isEmpty()? new Token(Tok.END, ""): q.getFirst(); }
        Token peek(int n){
            if (n <= 0) return peek();
            if (q.size() <= n) return new Token(Tok.END, "");
            return q.get(n);
        }
        boolean isEmpty(){ return q.isEmpty(); }
        void clear(){ q.clear(); }
    }

    // --- runtime / lexical buffers (reset before each run)
    static String src;
    static int pos;
    static TokenQueue tokenQueue = new TokenQueue();

    // KMP (Knuth-Morris-Pratt) utility — included/available for string search tasks
    static int[] kmpBuild(String pat){
        int m = pat.length();
        int[] lps = new int[m];
        int len = 0; if (m>0) lps[0] = 0;
        for (int i=1;i<m;){
            if (pat.charAt(i)==pat.charAt(len)){ len++; lps[i]=len; i++; }
            else {
                if (len!=0) len = lps[len-1];
                else { lps[i]=0; i++; }
            }
        }
        return lps;
    }
    static int kmpFind(String text, String pat, int from){
        if (pat.length()==0) return from;
        int[] lps = kmpBuild(pat);
        int i = from, j = 0;
        while (i < text.length()){
            if (text.charAt(i) == pat.charAt(j)){ i++; j++; if (j==pat.length()) return i-j; }
            else {
                if (j!=0) j = lps[j-1];
                else i++;
            }
        }
        return -1;
    }

    static boolean isIdentStart(char c){ return Character.isLetter(c) || c == '_'; }
    static boolean isIdentChar(char c){ return Character.isLetterOrDigit(c) || c == '_'; }

    static void lexAll() {
        tokenQueue.clear();
        pos = 0;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            // whitespace
            if (Character.isWhitespace(c)){ pos++; continue; }
            // comments: // or /* */
            if (c=='/' && pos+1 < src.length() && src.charAt(pos+1)=='/') {
                pos += 2; while (pos < src.length() && src.charAt(pos)!='\n') pos++;
                continue;
            }
            if (c=='/' && pos+1 < src.length() && src.charAt(pos+1)=='*') {
                pos += 2;
                int end = kmpFind(src, "*/", pos);
                if (end < 0) pos = src.length(); else pos = end + 2;
                continue;
            }
            // numbers
            if (Character.isDigit(c)) {
                long val = 0;
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                    val = val*10 + (src.charAt(pos)-'0');
                    pos++;
                }
                tokenQueue.push(new Token(val));
                continue;
            }
            // identifier or keyword
            if (isIdentStart(c)) {
                int start = pos;
                pos++;
                while (pos < src.length() && isIdentChar(src.charAt(pos))) pos++;
                String word = src.substring(start, pos);
                switch(word){
                    case "if": tokenQueue.push(new Token(Tok.KW_IF, word)); break;
                    case "else": tokenQueue.push(new Token(Tok.KW_ELSE, word)); break;
                    case "while": tokenQueue.push(new Token(Tok.KW_WHILE, word)); break;
                    case "func": tokenQueue.push(new Token(Tok.KW_FUNC, word)); break;
                    case "return": tokenQueue.push(new Token(Tok.KW_RETURN, word)); break;
                    case "print": tokenQueue.push(new Token(Tok.KW_PRINT, word)); break;
                    default: tokenQueue.push(new Token(Tok.ID, word)); break;
                }
                continue;
            }
            // two-char operators
            if (pos+1 < src.length()){
                String two = src.substring(pos, pos+2);
                switch(two){
                    case "==": tokenQueue.push(new Token(Tok.EQ, two)); pos+=2; continue;
                    case "!=": tokenQueue.push(new Token(Tok.NEQ, two)); pos+=2; continue;
                    case "<=": tokenQueue.push(new Token(Tok.LTE, two)); pos+=2; continue;
                    case ">=": tokenQueue.push(new Token(Tok.GTE, two)); pos+=2; continue;
                }
            }
            // single-char tokens
            switch(c){
                case '+': tokenQueue.push(new Token(Tok.PLUS, "+")); pos++; break;
                case '-': tokenQueue.push(new Token(Tok.MINUS, "-")); pos++; break;
                case '*': tokenQueue.push(new Token(Tok.MUL, "*")); pos++; break;
                case '/': tokenQueue.push(new Token(Tok.DIV, "/")); pos++; break;
                case '%': tokenQueue.push(new Token(Tok.MOD, "%")); pos++; break;
                case '=': tokenQueue.push(new Token(Tok.ASSIGN, "=")); pos++; break;
                case '<': tokenQueue.push(new Token(Tok.LT, "<")); pos++; break;
                case '>': tokenQueue.push(new Token(Tok.GT, ">")); pos++; break;
                case '(': tokenQueue.push(new Token(Tok.LPAREN, "(")); pos++; break;
                case ')': tokenQueue.push(new Token(Tok.RPAREN, ")")); pos++; break;
                case '{': tokenQueue.push(new Token(Tok.LBRACE, "{")); pos++; break;
                case '}': tokenQueue.push(new Token(Tok.RBRACE, "}")); pos++; break;
                case ',': tokenQueue.push(new Token(Tok.COMMA, ",")); pos++; break;
                case ';': tokenQueue.push(new Token(Tok.SEMI, ";")); pos++; break;
                default:
                    throw new RuntimeException("Unknown char at pos " + pos + ": '" + c + "'");
            }
        }
        tokenQueue.push(new Token(Tok.END, ""));
    }

    /* ---------------------------
       AST Node definitions
       --------------------------- */

    static abstract class AST {}
    static abstract class Stmt extends AST {}
    static abstract class Expr extends AST {}

    // Expressions
    static class IntLiteral extends Expr {
        long value; IntLiteral(long v){ value = v; }
    }
    static class VarExpr extends Expr {
        String name; VarExpr(String n){ name = n; }
    }
    static class BinaryExpr extends Expr {
        String op; Expr left, right;
        BinaryExpr(String op, Expr l, Expr r){ this.op=op; left=l; right=r;}
    }
    static class CallExpr extends Expr {
        String fname; List<Expr> args;
        CallExpr(String f, List<Expr> a){ fname=f; args=a; }
    }

    // Statements
    static class BlockStmt extends Stmt {
        List<Stmt> stmts = new ArrayList<>();
    }
    static class ExprStmt extends Stmt {
        Expr expr; ExprStmt(Expr e){ expr = e; }
    }
    static class IfStmt extends Stmt {
        Expr cond; Stmt thenBranch; Stmt elseBranch;
        IfStmt(Expr c, Stmt t, Stmt e){ cond = c; thenBranch=t; elseBranch=e; }
    }
    static class WhileStmt extends Stmt {
        Expr cond; Stmt body;
        WhileStmt(Expr c, Stmt b){ cond = c; body=b; }
    }
    static class ReturnStmt extends Stmt {
        Expr expr; ReturnStmt(Expr e){ expr = e; }
    }
    static class VarDeclStmt extends Stmt {
        String name; Expr init;
        VarDeclStmt(String n, Expr i){ name=n; init=i;}
    }
    static class AssignStmt extends Stmt {
        String name; Expr expr;
        AssignStmt(String n, Expr e){ name=n; expr=e; }
    }
    static class FuncDef {
        String name;
        List<String> params;
        BlockStmt body;
        FuncDef(String n, List<String> p, BlockStmt b){ name=n; params=p; body=b; }
    }

    /* ---------------------------
       Parser (recursive-descent)
       --------------------------- */

    static Token cur() { return tokenQueue.peek(); }
    static Token eat() { return tokenQueue.pop(); }
    static boolean accept(Tok t){
        if (cur().type == t){ eat(); return true; }
        return false;
    }
    static void expect(Tok t){
        if (cur().type != t) throw new RuntimeException("Expected " + t + " but got " + cur());
        eat();
    }

    static List<FuncDef> functions;
    static BlockStmt programBody;

    static void parseProgram(){
        programBody = new BlockStmt();
        while (cur().type != Tok.END) {
            if (cur().type == Tok.KW_FUNC) {
                functions.add(parseFunc());
            } else {
                // top-level statement
                programBody.stmts.add(parseStmt());
            }
        }
    }

    static FuncDef parseFunc(){
        expect(Tok.KW_FUNC);
        if (cur().type != Tok.ID) throw new RuntimeException("Function name expected");
        String fname = eat().text;
        expect(Tok.LPAREN);
        List<String> params = new ArrayList<>();
        if (cur().type != Tok.RPAREN){
            params.add(eat().text);
            while (accept(Tok.COMMA)) {
                params.add(eat().text);
            }
        }
        expect(Tok.RPAREN);
        BlockStmt body = parseBlock();
        return new FuncDef(fname, params, body);
    }

    static BlockStmt parseBlock(){
        expect(Tok.LBRACE);
        BlockStmt block = new BlockStmt();
        while (cur().type != Tok.RBRACE && cur().type != Tok.END){
            block.stmts.add(parseStmt());
        }
        expect(Tok.RBRACE);
        return block;
    }

    static Stmt parseStmt(){
        Token t = cur();
        // return
        if (t.type == Tok.KW_RETURN){
            eat();
            Expr e = parseExpr();
            expect(Tok.SEMI);
            return new ReturnStmt(e);
        }
        // if
        if (t.type == Tok.KW_IF){
            eat();
            expect(Tok.LPAREN);
            Expr cond = parseExpr();
            expect(Tok.RPAREN);
            Stmt thenB = parseStmtOrBlock();
            Stmt elseB = null;
            if (cur().type == Tok.KW_ELSE){
                eat();
                elseB = parseStmtOrBlock();
            }
            return new IfStmt(cond, thenB, elseB);
        }
        // while
        if (t.type == Tok.KW_WHILE){
            eat();
            expect(Tok.LPAREN);
            Expr cond = parseExpr();
            expect(Tok.RPAREN);
            Stmt body = parseStmtOrBlock();
            return new WhileStmt(cond, body);
        }
        // block
        if (t.type == Tok.LBRACE) return parseBlock();
        // print
        if (t.type == Tok.KW_PRINT){
            eat();
            expect(Tok.LPAREN);
            Expr e = parseExpr();
            expect(Tok.RPAREN);
            expect(Tok.SEMI);
            return new ExprStmt(new CallExpr("print", List.of(e)));
        }
        // assignment or expression statement
        if (t.type == Tok.ID){
            // lookahead to check assignment vs call
            Token idTok = tokenQueue.peek(0);    // current ID
            Token nextTok = tokenQueue.peek(1);  // lookahead
            if (nextTok.type == Tok.ASSIGN){
                // consume id, assign, expr;
                eat(); // id
                eat(); // =
                Expr e = parseExpr();
                expect(Tok.SEMI);
                return new AssignStmt(idTok.text, e);
            } else if (nextTok.type == Tok.LPAREN){
                // function call as statement
                eat(); // id
                eat(); // LPAREN
                List<Expr> args = new ArrayList<>();
                if (cur().type != Tok.RPAREN){
                    args.add(parseExpr());
                    while (accept(Tok.COMMA)) args.add(parseExpr());
                }
                expect(Tok.RPAREN);
                expect(Tok.SEMI);
                return new ExprStmt(new CallExpr(idTok.text, args));
            } else {
                // treat as expression statement starting with ID (e.g., variable access as expression)
                Expr e = parseExpr();
                expect(Tok.SEMI);
                return new ExprStmt(e);
            }
        }
        // expression statement
        Expr e = parseExpr();
        expect(Tok.SEMI);
        return new ExprStmt(e);
    }

    static Stmt parseStmtOrBlock(){
        if (cur().type == Tok.LBRACE) return parseBlock();
        return parseStmt();
    }

    // Expression parsing with precedence
    static Expr parseExpr(){ return parseEquality(); }

    static Expr parseEquality(){
        Expr left = parseRelational();
        while (cur().type == Tok.EQ || cur().type == Tok.NEQ){
            String op = eat().text;
            Expr right = parseRelational();
            left = new BinaryExpr(op, left, right);
        }
        return left;
    }

    static Expr parseRelational(){
        Expr left = parseAddSub();
        while (cur().type == Tok.LT || cur().type == Tok.LTE || cur().type == Tok.GT || cur().type == Tok.GTE){
            String op = eat().text;
            Expr right = parseAddSub();
            left = new BinaryExpr(op, left, right);
        }
        return left;
    }

    static Expr parseAddSub(){
        Expr left = parseMulDiv();
        while (cur().type == Tok.PLUS || cur().type == Tok.MINUS){
            String op = eat().text;
            Expr right = parseMulDiv();
            left = new BinaryExpr(op, left, right);
        }
        return left;
    }

    static Expr parseMulDiv(){
        Expr left = parseUnary();
        while (cur().type == Tok.MUL || cur().type == Tok.DIV || cur().type == Tok.MOD){
            String op = eat().text;
            Expr right = parseUnary();
            left = new BinaryExpr(op, left, right);
        }
        return left;
    }

    static Expr parseUnary(){
        if (cur().type == Tok.MINUS){
            eat();
            Expr p = parseUnary();
            return new BinaryExpr("neg", new IntLiteral(0), p);
        }
        return parsePrimary();
    }

    static Expr parsePrimary(){
        Token t = cur();
        if (t.type == Tok.INT){ eat(); return new IntLiteral(t.intVal); }
        if (t.type == Tok.ID){
            String name = eat().text;
            if (cur().type == Tok.LPAREN){
                // call
                eat();
                List<Expr> args = new ArrayList<>();
                if (cur().type != Tok.RPAREN){
                    args.add(parseExpr());
                    while (accept(Tok.COMMA)) args.add(parseExpr());
                }
                expect(Tok.RPAREN);
                return new CallExpr(name, args);
            } else {
                return new VarExpr(name);
            }
        }
        if (t.type == Tok.LPAREN){
            eat();
            Expr e = parseExpr();
            expect(Tok.RPAREN);
            return e;
        }
        throw new RuntimeException("Unexpected primary token: " + t);
    }

    /* ---------------------------
       Symbol table, Activation record stack, and Evaluator
       --------------------------- */

    // Symbol metadata for global symbols (functions and globals)
    static class Symbol {
        enum Kind { VAR, FUNC }
        Kind kind;
        String name;
        Object info; // for VAR -> value boxed Long, for FUNC -> FuncDef
        Symbol(Kind k, String n, Object i){ kind=k; name=n; info=i; }
    }

    // Global symbol table (hash map)
    static HashMap<String, Symbol> globalSym;

    // Activation Record representing a function call's local scope & return info
    static class ActivationRecord {
        String funcName;
        HashMap<String, Long> locals = new HashMap<>();
        ActivationRecord(String f){ funcName = f; }
    }

    // Call stack of activation records
    static Deque<ActivationRecord> callStack;

    // A runtime exception used to implement 'return' flow control with a value
    static class ReturnException extends RuntimeException {
        Long value;
        ReturnException(Long v){ value = v; }
    }

    static Long evalExpr(Expr e){
        if (e instanceof IntLiteral) return ((IntLiteral)e).value;
        if (e instanceof VarExpr){
            String name = ((VarExpr)e).name;
            Long val = lookupVar(name);
            if (val == null) throw new RuntimeException("Undefined variable: " + name);
            return val;
        }
        if (e instanceof BinaryExpr){
            BinaryExpr be = (BinaryExpr)e;
            Long L = evalExpr(be.left);
            Long R = evalExpr(be.right);
            switch(be.op){
                case "+": return L + R;
                case "-": return L - R;
                case "*": return L * R;
                case "/": if (R==0) throw new RuntimeException("Division by zero"); return L / R;
                case "%": return L % R;
                case "==": return (L.equals(R))?1L:0L;
                case "!=": return (!L.equals(R))?1L:0L;
                case "<": return (L < R)?1L:0L;
                case "<=": return (L <= R)?1L:0L;
                case ">": return (L > R)?1L:0L;
                case ">=": return (L >= R)?1L:0L;
                case "neg": return -R;
                default: throw new RuntimeException("Unknown binary op: " + be.op);
            }
        }
        if (e instanceof CallExpr){
            CallExpr ce = (CallExpr)e;
            if (ce.fname.equals("print")){
                if (ce.args.size() != 1) throw new RuntimeException("print takes 1 argument");
                Long v = evalExpr(ce.args.get(0));
                System.out.println(v);
                return 0L;
            }
            Symbol sym = globalSym.get(ce.fname);
            if (sym == null || sym.kind != Symbol.Kind.FUNC) throw new RuntimeException("Unknown function: " + ce.fname);
            FuncDef fd = (FuncDef) sym.info;
            if (fd.params.size() != ce.args.size()) throw new RuntimeException("Arity mismatch for " + ce.fname);
            // prepare activation record
            ActivationRecord ar = new ActivationRecord(ce.fname);
            for (int i=0;i<fd.params.size();i++){
                Long argVal = evalExpr(ce.args.get(i));
                ar.locals.put(fd.params.get(i), argVal);
            }
            callStack.push(ar);
            try {
                execBlock(fd.body);
            } catch (ReturnException re){
                callStack.pop();
                return re.value == null ? 0L : re.value;
            }
            callStack.pop();
            return 0L;
        }
        throw new RuntimeException("Unknown expr type: " + e);
    }

    static Long lookupVar(String name){
        // check local activation records top-down
        for (ActivationRecord ar : callStack) {
            if (ar.locals.containsKey(name)) return ar.locals.get(name);
        }
        // then globals
        Symbol sym = globalSym.get(name);
        if (sym != null && sym.kind == Symbol.Kind.VAR) return (Long)sym.info;
        return null;
    }

    static void assignVar(String name, Long value){
        // assign to nearest local; if none exist, create global
        for (ActivationRecord ar : callStack) {
            if (ar.locals.containsKey(name)){
                ar.locals.put(name, value);
                return;
            }
        }
        // global
        globalSym.put(name, new Symbol(Symbol.Kind.VAR, name, value));
    }

    static void execStmt(Stmt s){
        if (s instanceof BlockStmt) execBlock((BlockStmt)s);
        else if (s instanceof ExprStmt) evalExpr(((ExprStmt)s).expr);
        else if (s instanceof IfStmt){
            IfStmt is = (IfStmt)s;
            Long cond = evalExpr(is.cond);
            if (cond != 0) execStmt(is.thenBranch);
            else if (is.elseBranch != null) execStmt(is.elseBranch);
        } else if (s instanceof WhileStmt){
            WhileStmt ws = (WhileStmt)s;
            while (evalExpr(ws.cond) != 0) execStmt(ws.body);
        } else if (s instanceof ReturnStmt){
            ReturnStmt rs = (ReturnStmt)s;
            Long val = rs.expr == null ? 0L : evalExpr(rs.expr);
            throw new ReturnException(val);
        } else if (s instanceof AssignStmt){
            AssignStmt as = (AssignStmt)s;
            Long val = evalExpr(as.expr);
            assignVar(as.name, val);
        } else {
            throw new RuntimeException("Unsupported stmt execution: " + s);
        }
    }

    static void execBlock(BlockStmt block){
        for (Stmt st : block.stmts){
            execStmt(st);
        }
    }

    /* ---------------------------
       CFG builder (simple stub using graph structures)
       --------------------------- */

    static class CFGNode {
        int id;
        String label;
        List<CFGNode> outs = new ArrayList<>();
        CFGNode(int id, String label){ this.id = id; this.label = label; }
    }
    static class CFG {
        List<CFGNode> nodes = new ArrayList<>();
        Map<Integer, CFGNode> map = new HashMap<>();
        int nextId = 1;
        CFGNode newNode(String label){
            CFGNode n = new CFGNode(nextId++, label);
            nodes.add(n); map.put(n.id, n); return n;
        }
    }

    static CFG buildCFGForFunction(FuncDef f){
        // Very basic CFG: each statement becomes a node with edges in sequence.
        CFG cfg = new CFG();
        CFGNode entry = cfg.newNode("entry");
        CFGNode prev = entry;
        for (Stmt s : f.body.stmts){
            CFGNode n = cfg.newNode(s.getClass().getSimpleName());
            prev.outs.add(n);
            prev = n;
        }
        CFGNode exit = cfg.newNode("exit");
        prev.outs.add(exit);
        return cfg;
    }

    /* ---------------------------
       Top-level runner
       --------------------------- */

    static void registerFunctions(){
        for (FuncDef fd : functions){
            globalSym.put(fd.name, new Symbol(Symbol.Kind.FUNC, fd.name, fd));
        }
    }

    static void runProgram(){
        // register functions first
        registerFunctions();
        // build CFGs for each function (example of graph construction)
        for (FuncDef fd : functions){
            CFG cfg = buildCFGForFunction(fd);
            // For now we simply print a summary:
            System.err.println("[CFG] Function " + fd.name + " has " + cfg.nodes.size() + " nodes");
        }
        // execute top-level statements as "main"
        ActivationRecord mainAr = new ActivationRecord("main");
        callStack.push(mainAr);
        try {
            execBlock(programBody);
        } catch (ReturnException re){
            // ignore return at top-level
        }
        callStack.pop();
    }

    /* ---------------------------
       API: runSource
       --------------------------- */

    /**
     * Run source code and return combined stdout+stderr output.
     * Resets internal state so this method can be called repeatedly.
     */
    public static String runSource(String source) {
        // Reset all runtime/parser state
        src = source == null ? "" : source;
        tokenQueue = new TokenQueue();
        functions = new ArrayList<>();
        programBody = null;
        globalSym = new HashMap<>();
        callStack = new ArrayDeque<>();

        // Capture stdout and stderr
        ByteArrayOutputStream baosOut = new ByteArrayOutputStream();
        ByteArrayOutputStream baosErr = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        try (PrintStream psOut = new PrintStream(baosOut);
             PrintStream psErr = new PrintStream(baosErr)) {
            System.setOut(psOut);
            System.setErr(psErr);

            // Lex -> populate token queue
            lexAll();
            // Parse
            parseProgram();
            // Run
            runProgram();

            psOut.flush();
            psErr.flush();
        } catch (Exception ex) {
            // restore streams before throwing or returning
            System.setOut(oldOut);
            System.setErr(oldErr);
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            return "Runtime Error:\n" + sw.toString();
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }

        // combine stderr then stdout, so CFG summaries (err) appear before print outputs (out)
        String errPart = baosErr.toString();
        String outPart = baosOut.toString();
        String combined = (errPart.isEmpty() ? "" : errPart) + (outPart.isEmpty() ? "" : outPart);
        return combined.isEmpty() ? "(no output)\n" : combined;
    }

    /* ---------------------------
       Demo / Main
       --------------------------- */

    public static void main(String[] args) throws Exception {
        if (args.length == 0){
            System.out.println("Usage: java MiniInterpreter <sourcefile>");
            System.out.println("No file provided — running built-in demo.\n");
            String output = runSource(demoProgram());
            System.out.print(output);
        } else {
            String srcFile = new String(Files.readAllBytes(Paths.get(args[0])));
            String output = runSource(srcFile);
            System.out.print(output);
        }
    }

    // kept public so UI can load it
    public static String demoProgram(){
        return """
        // Demo language for MiniInterpreter
        // Factorial using recursion, plus loops, conditionals, and print

        func fact(n) {
            if (n == 0) {
                return 1;
            } else {
                return n * fact(n - 1);
            }
        }

        x = 7;
        y = fact(x);
        print(y);

        // iterative sum
        func sumN(n) {
            i = 0;
            acc = 0;
            while (i < n) {
                i = i + 1;
                acc = acc + i;
            }
            return acc;
        }

        print(sumN(10));
        """;
    }
}
