#How To Generate The MOLGENIS Compute 5 User Guide
This file describes how to generate the molgenis-compute-core documents in Bootstrap style

The MOLGENIS Compute 5 User Guide is available at <a href="https://rawgit.com/molgenis/molgenis-compute/master/molgenis-compute-core/README.html"> Molgenis Compute 5 User Guide</a>

##You will need:
* README.txt --> contains the content of the guide.
* Images related to the content.
* Installation of python
* Installation of AsciiDoc
* Installation bootstrap docs backend

##Install Python
1. Check if python exists [python —version]
2. You can install Python from (https://www.python.org/downloads/)

##Install AsciiDocs
Text based document generation <a href=http://www.methods.co.nz/asciidoc/INSTALL.html />AsciiDoc</a>
Use the "Distribution tarball installation"

##Installation bootstrap docs backend
1. To install the bootstrap docs backend, download <a href="https://github.com/downloads/mojavelinux/asciidoc-bootstrap-docs-backend/bootstrap-docs.zip" />bootstrap-docs.zip</a> and install it using the asciidoc command (you will need AsciiDoc version 8.6.6 or newer):
 * ```asciidoc --backend install bootstrap-docs.zip```
 * If multiple versions of python installed use this:
 ** ```python2.7 <location>/asciidoc.py --backend install bootstrap-docs.zip```

2. Next, checkout the Twitter Bootstrap repository in the backend folder:
 * ```cd ~/.asciidoc/backends/bootstrap/bootstrap-docs```
 * ```git clone —branch v2.3.2 https://github.com/twbs/bootstrap.git```

If you enable the link-assets attribute, then you should also checkout (or symlink) the bootstrap clone in the same directory as the rendered file. You also need to create a symlink to asciidoc.js included in this backend.

##Generating file:
* ```asciidoc -b ~/.asciidoc/backends/bootstrap/bootstrap-docs/bootstrap-docs ~/.asciidoc/README.txt```

##Error message: 
If you get a error you probably should create a directory for the generated doc.

##Adding pictures
for adding pictures use img tags in the generated html
* ```<p><img src="images/dashboard.png"></p>```
