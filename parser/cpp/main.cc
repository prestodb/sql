#include <iomanip>
#include <iostream>
#include <string>
#include <stdio.h>
#include <stdlib.h>

#include "SqlParserConstants.h"
#include "CharStream.h"
#include "CharStream.h"
#include "Token.h"
#include "SqlParser.h"
#include "SqlParserTokenManager.h"
#include "parser.h"

#include <ctime>

using namespace commonsql::parser;
using namespace std;

static constexpr int kBufferSize = 128;

JAVACC_STRING_TYPE ReadFileFully(char *file_name) {
  JAVACC_STRING_TYPE s;
  // fstream includes math.h and conflicts with the DOMAIN keyword on MacOS.
  // See: https://github.com/prestodb/sql/pull/45
  // Use C style file reading to avoid this conflict.
  FILE* fp = fopen(file_name, "r");
  assert(fp != NULL);
  char buffer[kBufferSize];
  // Very inefficient.
  while (!fgets(buffer, kBufferSize, fp )) {
   s.append(buffer, kBufferSize);
  }
  s.append(buffer, strlen(buffer));
  return s;
}

int main(int argc, char **argv) {
  if (argc < 2) {
    cout << "Usage: sqlparser <query file>" << endl;
    exit(1);
  }
  JAVACC_STRING_TYPE s = ReadFileFully(argv[1]);

  clock_t start,finish;
  double time;
  start = clock();

  for (int i = 0; i < 1; i++) {
    CharStream *stream = new CharStream(s.c_str(), s.size() - 1, 1, 1);
    SqlParserTokenManager *scanner = new SqlParserTokenManager(stream);
    SqlParser parser(scanner);
    parser.setErrorHandler(new ParseErrorHandler());
    parser.compilation_unit();
    SimpleNode *root = (SimpleNode*)parser.jjtree.peekNode();
    if (root) {
      JAVACC_STRING_TYPE buffer;
      root->dumpToBuffer(" ", "\n", &buffer);
      printf("%s\n", buffer.c_str());
    }
  }

  finish = clock();
  time = (double(finish)-double(start))/CLOCKS_PER_SEC;
  printf ("Avg parsing time: %lfms\n", (time*1000)/1);
}
