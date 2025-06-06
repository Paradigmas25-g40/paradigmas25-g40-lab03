JAVAC=javac
JAVA=java
LIB_DIR=lib
OUT_DIR=out

SPARK_FOLDER=/home/santi/Descargas/spark-4.0.0-bin-hadoop3

# Librerías extra a incluir
CLASSPATH=$(OUT_DIR):$(LIB_DIR)/json-20231013.jar:$(SPARK_FOLDER)/jars/*

SOURCES=$(shell find src -name "*.java")

all: build run

build:
	$(JAVA_PATH)$(JAVAC) -cp $(CLASSPATH) -d $(OUT_DIR) $(SOURCES)

run:
	$(JAVA_PATH)$(JAVA) -cp $(CLASSPATH) SparkFeedFetcher

clean:
	rm -rf $(OUT_DIR) 