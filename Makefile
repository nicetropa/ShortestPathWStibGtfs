JAVAC = javac
JAVA = java
SRC_DIR = src
BIN_DIR = bin
LIB_DIR = lib
MAIN_CLASS = App

SOURCES = $(wildcard $(SRC_DIR)/*.java)

CLASSPATH = $(LIB_DIR)/*:$(BIN_DIR)

all: $(BIN_DIR) compile

compile:
	@echo "Compilation..."
	$(JAVAC) -cp "$(CLASSPATH)" -d $(BIN_DIR) $(SOURCES)

run: all
	@echo "Exécution..."
	$(JAVA) -cp "$(CLASSPATH)" $(MAIN_CLASS)

clean:
	@echo "Nettoyage..."
	rm -rf $(BIN_DIR)

$(BIN_DIR):
	mkdir -p $(BIN_DIR)
