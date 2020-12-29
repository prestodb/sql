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
  int NumChildren() { return jjtGetNumChildren(); }
  AstNode* GetChild(int i) { return static_cast<AstNode*>(jjtGetChild(i)); }
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
};

}
}

#endif
