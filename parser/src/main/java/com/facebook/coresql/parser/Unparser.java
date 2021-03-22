/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.coresql.parser;

import static com.facebook.coresql.parser.SqlParserConstants.EOF;

public class Unparser
        extends SqlParserDefaultVisitor
{
    protected StringBuilder stringBuilder = new StringBuilder();
    private Token lastToken = new Token();

    public static String unparse(AstNode node, Unparser unparser)
    {
        unparser.stringBuilder.setLength(0);
        unparser.lastToken.next = node.beginToken;
        node.jjtAccept(unparser, null);
        unparser.printTrailingComments();
        return unparser.stringBuilder.toString();
    }

    public static String unparse(AstNode node)
    {
        return unparse(node, new Unparser());
    }

    private void printSpecialTokens(Token t)
    {
        // special tokens are chained in reverse.
        Token specialToken = t.specialToken;
        if (specialToken != null) {
            while (specialToken.specialToken != null) {
                specialToken = specialToken.specialToken;
            }
            while (specialToken != null) {
                stringBuilder.append(specialToken.image);
                specialToken = specialToken.next;
            }
        }
    }

    private void printTrailingComments()
    {
        if (lastToken != null && lastToken.kind == EOF) {
            printSpecialTokens(lastToken);
        }
    }

    public final void printToken(String s)
    {
        stringBuilder.append(" " + s + " ");
    }

    private void printToken(Token t)
    {
        while (lastToken != t) {
            lastToken = lastToken.next;
            printSpecialTokens(lastToken);
            stringBuilder.append(lastToken.image);
        }
    }

    protected void printUptoToken(Token t)
    {
        while (lastToken.next != t) {
            printToken(lastToken.next);
        }
    }

    protected void moveToEndOfNode(AstNode node)
    {
        lastToken = node.endToken;
    }

    protected void unparseUpto(AstNode node)
    {
        printUptoToken(node.beginToken);
    }

    protected void unparseRestOfTheNode(AstNode node)
    {
        if (lastToken != node.endToken) {
            printUptoToken(node.endToken);
        }
    }

    @Override
    public void defaultVisit(SimpleNode node, Void data)
    {
        AstNode astNode = (AstNode) node;
        AstNode firstChild = astNode.FirstChild();
        AstNode lastChild = astNode.LastChild();

        // Print the tokens.
        if (firstChild == null || astNode.beginToken != firstChild.beginToken) {
            printToken(astNode.beginToken);
        }

        node.childrenAccept(this, data);

        if (lastChild != null && astNode.endToken != lastChild.endToken) {
            printToken(astNode.endToken);
        }
    }
}
