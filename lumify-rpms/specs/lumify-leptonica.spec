Name:           lumify-leptonica
Version:        $VERSION
Release:        $RELEASE
Summary:        Leptonica is a pedagogically-oriented open source site containing software that is broadly useful for image processing and image analysis applications.
Group:          System Environment/Libraries
License:        GPL
URL:            http://www.leptonica.com/
Source:         $SOURCE
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

%description

%prep
%setup -q -n %{name}

%build
%configure --prefix='/usr/local'
export QA_RPATHS=$[ 0x0001 ]
make %{?_smp_mflags}

%install
rm -rf %{buildroot}
export QA_RPATHS=$[ 0x0001 ]
make install DESTDIR=%{buildroot}

%clean
rm -rf %{buildroot}

%files
%defattr(-,root,root,-)
%doc README.html
/usr/bin/adaptmaptest
/usr/bin/adaptnorm_reg
/usr/bin/affine_reg
/usr/bin/alltests_reg
/usr/bin/alphaops_reg
/usr/bin/alphaxform_reg
/usr/bin/arithtest
/usr/bin/barcodetest
/usr/bin/baselinetest
/usr/bin/bilinear_reg
/usr/bin/binarize_reg
/usr/bin/bincompare
/usr/bin/binmorph1_reg
/usr/bin/binmorph2_reg
/usr/bin/binmorph3_reg
/usr/bin/binmorph4_reg
/usr/bin/binmorph5_reg
/usr/bin/blend2_reg
/usr/bin/blend_reg
/usr/bin/blendcmaptest
/usr/bin/blendtest1
/usr/bin/buffertest
/usr/bin/byteatest
/usr/bin/ccbordtest
/usr/bin/cctest1
/usr/bin/ccthin1_reg
/usr/bin/ccthin2_reg
/usr/bin/cmapquant_reg
/usr/bin/coloring_reg
/usr/bin/colormask_reg
/usr/bin/colormorphtest
/usr/bin/colorquant_reg
/usr/bin/colorseg_reg
/usr/bin/colorsegtest
/usr/bin/colorspacetest
/usr/bin/compare_reg
/usr/bin/comparepages
/usr/bin/comparetest
/usr/bin/compfilter_reg
/usr/bin/conncomp_reg
/usr/bin/contrasttest
/usr/bin/conversion_reg
/usr/bin/convertfilestopdf
/usr/bin/convertfilestops
/usr/bin/convertformat
/usr/bin/convertsegfilestopdf
/usr/bin/convertsegfilestops
/usr/bin/converttogray
/usr/bin/converttops
/usr/bin/convolve_reg
/usr/bin/convolvetest
/usr/bin/cornertest
/usr/bin/croptext
/usr/bin/dewarp_reg
/usr/bin/dewarptest1
/usr/bin/dewarptest2
/usr/bin/dewarptest3
/usr/bin/digitprep1
/usr/bin/distance_reg
/usr/bin/dithertest
/usr/bin/dna_reg
/usr/bin/dwalineargen
/usr/bin/dwamorph1_reg
/usr/bin/dwamorph2_reg
/usr/bin/edgetest
/usr/bin/enhance_reg
/usr/bin/equal_reg
/usr/bin/expand_reg
/usr/bin/extrema_reg
/usr/bin/falsecolortest
/usr/bin/fcombautogen
/usr/bin/fhmtauto_reg
/usr/bin/fhmtautogen
/usr/bin/fileinfo
/usr/bin/findpattern1
/usr/bin/findpattern2
/usr/bin/findpattern3
/usr/bin/findpattern_reg
/usr/bin/flipdetect_reg
/usr/bin/flipselgen
/usr/bin/fmorphauto_reg
/usr/bin/fmorphautogen
/usr/bin/fpix_reg
/usr/bin/fpixcontours
/usr/bin/gammatest
/usr/bin/genfonts
/usr/bin/gifio_reg
/usr/bin/graphicstest
/usr/bin/grayfill_reg
/usr/bin/graymorph1_reg
/usr/bin/graymorph2_reg
/usr/bin/graymorphtest
/usr/bin/grayquant_reg
/usr/bin/hardlight_reg
/usr/bin/heap_reg
/usr/bin/histotest
/usr/bin/inserttest
/usr/bin/ioformats_reg
/usr/bin/iotest
/usr/bin/jbcorrelation
/usr/bin/jbrankhaus
/usr/bin/jbwords
/usr/bin/kernel_reg
/usr/bin/lineremoval
/usr/bin/listtest
/usr/bin/livre_adapt
/usr/bin/livre_hmt
/usr/bin/livre_makefigs
/usr/bin/livre_orient
/usr/bin/livre_pageseg
/usr/bin/livre_seedgen
/usr/bin/livre_tophat
/usr/bin/locminmax_reg
/usr/bin/logicops_reg
/usr/bin/lowaccess_reg
/usr/bin/maketile
/usr/bin/maze_reg
/usr/bin/misctest1
/usr/bin/modifyhuesat
/usr/bin/morphseq_reg
/usr/bin/morphtest1
/usr/bin/mtifftest
/usr/bin/numa_reg
/usr/bin/numaranktest
/usr/bin/otsutest1
/usr/bin/otsutest2
/usr/bin/overlap_reg
/usr/bin/pagesegtest1
/usr/bin/pagesegtest2
/usr/bin/paint_reg
/usr/bin/paintmask_reg
/usr/bin/partitiontest
/usr/bin/pdfiotest
/usr/bin/pdfseg_reg
/usr/bin/pixa1_reg
/usr/bin/pixa2_reg
/usr/bin/pixaatest
/usr/bin/pixadisp_reg
/usr/bin/pixalloc_reg
/usr/bin/pixcomp_reg
/usr/bin/pixmem_reg
/usr/bin/pixserial_reg
/usr/bin/pixtile_reg
/usr/bin/plottest
/usr/bin/pngio_reg
/usr/bin/printimage
/usr/bin/printsplitimage
/usr/bin/printtiff
/usr/bin/projection_reg
/usr/bin/projective_reg
/usr/bin/psio_reg
/usr/bin/psioseg_reg
/usr/bin/pta_reg
/usr/bin/ptra1_reg
/usr/bin/ptra2_reg
/usr/bin/quadtreetest
/usr/bin/rank_reg
/usr/bin/rankbin_reg
/usr/bin/rankhisto_reg
/usr/bin/ranktest
/usr/bin/rasterop_reg
/usr/bin/rasteropip_reg
/usr/bin/reducetest
/usr/bin/removecmap
/usr/bin/renderfonts
/usr/bin/rotate1_reg
/usr/bin/rotate2_reg
/usr/bin/rotatefastalt
/usr/bin/rotateorth_reg
/usr/bin/rotateorthtest1
/usr/bin/rotatetest1
/usr/bin/runlengthtest
/usr/bin/scale_reg
/usr/bin/scaleandtile
/usr/bin/scaletest1
/usr/bin/scaletest2
/usr/bin/seedfilltest
/usr/bin/seedspread_reg
/usr/bin/selio_reg
/usr/bin/sharptest
/usr/bin/shear2_reg
/usr/bin/shear_reg
/usr/bin/sheartest
/usr/bin/showedges
/usr/bin/skew_reg
/usr/bin/skewtest
/usr/bin/smallpix_reg
/usr/bin/smoothedge_reg
/usr/bin/snapcolortest
/usr/bin/sorttest
/usr/bin/splitcomp_reg
/usr/bin/splitimage2pdf
/usr/bin/string_reg
/usr/bin/subpixel_reg
/usr/bin/sudokutest
/usr/bin/textlinemask
/usr/bin/threshnorm_reg
/usr/bin/translate_reg
/usr/bin/trctest
/usr/bin/viewertest
/usr/bin/warper_reg
/usr/bin/warpertest
/usr/bin/watershedtest
/usr/bin/wordsinorder
/usr/bin/writemtiff
/usr/bin/writetext_reg
/usr/bin/xformbox_reg
/usr/bin/xtractprotos
/usr/bin/xvdisp
/usr/bin/yuvtest
/usr/include/leptonica/*
%{_libdir}/liblept.a
%{_libdir}/liblept.la
%{_libdir}/liblept.so
%{_libdir}/liblept.so.3
%{_libdir}/liblept.so.3.0.0

%changelog

