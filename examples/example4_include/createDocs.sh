#
# - this example shows how to use the -include option
# - TeXDoclet generated output (TeXDoclet.tex and TeXDoclet_preamble.tex) 
#   is included in another Latex document (MyDocument.tex) here
#

mkdir texdoclet_output

rm MyDocument.aux
rm MyDocument.idx
rm MyDocument.out
rm MyDocument.toc
rm MyDocument.pdf
rm texdoclet_output/TeXDoclet.aux
rm texdoclet_output/TeXDoclet_preamble.aux

# -imagespath ".." not needed here because path texdoclet_images is in subdir of MyDocument.tex

javadoc -docletpath ../../target/TeXDoclet.jar \
	-doclet org.stfm.texdoclet.TeXDoclet \
	-tree \
	-noindex \
	-hyperref \
	-output texdoclet_output/TeXDoclet.tex \
	-sourcepath ../../src/main/java \
	-subpackages org \
 	-include \
 	-sectionlevel section

pdflatex MyDocument.tex
pdflatex MyDocument.tex
