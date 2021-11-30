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

import com.facebook.coresql.linter.warning.WarningCollector;

import java.io.StringReader;

public class AstNode
        extends com.facebook.coresql.parser.SimpleNode
{
    public Token beginToken;
    public Token endToken;

    protected AstNode(int id)
    {
        super(id);
    }

    protected AstNode(com.facebook.coresql.parser.SqlParser parser, int id)
    {
        super(parser, id);
    }

    public int Kind()
    {
        return id;
    }

    public final int NumChildren()
    {
        return jjtGetNumChildren();
    }

    public final AstNode GetChild(int i)
    {
        return (AstNode) jjtGetChild(i);
    }

    public String GetImage()
    {
        return NumChildren() == 0 ? beginToken.image : null;
    }

    public final void VisitNode(com.facebook.coresql.parser.SqlParserVisitor visitor)
    {
        jjtAccept(visitor, null);
    }

    public final AstNode FirstChild()
    {
        return NumChildren() > 0 ? GetChild(0) : null;
    }

    public final AstNode LastChild()
    {
        return NumChildren() > 0 ? GetChild(NumChildren() - 1) : null;
    }

    public final AstNode GetFirstChildOfKind(int kind)
    {
        for (int i = 0; i < NumChildren(); i++) {
            if (GetChild(i).Kind() == kind) {
                return GetChild(i);
            }
        }

        return null;
    }

    public final boolean hasChildOfKind(int kind)
    {
        return GetFirstChildOfKind(kind) != null;
    }

    public Location getLocation()
    {
        return new Location(beginToken.beginLine, beginToken.beginColumn, endToken.endLine, endToken.endColumn);
    }

    @Override
    public String toString(String prefix)
    {
        return super.toString(prefix) + " (" + getLocation().toString() + ")" +
                (NumChildren() == 0 ? " (" + beginToken.image + ")" : "");
    }

    @Override
    public AstNode visitLogicalBinary(com.facebook.coresql.parser.SqlParser context)
    {
        LogicalBinaryExpression.Operator operator = getLogicalBinaryOperator(context.operator);
        boolean warningForMixedAndOr = false;
        Expression left = (Expression) visit(context.left);
        Expression right = (Expression) visit(context.right);

        if (operator.equals(LogicalBinaryExpression.Operator.OR) &&
                (mixedAndOrOperatorParenthesisCheck(right, context.right, LogicalBinaryExpression.Operator.AND) ||
                        mixedAndOrOperatorParenthesisCheck(left, context.left, LogicalBinaryExpression.Operator.AND))) {
            warningForMixedAndOr = true;
        }

        if (operator.equals(LogicalBinaryExpression.Operator.AND) &&
                (mixedAndOrOperatorParenthesisCheck(right, context.right, LogicalBinaryExpression.Operator.OR) ||
                        mixedAndOrOperatorParenthesisCheck(left, context.left, LogicalBinaryExpression.Operator.OR))) {
            warningForMixedAndOr = true;
        }

        if (warningForMixedAndOr) {
            warningConsumer.accept(new ParsingWarning(
                    "The Query contains OR and AND operator without proper parenthesis. "
                            + "Make sure the operators are guarded by parenthesis in order "
                            + "to fetch logically correct results",
                    context.getStart().getLine(), context.getStart().getCharPositionInLine()));
        }

        return new LogicalBinaryExpression(
                getLocation(context.operator),
                getLogicalBinaryOperator(context.operator),
                (Expression) visit(context.left),
                (Expression) visit(context.right));
        operator,
                left,
                right);
    }

    private boolean mixedAndOrOperatorParenthesisCheck(Expression expression, SqlBaseParser.BooleanExpressionContext node, LogicalBinaryExpression.Operator operator)
    {
        if (expression instanceof LogicalBinaryExpression) {
            if (((LogicalBinaryExpression) expression).getOperator().equals(operator)) {
                if (node.children.get(0) instanceof SqlBaseParser.ValueExpressionDefaultContext) {
                    return !(((SqlBaseParser.PredicatedContext) node).valueExpression().getChild(0) instanceof
                            SqlBaseParser.ParenthesizedExpressionContext);
                }
                else {
                    return true;
                }
            }
        }
        return false;
    }
}
