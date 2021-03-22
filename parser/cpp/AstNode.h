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
 public:
  Token* beginToken;
  Token* endToken;
  virtual ~AstNode() {}
  AstNode(int id) : SimpleNode(id) {}
  
  AstNode(SqlParser* parser, int id) : SimpleNode(parser, id) {}

  int kind() const { return id; }

  int numChildren() const { return jjtGetNumChildren(); }
  
  AstNode* getChild(int i) const { return static_cast<AstNode*>(jjtGetChild(i)); }
  
  std::string getImage() const { return numChildren() == 0 ? beginToken->image : string(""); }

  void VisitNode(SqlParserVisitor* visitor) const { jjtAccept(visitor, nullptr); }

  void VisitChildren(SqlParserVisitor* visitor) const { jjtChildrenAccept(visitor, nullptr); }

  const AstNode* firstChild() const { return numChildren() > 0 ? getChild(0) : nullptr; }

  const AstNode* lastChild() const { return numChildren() > 0 ? getChild(numChildren() - 1) : nullptr; }

  const AstNode* getFirstChildOfKind(int kind) const {
    for (int i = 0; i < numChildren(); i++) {
      if (getChild(i)->kind() == kind) {
        return getChild(i);
      }
    }

    return nullptr;
  }

  std::string toString(const JJString& prefix) const {
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
};

}
}

#endif
