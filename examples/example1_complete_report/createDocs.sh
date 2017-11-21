mkdir texdoclet_output

rm texdoclet_output/TeXDoclet.aux

javadoc -docletpath ../../target/TeXDoclet.jar \
	-doclet org.stfm.texdoclet.TeXDoclet \
	-tree \
	-hyperref \
	-imagespath ".." \
	-output texdoclet_output/TeXDoclet.tex \
	-title "TeXDoclet Java Documentation" \
	-subtitle "Created with Javadoc TeXDoclet Doclet" \
	-author "Greg Wonderly \and S{\"o}ren Caspersen \and Stefan Marx" \
	-sourcepath ../../src/main/java:../../src/test/java \
	-subpackages org \
 	-shortinherited \
 	-classdeclrframe trBL \
 	-methoddeclrframe single

cd texdoclet_output
pdflatex TeXDoclet.tex
pdflatex TeXDoclet.tex
mkdir -p ../javadoc
cp TeXDoclet.pdf ../javadoc
