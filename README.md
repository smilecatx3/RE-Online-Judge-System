# RE: Online Judge System  

The system provides an online service for programming examination.  

**Features**:

- The source code to be submitted can be a plain text (i.e. the text in the code editor) or a zip
  file. For those problems that require more than one class (eg. a class design problem) are
  suitable for the second submission type. Also, for those users who don't want to put all the code
  in a single file can choose the second submission type.
- The problem to be shown is a PDF file rather than plain text, and hence it can be easily designed
  prettier such as by providing descriptive pictures about the problem.

**Supported Languages**:
The current version only supports Java programming language.

## Prerequisites

- [JDK 10+](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
- Web container: [Tomcat](http://tomcat.apache.org/)
- Builder: [SCons](https://scons.org/)
- Java library dependencies:
  - [Apache Commons Configuration](http://commons.apache.org/proper/commons-configuration/)
  - [Apache Commons FileUpload](http://commons.apache.org/proper/commons-fileupload/)
  - [Apache Commons Lang](http://commons.apache.org/proper/commons-lang/)
  - [Apache Commons Logging](http://commons.apache.org/proper/commons-logging/)
  - [JavaParser](https://javaparser.org/)
  - [JSON-java](https://github.com/douglascrockford/JSON-java)
  - [Zip4j](http://www.lingala.net/zip4j/)

You can get prebuilt libs [here](https://drive.google.com/drive/folders/130ug9bok-Xy2CJjNWvBENqonS-o4FiQi?usp=sharing).

## Build and Installation

### [Build the Judge System]

The project uses [SCons](https://scons.org/) as software builder. You need to specify the Java
compiler and the path to the directory containing the required libs in the config file *config.ini*.
Use the command `scons` to build the project, and the final product is a war file. Put the war file
to the *Tomcat/webapps* directory.

Configure the system using the config file located in *WEB-INF/config.json*.

- **system**: the system-wide settings.
  - **testset_dir**: the path to the test set directory.
  - **timeout**: the general (default) timeout used in the system for those time-consuming tasks that
    not specify the timeout.
  - **base_score**: the *lowest* score for a judgement in the range between 0 and 100.
- **java**: the settings for Java source code.
  - **jdk_path**: the path to the JDK tools (bin) directory.
- **scons**: the settings for the SCons builder.
  - **python**: the path to the Python interpreter.
  - **scons**: the path to the SCons main script.
  - **timeout**: the timeout for a build process.

### [Online Judge Website]

You may need to prepare problem files (of pdf) and test set files (of json). By default the problem
files are put under the *data/problems* directory and the website depends this path to load and show
the problems. You can change the path by modifying `problemFilesDir` in *scripts/index.js*. Also, the
website provides a feature that shows the raw test file, and by default the files are put under
*data/problems*. You can change the path by modifying `testsetFilesDir` in *scripts/judge.js*.

The website provides a code template used in the code editor for each programming language. By
default the templates are put under *data/templates*. You can change the path to these templates by
modifying `templateFilesTable` in *scripts/index.js*.

Besides the essential image files, other ones (eg. favicon) used in the website are not provided.
Put those images inside the *assets* directory if needed.

To set the content of the dropdown list of problem ids and programming languages supported by the
system, modify the *data/options.json* file.

## Misc

Refer to the [wiki](https://github.com/smilecatx3/RE-Online-Judge-System/wiki) page for more information.

- [Test Set Format](https://github.com/smilecatx3/RE-Online-Judge-System/wiki/Test-Set-Format)

## Licensing

The project is distributed under the MIT license.
