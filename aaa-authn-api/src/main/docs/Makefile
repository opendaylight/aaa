all: sssd_configuration.html sssd_configuration.pdf mapping.html


images = sssd_01.png sssd_02.png sssd_03.png sssd_04.png sssd_05.png

sssd_configuration.html: $(images)

sssd_configuration.pdf: $(images)

%.html: %.rst
	rst2html $< $@

%.pdf: %.rst
	rst2pdf --footer='-###Page###-' $< -o $@

%.png: %.svg
	inkscape -z -e $@ -w 800 $<

sssd_01.svg: sssd_01.diag
	blockdiag -Tsvg $<

sssd_02.svg: sssd_02.diag
	blockdiag -Tsvg $<

sssd_03.svg: sssd_03.diag
	seqdiag -Tsvg $<

sssd_04.svg: sssd_04.diag
	blockdiag -Tsvg $<
