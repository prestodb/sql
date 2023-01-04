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

import hu.webarticum.treeprinter.SimpleTreeNode;
import hu.webarticum.treeprinter.TreeNode;
import hu.webarticum.treeprinter.printer.listing.ListingTreePrinter;
import org.junit.jupiter.api.Assertions;

import java.io.StringReader;
import java.util.regex.Pattern;

public final class TestUtils
{
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("(--.*$)|(/\\*.*?\\*/)", Pattern.MULTILINE);
    private static final Pattern SQL_SANITATION_PATTERN = Pattern.compile("(\\s+)", Pattern.MULTILINE);
    // Assure SPACE around Syntax Characters
    private static final Pattern SQL_SANITATION_PATTERN2 = Pattern
            .compile("\\s*([!/,()=+\\-*|\\]<>:])\\s*", Pattern.MULTILINE);

    private TestUtils()
    {}

    public static String buildSqlString(final String originalSql, boolean relaxed)
    {
        if (relaxed) {
            // remove comments
            String sanitizedSqlStr = SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");
            // redundant white space
            sanitizedSqlStr = SQL_SANITATION_PATTERN.matcher(sanitizedSqlStr).replaceAll(" ");
            // assure spacing around Syntax Characters
            sanitizedSqlStr = SQL_SANITATION_PATTERN2.matcher(sanitizedSqlStr).replaceAll("$1");
            return sanitizedSqlStr.trim().toLowerCase();
        }
        else {
            // remove comments only
            return SQL_COMMENT_PATTERN.matcher(originalSql).replaceAll("");
        }
    }

    // re-implement this method since we want to catch the Error
    public static AstNode parseStatement(String sql) throws ParseException
    {
        SqlParserTokenManager tokenManager = new SqlParserTokenManager(
                new SimpleCharStream(new StringReader(sql), 1, 1));
        SqlParser parser = new SqlParser(tokenManager);
        parser.direct_SQL_statement();
        return parser.getResult();
    }

    public static AstNode assertParseAndUnparse(String sqlStr, boolean relaxed)
    {
        String expectedSqlStr = buildSqlString(sqlStr, relaxed);
        AstNode ast = null;
        try {
            ast = parseStatement(sqlStr);
            String actualSqlStr = buildSqlString(Unparser.unparse(ast), relaxed);
            Assertions.assertEquals(expectedSqlStr, actualSqlStr, "Failed SQL:\n" + sqlStr);
        }
        catch (ParseException ex) {
            Assertions.fail(ex.getLocalizedMessage() + "\n" + sqlStr);
        }
        return ast;
    }

    public static AstNode assertParseAndUnparse(String sqlStr)
    {
        return assertParseAndUnparse(sqlStr, true);
    }

    public static SimpleTreeNode translateNode(AstNode astNode)
    {
        String image = astNode.GetImage() == null || astNode.GetImage().isEmpty()
                ? astNode.toString() + " [" + astNode.getLocation() + "]"
                : astNode.toString() + ": " + astNode.GetImage() + " [" + astNode.getLocation() + "]";
        SimpleTreeNode simpleTreeNode = new SimpleTreeNode(image);
        AstNode[] astNodeChildren = new AstNode[astNode.NumChildren()];
        for (int i = 0; i < astNode.NumChildren(); i++) {
            simpleTreeNode.addChild(translateNode(astNode.GetChild(i)));
        }
        return simpleTreeNode;
    }

    public static String formatToTree(AstNode astNode) throws Exception
    {
        TreeNode rootTreeNode = null;
        SimpleTreeNode rootNode = new SimpleTreeNode("SQL Text");
        rootNode.addChild(translateNode(astNode));
        return new ListingTreePrinter().stringify(rootNode);
    }
}
