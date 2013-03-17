TeXDoclet
=========

TeXDoclet is a Java doclet implementation that generates a LaTeX file from you Java code documentation.

The doclet is based on the doclet originally created by Greg Wonderly of
[C2 technologies Inc.](http://www.c2-tech.com>) and its revision by Soeren Caspersen of
[XO Software](http://www.xosoftware.dk).

See <http://doclet.github.com> for more information.

Build
-----

    mvn clean install

Run
---

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
		-subpackages org

See `createDocs.sh` scripts in `/examples` subdirectory for more examples.

Print help (TeXDoclet + javadoc help) :

	javadoc -docletpath target/TeXDoclet.jar -doclet org.stfm.texdoclet.TeXDoclet

or (TeXDoclet help only) :

	java -jar target/TeXDoclet.jar -h

Previous versions
-----------------

- The intitial project of Greg Wonderly is available here : <http://java.net/projects/texdoclet>.
- Its [revision](http://egee-jra1-integration.web.cern.ch/egee-jra1-integration/repository/texdoclet/1.3/share/README.txt) by Soeren Caspersen you find here : <http://egee-jra1-integration.web.cern.ch/egee-jra1-integration/repository/texdoclet>.

License
-----------------

TeXDoclet is free software and available under the terms of [BSD-2-Clause](http://opensource.org/licenses/BSD-2-Clause) License. For more information see LICENSE.txt.