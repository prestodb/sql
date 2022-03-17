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
import static com.facebook.coresql.parser.SqlParserConstants.tokenImage;

public class Unparser
        extends com.facebook.coresql.parser.SqlParserDefaultVisitor
{
    protected StringBuilder stringBuilder = new StringBuilder();
    private Token lastToken = new Token();

    public static String unparseClean(AstNode node, Unparser unparser)
    {
        unparser.stringBuilder.setLength(0);
        unparser.lastToken.next = node.beginToken;
        node.jjtAccept(unparser, null);
        unparser.printTrailingComments();
        return unparser.stringBuilder.toString();
    }

    public static String unparseClean(AstNode node)
    {
        return unparseClean(node, new Unparser());
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

    public final void printKeyword(int keyword)
    {
        printToken(tokenImage[keyword].substring(1, tokenImage[keyword].length() - 1));
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

    @Override
    public void visit(CompilationUnit node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(UnaryExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(UnsignedNumericLiteral node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(CharStringLiteral node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(DateLiteral node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(TimeLiteral node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(TimestampLiteral node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(IntervalLiteral node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(BooleanLiteral node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Unsupported node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Identifier node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(TableName node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(SchemaName node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(CatalogName node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(SchemaQualifiedName node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(ArrayType node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(FieldDefinition node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(ParenthesizedExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(BuiltinValue node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(NullLiteral node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(ArrayLiteral node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(QualifiedName node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(GroupingOperation node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(WindowFunction node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(NullTreatment node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(NullIf node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Coalesce node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(CaseExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(WhenClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(ElseClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(WhenOperand node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(SearchedCaseOperand node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(CastEpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(FieldReference node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(AggregationFunction node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(FunctionCall node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Unused node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Lambda node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(ArrayElement node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Add node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Subtract node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Multiply node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Divide node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Mod node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(TimeZoneField node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Concatenation node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(AdditiveEpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(MultiplicativeExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(OrExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(AndExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(NotExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(IsExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(RowExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(RowExression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Values node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(TableExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(FromClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(CommaJoin node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Join node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(TableSample node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(AliasedTable node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Alias node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Unnest node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(ColumnNames node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(OnClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(UsingClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(WhereClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(GroupbyClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Rollup node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Cube node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(GroupingSets node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(HavingClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(PartitionByClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(OrderByClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(WindowFrameUnits node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(WindowFrameExtent node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(UnboundedPreceding node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(CurrentRow node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(WindowFramePreceding node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(WindowFrameBetween node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(UnboundedFollowing node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(WindowFrameFollowing node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Select node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(SelectList node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(SelectItem node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Star node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(QuerySpecification node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(WithClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Cte node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(SetOperation node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Subquery node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Comparison node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Between node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(InPredicate node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(InvalueList node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Like node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(IsNull node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(QuantifiedComparison node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Exists node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(IsDistinct node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(InvervalQualifier node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(NonSecondField node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(SecondField node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(NonSecondDateTimeField node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(LanguageClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(ArgumentList node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(NamedArgument node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(SetQuantifier node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(FilterClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(SortSpecificationList node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(SortSpecification node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(OrderingDirection node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(NullOrdering node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(CreateSchema node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(Unsuppoerted node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(UseStatement node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(LambdaBody node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(LambdaParams node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(LimitClause node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(MapType node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(TryExpression node, Void data)
    {
        defaultVisit(node, data);
    }

    @Override
    public void visit(CastExpression node, Void data)
    {
        defaultVisit(node, data);
    }
}
