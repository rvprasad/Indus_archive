import antlr.*;
public class TestLevel2Main {
    public static void main(String[] args) throws Exception {
        TestLevel2Lexer lexer = new TestLevel2Lexer(System.in);
        TestLevel2Parser parser = new TestLevel2Parser(lexer);
        parser.firstRule();
        System.out.println(parser.getAST().toStringTree());
    }
}
