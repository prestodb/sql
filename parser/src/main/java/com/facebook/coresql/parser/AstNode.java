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

import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTADDITIVEEXPRESSION;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTANDEXPRESSION;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTBETWEEN;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTBUILTINFUNCTIONCALL;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTCOMPARISON;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTFUNCTIONCALL;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTINPREDICATE;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTISNULL;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTLIKE;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTMULTIPLICATIVEEXPRESSION;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTNOTEXPRESSION;
import static com.facebook.coresql.parser.SqlParserTreeConstants.JJTOREXPRESSION;

public class AstNode
        extends com.facebook.coresql.parser.SimpleNode
{
    public Token beginToken;
    public Token endToken;
    private int operator;
    private boolean negated;

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

    // Other utility methods.

    public boolean IsNegatableOperator()
    {
        switch (Kind()) {
            case JJTBETWEEN:
            case JJTLIKE:
            case JJTINPREDICATE:
            case JJTISNULL:
                return true;
            default:
                return false;
        }
    }

    public boolean IsOperator()
    {
        switch (Kind()) {
            case JJTADDITIVEEXPRESSION:
            case JJTMULTIPLICATIVEEXPRESSION:
            case JJTOREXPRESSION:
            case JJTANDEXPRESSION:
            case JJTNOTEXPRESSION:
            case JJTCOMPARISON:
                // Other binary negatable operators
                return true;
            default:
                return false;
        }
    }

    public void SetOperator(int operator)
    {
        this.operator = operator;
    }

    public int GetOperator()
    {
        if (!IsOperator()) {
            throw new IllegalArgumentException("GetOperator cannot be called on: " + this);
        }

        return operator;
    }

    public String GetFunctionName()
    {
        if (Kind() == JJTFUNCTIONCALL) {
            return GetChild(0).GetImage();
        }

        if (Kind() == JJTBUILTINFUNCTIONCALL) {
            return beginToken.image;
        }

        return null;
    }

    public void SetNegated(boolean negated)
    {
        this.negated = negated;
    }

    public boolean IsNegated()
    {
        return negated;
    }
}
