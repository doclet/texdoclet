mkdir texdoclet_output

rm texdoclet_output/TeXDoclet.aux

javadoc -docletpath ../../target/TeXDoclet.jar \
	-doclet org.stfm.texdoclet.TeXDoclet \
	-noindex \
	-hyperref \
	-texinit texdoclet_include/preamble.tex \
	-texsetup texdoclet_include/setup.tex \
	-texintro texdoclet_include/intro.tex \
	-texfinish texdoclet_include/finish.tex \
	-imagespath ".." \
	-output texdoclet_output/TeXDoclet.tex \
	-sourcepath ../../src/main/java \
	-subpackages org \
	-sectionlevel section \
	-docclass article \
	-nosummaries

cd texdoclet_output
pdflatex TeXDoclet.tex
pdflatex TeXDoclet.tex
mkdir -p ../javadoc
cp TeXDoclet.pdf ../javadoc
