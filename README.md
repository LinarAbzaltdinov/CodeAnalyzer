# CodeAnalyzer
Simple static code analyzer for searching NullPointerExceptions. 

It searches vars that not initialized / null / referenced to other null variables, and checks invocations on this null-vars or passsings them to other methods (so search is recoursive).

## Usage
1. Install [Maven](https://maven.apache.org/install.html)
2. Run
    ```
    git clone https://github.com/LinarAbzaltdinov/CodeAnalyzer.git
    cd CodeAnalyzer/
    mvn clean install
    mvn exec:java -Dexec.mainClass=ru.abzaltdinov.Main -Dexec.args="Test.java"
    ``` 
    where specify YOUR source .java file or pom.xml of Maven project instead of "Test.java".
3. Check "AnalyzeResults.txt"

## Exmaple
Example of output for [Test.java](Test.java):
```
Expected NullPointerExceptions(with stack trace):

MainClass.solveD(in, out)
(/Users/linarkou/Documents/CodeAnalyzer/Test.java:11), columns 9-24
out.printf("%.3f %.3f", min, max)
(/Users/linarkou/Documents/CodeAnalyzer/Test.java:41), columns 9-42
out can be NULL

out.close()
(/Users/linarkou/Documents/CodeAnalyzer/Test.java:16), columns 9-20
out can be NULL

c.toString()
(/Users/linarkou/Documents/CodeAnalyzer/Test.java:18), columns 9-21
c can be NULL
```
or in [AnalyzeResults.txt](analyzeResults.txt)
