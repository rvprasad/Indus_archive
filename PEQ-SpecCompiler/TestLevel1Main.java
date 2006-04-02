import antlr.*;
import antlr.collections.AST;

public class TestLevel1Main {
    public static void main(String[] args) throws Exception {
        TokenStreamSelector selector = new TokenStreamSelector();

        TestLevel1Lexer lexer1 = new TestLevel1Lexer(System.in);
        TestLevel2Lexer lexer2 = new TestLevel2Lexer(lexer1.getInputState());
        
        String level2LexerName = "lexer2";
        lexer1.setSelector(selector);
        lexer1.setLevel2LexerName(level2LexerName);
        lexer2.setSelector(selector);
        selector.addInputStream(lexer1, "lexer1");
        selector.addInputStream(lexer2, level2LexerName);
        selector.select(lexer1);

        TestLevel1Parser parser = new TestLevel1Parser(selector);
        parser.setLevel2Parser(new TestLevel2Parser((TokenStream) null));
        parser.firstRule();
        AST ast = parser.getAST();
        System.out.println(ast.toStringTree());
    }
}
