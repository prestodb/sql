#include "parser.h"
#include "ErrorHandler.h"
#include "Token.h"
#include "SqlParserConstants.h"
#include "SqlParser.h"

#include <iostream>

using std::cerr;
using std::cout;

namespace commonsql {
namespace parser {

  inline bool isKeyword(Token* t) {
    return t->kind >= MIN_RESERVED_WORD and t->kind <= MAX_RESERVED_WORD;
  }

  // Since we have a lookahead of 3, check the next 2 tokens to see if someone is usign a keyword as identifier.
  inline Token* getPossibleKeyword(SqlParser* parser) {
    if (isKeyword(parser->getToken(1))) return parser->getToken(1);
    if (isKeyword(parser->getToken(2))) return parser->getToken(2);
    return nullptr;
  }

  void ParseErrorHandler::handleUnexpectedToken(int expectedKind, const JJString& expectedToken, Token* actual, SqlParser* parser) {
    Token* possibleKeyword = getPossibleKeyword(parser);
    if (possibleKeyword == nullptr) {
      cerr << actual->beginLine
           << ":"
           << actual->beginColumn
           << " Unexpected token: \""
           << (*actual).image << "\" after: \""
           << parser->getToken(0)->image
           << "\". Expecting: "
           << expectedKind
           << "("
           << (expectedKind <= 0 ? "EOF" : tokenImage[expectedKind])
           << ")\n";
    } else {
      cerr << actual->beginLine << ":" << actual->beginColumn << " Unexpected keyword: \"" << (*possibleKeyword).image << "\"" << "\n";
    }
  }

  void ParseErrorHandler::handleParseError(Token* last, Token* unexpected, const JJSimpleString& production, SqlParser* parser) {
    cerr << last->beginLine << ":" << last->beginColumn << " Unexpected token: " << (*unexpected).image << " after: " << (*last).image << " while parsing: " << production << "\n";
  }

  void ParseErrorHandler::handleOtherError(const JJString& message, SqlParser* parser) {
    cerr << "Error: %s\n" << message.c_str() << "\n";
  };

}
}
