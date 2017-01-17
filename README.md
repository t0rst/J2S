## J2S — Yet another Java to Swift converter (Java 8, Swift 3) ![GitHub license](https://img.shields.io/badge/license-MIT-lightgrey.svg)
Torsten Louland, Satisfying Structures BVBA



1.  [Aims](#aims)
1.  [Install](#install)
1.  [Build](#build)
1.  [Use](#use)
1.  [Workflow](#workflow)
1.  [To Do](#to-do)



### Aims
J2S aims to do most of the syntax conversion work for you. It is far from perfect and will never understand the code in the way that a compiler can. Instead, it gets you over the initial conversion hurdle, from which point you use a mixture of manual and automated conversion, as suggested in [Workflow](#workflow).



### Install
You need:

-   Java SE 8 ([download](http://www.oracle.com/technetwork/java/javase/downloads/index.html))

-   [ANTLR](https://github.com/antlr/antlr4/blob/master/README.md) 4.6 (Easiest using [homebrew](http://brew.sh)... `$ brew update ; brew install antlr` or `$ brew upgrade antlr`)



### Build
You can build this project with the [IntelliJ](https://www.jetbrains.com/idea/#chooseYourEdition) IDE (Community or Ultimate) but not Android Studio (project file incompatibilities); or other Java dev IDEs.

If you use IntelliJ, then when you open the J2S project

1.  Go to File > Project Structure > Libraries, click on `+`, choose java library, and paste in the path to the antlr jar from the CLASSPATH in `$ cat $(which antlr4)` (dont include trailing `:.`) if you installed with homebrew, otherwise where you installed it. Then check that you can build J2S.

1.  (optional) To run from within IntelliJ, go to Run > Edit Configurations…, click on plus, select an Application configuration, set Main class to com.satisfyingstructures.J2S.J2S and add `-a path-to-your-arguments-file` (described in [use](#use) below) through which you can specify input, output and options.

1.  (optional) There also is a useful ANTLR v4 plugin available to download via Preferences > Plugins.

Or you can build from the command line:

1.  Get `antlr` to regenerate the basic Java8 parser, visitor and listener source. Skip this step first time round and come back to it if [using](#use) J2S fails with a version mismatch between the antlr runtime and source pulled from github (currently generated by ANTLR 4.5.3). 

    ```sh
    $ cd directory-containing-this
    $ antlr4 -o src/com/satisfyingstructures/J2S/antlr -package com.satisfyingstructures.J2S.antlr -Dlanguage=Java -listener -visitor Java8.g4
    ```

1.  Compile J2S

    ```sh
    cd directory-containing-this
    mkdir -p out/production/J2S/com/satisfyingstructures/J2S/antlr
    javac \
        -cp "/usr/local/Cellar/antlr/4.6/antlr-4.6-complete.jar:." \
        -d out/production/J2S \
        src/com/satisfyingstructures/J2S/*.java \
        src/com/satisfyingstructures/J2S/antlr/*.java
    ```



### Use
Use the `J2S.sh` wrapper script in this directory to invoke the J2S tool. Options are as follows:

    J2S [-i path] [-o path] [-w path] [-a path] [-f] [-Dkey=value | -Dkey]* [path]

| option | notes |
| ------ | ----------- |
| -i input-file-path | if this option not used, then take input from stdin |
| -o output-file-path | enclosing directory must exist; if this option not used, then send output to stdout |
| -f | force overwrite of existing file (the default is not to overwrite) |
| -w working-directory | input and output paths subsequent to this argument, can be specified relative to this existing directory |
| -a arguments-file-path | insert arguments from this file into the argument list as if inserted at this position on the command line; one argument per line; lines are whitespace trimmed; empty lines are ignored; hash-suffix to line-end is ignored. |
| -Dkey[=value] | define a value for a key that the converter will use; absent value ==> value=1 |
| -Mtype=replacement | map 'type' to 'replacement' in generated Swift output |
| -h \| -help | show this usage help |

(Note: J2S is currently not always writing output with native line breaks on macOS, hence pipe output through `tr -d '\r'` to strip CR from CRLF line breaks. On the fix list.)

Additional options offered by J2S.sh, which must appear before any pass through options: `--timed` to give timing, and `--javaxxxx` to pass xxxx as a parameter to java.

An arguments file is useful with `-a` for passing mappings that are frequently used, e.g.
```
-MFloat=CGFloat
-MPointF=CGPoint
-MRectF=CGRect
```



### Workflow
A possible workflow, assuming you are using git, use three branches: `JavaAsSwift`, `J2S` and `dev`.

-   `JavaAsSwift` contains the java files with extensions replaced by .swift and organised in the same structure that you will have for your ported Swift files; it is present to allow you to look at your converted source side-by-side with the original java; every time your java source changes, add the changes as a commit on this branch.

-   In your `J2S` branch only commit successive stages of source files generated by J2S, which will happen whenever your java source changes or J2S is improved.

-   Your `dev` branch will intersect the root of your `J2S` branch and will thereafter diverge as you make and commit manual changes. Whenever you commit a new change onto your `J2S` branch, cherry-pick onto the end of your `dev` branch.

To view your current source side-by-side with the original java, stash or commit your current changes, use a temp branch: `git checkout -b temp`, reset `git reset JavaAsSwift` so the workspace still contains your current space, but the index contains the `JavaAsSwift` state, and then show the comparison view in the Xcode version editor; finish by checking out your original branch again.



### To Do
Many things. This is a work in progress.

<!-- ### How It Works -->
<!-- ### Contributing -->
