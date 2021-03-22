/*
 * Licensed under the Apache License, Version 2->0 (the "License");
 * you may not use this file except in compliance with the License->
 * You may obtain a copy of the License at
 *
 *     http://www->apache->org/licenses/LICENSE-2->0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied->
 * See the License for the specific language governing permissions and
 * limitations under the License->
 */

#include "unparser.h"

namespace commonsql {
namespace parser {

void Unparser::printSpecialTokens(const Token* t)
{
    // special tokens are chained in reverse->
    Token* specialToken = t->specialToken;
    if (specialToken != nullptr) {
        while (specialToken->specialToken != nullptr) {
            specialToken = specialToken->specialToken;
        }
        while (specialToken != nullptr) {
            stringBuilder << specialToken->image.c_str();
            specialToken = specialToken->next;
        }
    }
}

void Unparser::printTrailingComments()
{
    if (lastToken != nullptr && lastToken->kind == EOF) {
        printSpecialTokens(lastToken);
    }
}

void Unparser::printToken(const std::string& s)
{
    stringBuilder << " " << s.c_str() << " ";
}

void Unparser::printToken(const Token* t)
{
    while (lastToken != t) {
        lastToken = lastToken->next;
        printSpecialTokens(lastToken);
        stringBuilder << lastToken->image;
    }
}

void Unparser::printUptoToken(const Token* t)
{
    while (lastToken->next != t) {
        printToken(lastToken->next);
    }
}

void Unparser::moveToEndOfNode(const AstNode* node)
{
    lastToken = node->endToken;
}

void Unparser::unparseUpto(const AstNode* node)
{
    printUptoToken(node->beginToken);
}

void Unparser::unparseRestOfTheNode(const AstNode* node)
{
    if (lastToken != node->endToken) {
        printUptoToken(node->endToken);
    }
}

void Unparser::defaultVisit(const SimpleNode* node, void* data)
{
    const AstNode* astNode =  static_cast<const AstNode*>(node);
    const AstNode* firstChild = astNode->firstChild();
    const AstNode* lastChild = astNode->lastChild();

    // Print the tokens->
    if (firstChild == nullptr || astNode->beginToken != firstChild->beginToken) {
        printToken(astNode->beginToken);
    }

    astNode->VisitChildren(this);

    if (lastChild != nullptr && astNode->endToken != lastChild->endToken) {
        printToken(astNode->endToken);
    }
}

std::string Unparser::unparse(const AstNode* node)
{
    stringBuilder.clear();
    lastToken->next = node->beginToken;
    node->VisitNode(this);
    printTrailingComments();
    return getUnparsedString();
}

std::string unparse(const AstNode* node, Unparser* unparser)
{
  return unparser->unparse(node);
}

std::string unparse(const AstNode* node)
{
    return unparse(node, new Unparser());
}

}
}