#ifndef __UNPARSE_H__
#define __UNPARSE_H__

#include <sstream>
#include "AstNode.h"
#include "SqlParserVisitor.h"

namespace commonsql {
namespace parser {

using std::string;
using std::stringstream;

class Unparser : public SqlParserDefaultVisitor {
  private:
    Token* lastToken = new Token();

    virtual void printSpecialTokens(const Token* t);
    virtual void printTrailingComments();

  protected:
    std::stringstream stringBuilder;

    virtual void printToken(const std::string& s);
    virtual void printToken(const Token* t);
    virtual void printUptoToken(const Token* t);
    virtual void moveToEndOfNode(const AstNode* node);
    virtual void unparseUpto(const AstNode* node);
    virtual void unparseRestOfTheNode(const AstNode* node);
    virtual const string getUnparsedString() const { return stringBuilder.str(); }

  public:
    string unparse(const AstNode* node);
    void defaultVisit(const SimpleNode* node, void* data);
    virtual ~Unparser() {}
};

std::string unparse(const AstNode* node, Unparser* unparser);
std::string unparse(const AstNode* node);

}
}

#endif
