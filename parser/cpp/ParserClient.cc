
#include "ParserClient.h"

#include "SqlParserConstants.h"
#include "SqlParserVisitor.h"

using namespace commonsql::parser;
using namespace std;

static const string GetIdSequence(Token* start, Token* end) {
  string id_sequence;
  for (; start != nullptr && start != end; start = start->next) {
    id_sequence += start->image + ".";
  }

  id_sequence += start->image;
  return id_sequence;
}

class QuietErrorHandler : public ErrorHandler {
  public:
    void handleUnexpectedToken(int expectedKind, const JJString& expectedToken, Token* actual, SqlParser* parser) {}
    void handleParseError(Token* last, Token* unexpected, const JJSimpleString& production, SqlParser* parser) {}
    void handleOtherError(const JJString& message, SqlParser* parser) {}
};

class TableNameVisitor : SqlParserDefaultVisitor {

  vector<string> tableNames;

 public:
  virtual void  defaultVisit(const SimpleNode* node, void*  data)  {
    const AstNode* astNode = static_cast<const AstNode*>(node);
    for (int i = 0; i < astNode->NumChildren(); i++) {
      astNode->GetChild(i)->jjtAccept(this, data);
    }
  }

  virtual void  visit(const TableName* node, void*  data) {
    const AstNode* nameNode = node->GetChild(0);
    tableNames.push_back(GetIdSequence(nameNode->beginToken, nameNode->endToken));
  }

  vector<string> GetTableNames() { 
    return tableNames;
  }
};

vector<string> GetTableNamesApprox(const string& sql) {
  vector<string> tableNames;

  CharStream stream(sql.c_str(), sql.size() - 1, 1, 1);
  SqlParserTokenManager scanner(&stream);
  SqlParser parser(&scanner);
  parser.setErrorHandler(new QuietErrorHandler());

  TableNameVisitor tableNameVisitor;
  Token* start = parser.getToken(1);
  while (start != null) {
    while (start != null && start->kind != FROM && start->kind != _EOF) {
      start = parser.getNextToken();
    }

    if (start == null || start->kind == _EOF) break; 

    // Now try to parse a from clause
    parser.from_clause();
    AstNode* fromClause = static_cast<AstNode*>(parser.PopNode());
    if (fromClause != nullptr) {
      tableNameVisitor.defaultVisit(fromClause, nullptr);
    }

    delete fromClause;
    parser.Reset();
    start = parser.getToken(1);
  }

  //delete scanner;
  //delete stream;
  return tableNameVisitor.GetTableNames();
}

const vector<string> GetTableNames(string sql) {
  // Save the start token
  return GetTableNamesApprox(sql);

#if 0
    parser.compilation_unit();

    SimpleNode* root = (SimpleNode*)parser.jjtree.peekNode();
    if (root != nullptr && static_cast<AstNode*>(root)->NumChildren() > 0) {
      JAVACC_STRING_TYPE buffer;
      root->dumpToBuffer(" ", "\n", &buffer);
      printf("%s\n", buffer.c_str());
      return GetTableNames(static_cast<AstNode*>(root));
    } else {
printf("lexing***");
      // Just read off all tokens from the parser.
      while (parser.getNextToken()->kind != _EOF) ;
      return GetTableNamesApprox(firstToken);
    }

    return d;
#endif
}
