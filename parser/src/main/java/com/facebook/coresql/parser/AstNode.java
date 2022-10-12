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

    public String GetSqlString()
    {
        return Unparser.unparseClean(this);
    }
}
