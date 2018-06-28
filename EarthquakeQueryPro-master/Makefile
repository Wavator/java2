DIR_BIN = ./bin
DIR_SRC = ./src
DIR_RES = ./res

CLS = Main Latitude Longitude Quake Spider DataModel Controller ProjErr
RES = $(wildcard ${DIR_RES}/*)

CLS_TGT = $(patsubst %, ${DIR_BIN}/%.class, $(CLS))
RES_TGT = $(patsubst ${DIR_RES}/%, ${DIR_BIN}/%, $(RES))

JAVAOPT = -classpath lib/*:${DIR_SRC}:${DIR_BIN}

all: ${CLS_TGT} ${RES_TGT} ${DIR_BIN}/${DIR_RES}

clean:
	-rm -rf $(DIR_BIN)/*

run: all
	@cd $(DIR_BIN) && java -cp ../lib/*: Main

${DIR_BIN}/%.class: ${DIR_SRC}/%.java
	javac -d ${DIR_BIN} ${JAVAOPT} $<

${DIR_BIN}/%: ${DIR_RES}/%
	@ln -sf ../$< $@

${DIR_BIN}/${DIR_RES}: ${DIR_RES}
	@ln -sf ../$< $@

.PHONY: run clean
