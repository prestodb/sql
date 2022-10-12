#ifndef _ASTNODE_H_
#define _ASTNODE_H_

#include <string>

#include "Token.h"
#include "Node.h"
#include "SimpleNode.h"

using std::string;

namespace commonsql {
namespace parser {

class AstNode;

class AstNode : public SimpleNode {
 private:
  int op;
  bool negated;

 public:
  Token* beginToken;
  Token* endToken;
  virtual ~AstNode() {}
  AstNode(int id) : SimpleNode(id) {}
  AstNode(SqlParser* parser, int id) : SimpleNode(parser, id) {}
  int NumChildren() const { return jjtGetNumChildren(); }
  AstNode* GetChild(int i) const { return static_cast<AstNode*>(jjtGetChild(i)); }
  int Kind() const { return id; }
  JJString GetImage() const { return NumChildren() == 0 ? beginToken->image : ""; }
  JJString toString(const JJString& prefix) const {
    return SimpleNode::toString(prefix) +
           string(" (") +
           std::to_string(beginToken->beginLine) +
           string(":") +
           std::to_string(beginToken->beginColumn) +
           string(" - ") +
           std::to_string(endToken->endLine) +
           string(":") +
           std::to_string(endToken->endColumn) +
           string(")");
  }

    // Other utility functions.
    bool IsNegatableOperator()
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

    bool IsOperator() const
    {
        switch (Kind()) {
            case JJTADDITIVEEXPRESSION:
            case JJTMULTIPLICATIVEEXPRESSION:
            case JJTOREXPRESSION:
            case JJTANDEXPRESSION:
            case JJTNOTEXPRESSION:
            case JJTCOMPARISON:
                return true;
            default:
                return false;
        }
    }

    void SetOperator(int op)
    {
        this->op = op;
    }

    int GetOperator() const
    {
        assert(IsOperator());
        return op;
    }

    JJString GetFunctionName() const
    {
        if (Kind() == JJTFUNCTIONCALL) {
            return GetChild(0)->GetImage();
        }

        if (Kind() == JJTBUILTINFUNCTIONCALL) {
            return beginToken->image;
        }

        return "";
    }

    void SetNegated(bool negated) { this->negated = negated; }
    bool IsNegated() const { return negated; }
};

}
}

#endif
