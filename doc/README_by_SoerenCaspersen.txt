This is a variant of the TexDoclet written by Greg Wonderly of C2 technologies. 
The original can be found at http://www.c2-tech.com/java/texdoclet.

If you are familiar with the original TexDoclet, these are the additional features 
this doclet has to offer:

- hyperref. This doclet uses the hyperref package, which means that
  you will be able to produce a browsable PDF-file with much similar
  features to those of Sun's standard doclet.
- A complete class hierarchy.
- Support for the standard option "-overview", which will generate a new
  chapter "Introduction".
- An index of all classes, interfaces, methods and fields.
- Support for double sided printing,
- Support for version tags.
- Support for throws tags
- A re engineered HTML to LaTeX translator. Gregs HTML-parser was not 
  very robust to "sloppy" HTML (no offense Greg:-). Instead a back end
  to swings HTML-parser has been implemented. The result is a much more
  robust translation. However, the number of supported HTML tags is still
  about the same.
- A new exciting feature is the possibility of passing TeX directly to 
  the TeX-compiler. This of course can seriously compromise the translation
  of HTML into LaTeX, but offers great possibilities for TeXperts. For example
  
  <TEX txt="\LaTeX{}">LaTeX</TEX>
  
  will result in the well known LaTeX-logo. Between the begin-tag and end-tag 
  it is possible to give some alternative HTML that will be ignored by the 
  TexDoclet, but will appear if the HTML is parsed by a doclet other that the 
  TexDoclet. It is encouraged to use this feature since it ensures that your 
  javadoc also makes sense when used with other doclets.
  This feature makes it possible to use scientific notation and to include 
  postscript images and much more.
- If an method is undocumented, the javadoc of the overridden method (if any) or
  javadoc of implementet interfaces is copied, as is the case in the standard 
  doclet provided by Sun.
- "All known subclasses" and "All classes known to implement interface" sections
  are createt for each class/interface.
- Support for the HTML tags <IMG>, <SUP> and <SUB>.
- Summaries of fields, constructors and methods.
  

TeXDocumentation.bat provides an example of how to use the doclet and compile
the result using the pdflatex compiler. JDK1.2 or higher and pdflatex is assumed
to be available on your path.

The TexDoclet now ships with the PngEncoder which is distributed under the LGPL 
(Lesser Gnu Public License).


Søren Caspersen
XO Software