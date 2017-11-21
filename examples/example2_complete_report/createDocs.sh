#
# this example demonstrates the use of different .tex inlude files
#

mkdir texdoclet_output

rm texdoclet_output/TeXDoclet.aux

javadoc -docletpath ../../target/TeXDoclet.jar \
	-doclet org.stfm.texdoclet.TeXDoclet \
	-noindex \
	-tree \
	-hyperref \
	-texsetup texdoclet_include/setup.tex \
	-texintro texdoclet_include/intro.tex \
	-texfinish texdoclet_include/finish.tex \
	-texinit texdoclet_include/preamble.tex \
	-imagespath ".." \
	-output texdoclet_output/TeXDoclet.tex \
	-title "TeXDoclet Java Documentation" \
	-subtitle "Created with Javadoc TeXDoclet Doclet" \
	-author "Greg Wonderly \and S\"oren Caspersen \and Stefan Marx" \
	-sourcepath ../../src/main/java \
	-subpackages org \
 	-sectionlevel section

cd texdoclet_output
pdflatex TeXDoclet.tex
pdflatex TeXDoclet.tex
mkdir -p ../javadoc
cp TeXDoclet.pdf ../javadoc
