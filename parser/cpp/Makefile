
CC = clag
CXX = clang++
WORKING_DIR=$(PWD)
GENDIR=$(WORKING_DIR)/target/generated-sources/javacc
GENERATED_FILES = $(GENDIR)/*.cc
GENERATED_OBJS = $(GENDIR)/*.o

override CXXFLAGS += -fbracket-depth=1024 -funsigned-char -I$(WORKING_DIR) -I$(GENDIR) -std=c++11

$(WORKING_DIR)/target/parser_test: main.cc ParseErrorHandler.cc main.cc ParserClient.cc $(GENERATED_OBJS)
	$(CXX) $(OPT) $(CXXFLAGS) main.cc ParseErrorHandler.cc ParserClient.cc $(GENERATED_OBJS) -o $(WORKING_DIR)/target/parser_test 

$(GENERATED_OBJS): ../grammar/sql-spec.txt
	mvn compile
	cd $(GENDIR) && $(CXX) $(OPT) $(CXXFLAGS) -Wno-dangling-else -c *.cc

clean:
	/bin/rm -f target/*.*

real_clean:
	/bin/rm -rf target
