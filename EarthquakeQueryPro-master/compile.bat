@echo off

javac -d bin -cp lib\*.jar:res:bin src\*.java
copy /y res\* bin\
