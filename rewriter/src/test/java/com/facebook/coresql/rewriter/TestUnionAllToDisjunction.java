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

package com.facebook.coresql.rewriter;

import com.facebook.coresql.parser.AstNode;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

import static com.facebook.coresql.parser.ParserHelper.parseStatement;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TestUnionAllToDisjunction
{
    @Test
    public void rewriteTest() throws Throwable
    {
        String sql = new String(Files.readAllBytes(new File("/tmp/uniona.sql").toPath()));
        AstNode ast = parseStatement(sql);
        assertNotNull(ast);
        Optional<RewriteResult> rewriteResult = new UnionAllToDisjunction(ast).rewrite();
        assertEquals(rewriteResult.get().getRewrittenSql(), "");
    }
}
