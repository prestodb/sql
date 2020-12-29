#ifndef __PARSER_H_
#define __PARSER_H_

#include <iostream>
#include <deque>

#include "ErrorHandler.h"
#include "Token.h"
#include "SqlParserConstants.h"
#include "SqlParserTreeConstants.h"
#include "SqlParser.h"

using std::cerr;
using std::cout;
using std::deque;

namespace commonsql {
namespace parser {

class ParseErrorHandler: public ErrorHandler {
  public:
    virtual void handleUnexpectedToken(int expectedKind, const JJString& expectedToken, Token* actual, SqlParser* parser);
    virtual void handleParseError(Token* last, Token* unexpected, const JJSimpleString& production, SqlParser* parser);
    virtual void handleOtherError(const JJString& message, SqlParser* parser);
};

}
}

#endif
