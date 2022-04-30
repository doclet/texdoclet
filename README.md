> **WARNING**: TeXDoclet is only working with Java versions up to Java 11 because of the removal of the old doclet API (com.sun.javadoc). A migration to the new Java Doclet API is curently not planned.

TeXDoclet
=========

TeXDoclet is a Java doclet implementation that generates a LaTeX file from you Java code documentation.

Build
-----

    mvn clean install

Run
---

### From command line

You have to specify that *javadoc* has to use another doclet than the default doclet by giving the following options to the *javadoc* call :

	javadoc -docletpath <path to>TeXDoclet.jar \
		-doclet org.stfm.texdoclet.TeXDoclet \
		<TeXDoclet parameters>

Example :

	javadoc -docletpath target/TeXDoclet.jar \
		-doclet org.stfm.texdoclet.TeXDoclet \
		-noindex \
		-tree \
		-hyperref \
		-texinit texdoclet_include/preamble.tex \
		-imagespath ".." \
		-output texdoclet_output/TeXDoclet.tex \
		-title "TeXDoclet Java Documentation" \
		-author "Greg Wonderly \and S\"oren Caspersen \and Stefan Marx" \
		-sourcepath src/main/java \
		-subpackages org.stfm.texdoclet

The most important is that you specify the Java packages (option `-subpackage`) and their location in the file system (option `-sourcepath`) correctly, otherwise no output will be generated (specifying `-sourcepath` alone is not sufficient).

See `createDocs.sh` scripts in `/examples` subdirectory for more examples.

Print help (TeXDoclet + javadoc help) :

	javadoc -docletpath target/TeXDoclet.jar -doclet org.stfm.texdoclet.TeXDoclet

or (TeXDoclet help only) :

	java -jar target/TeXDoclet.jar -h


### Maven integration

Alternatively you can use *TeXDoclet* as an [alternate doclet](http://maven.apache.org/plugins/maven-javadoc-plugin/examples/alternate-doclet.html) if you want to make use of the maven-javadoc-plugin.

First you have to check out the *TeXDoclet* code and make a maven build (by `mvn clean install`). This installs the *TeXDoclet* artifact in your local repository.
Then you can use TeXDoclet as an alternate doclet in the `pom.xml` file of any project like this :

	<project>
	...
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-javadoc-plugin</artifactId>
				<version>2.9</version>
				<configuration>
					<doclet>org.stfm.texdoclet.TeXDoclet</doclet>
					<docletArtifact>
						<groupId>org.stfm</groupId>
						<artifactId>texdoclet</artifactId>
						<version>0.9-SNAPSHOT</version>
					</docletArtifact>
					<sourcepath>src/main/java:src/test/java</sourcepath>
					<useStandardDocletOptions>false</useStandardDocletOptions><!-- important ! -->
					<destDir>apidocs_tex</destDir>
					<additionalOptions>
						<additionalOption>-tree<additionalOption>
						<additionalOption>-hyperref<additionalOption>
						<additionalOption>-output TeXDoclet.tex<additionalOption>
						<additionalOption>-createpdf<additionalOption>
						<additionalOption>-title "TeXDoclet Java Documentation"<additionalOption>
						<additionalOption>-subtitle "Created with Javadoc TeXDoclet Doclet"<additionalOption>
						<additionalOption>-author "Greg Wonderly \and S{\"o}ren Caspersen \and Stefan Marx"<additionalOption>
						<additionalOption>-subpackages org<additionalOption>
						<additionalOption>-shortinherited<additionalOption>
					</additionalOptions>
				</configuration>
			</plugin>
			...
		</plugins>
	</build>
	...

Calling `mvn javadoc:javadoc` creates `TeXDoclet.tex` (and `TeXDoclet.pdf`, see switch `-createpdf`) in the `target/site/apidocs/apidocs_tex` directory.

See `pom.xml` for example usage and more details about how to integrate *TeXDoclet* with maven `site:site` goal. Also check out [this example maven site project documentation](http://doclet.github.io/texdoclet/site).

Known issues
------------

If the `pdflatex` document compilation fails with a "TeX capacity exceeded, sorry ..." error message you better use the alternative command `lualatex` from the [LuaTeX project](http://www.luatex.org).

Previous versions
-----------------

- The intitial project of Greg Wonderly is available here: <http://java.net/projects/texdoclet>.
- Its [revision](http://egee-jra1-integration.web.cern.ch/egee-jra1-integration/repository/texdoclet/1.3/share/README.txt) by Soeren Caspersen you find here: <http://egee-jra1-integration.web.cern.ch/egee-jra1-integration/repository/texdoclet>.

License
-----------------

TeXDoclet is free software and available under the terms of [BSD-2-Clause](http://opensource.org/licenses/BSD-2-Clause) License. For more information see LICENSE.txt.
