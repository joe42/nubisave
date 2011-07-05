include $(top_srcdir)/Makefile.common

VAPIDIR = $(top_srcdir)/vapi

bin_PROGRAMS = nubisave

BUILT_SOURCES = nubisave.vala.stamp

nubisave_VALAFLAGS = \
	--thread \
	$(NUBISAVE_VALA_PKGS) \
	--vapidir $(VAPIDIR) \
	$(NULL)

nubisave_CFLAGS = \
	$(NUBISAVE_CFLAGS) \
	-I$(top_srcdir) \
	-D_FILE_OFFSET_BITS=64 \
	-DFUSE_USE_VERSION=26 \
	$(NULL)

nubisave_LDADD = \
	$(NUBISAVE_LIBS) \
	$(NULL)

nubisave_LDFLAGS = \
	$(NUBISAVE_LDFLAGS) \
	$(NULL)

nubisave_VALASOURCES = \
	CloudFile.vala \
	CloudFilePart.vala \
	CloudStorage.vala \
	Core.vala \
	Logger.vala \
	Main.vala \
	Paths.vala \
	Preferences.vala \
	Uuid.vala \
	ida/CauchyDecode.vala \
	ida/CauchyEncode.vala \
	ida/CauchyIDAParameters.vala \
	ida/CauchyInformationDispersalCodec.vala \
	ida/CauchyInformationDispersalDecoder.vala \
	ida/CauchyInformationDispersalEncoder.vala \
	ida/FiniteStack.vala \
	ida/InformationDispersalCodec.vala \
	ida/InformationDispersalCodecBase.vala \
	ida/InformationDispersalDecoder.vala \
	ida/InformationDispersalEncoder.vala \
	ida/InformationDispersalError.vala \
	ida/InitField.vala \
	$(NULL)

nubisave_SOURCES = \
	nubisave.vala.stamp \
	$(nubisave_VALASOURCES:.vala=.c) \
	$(NULL)

nubisave.vala.stamp: $(nubisave_VALASOURCES)
	$(AM_V_VALA)$(VALAC) -g -C $(nubisave_VALAFLAGS) $(filter %.vala %.c,$^)
	$(AM_V_at)touch $@

DISTCLEANFILES = \
	$(nubisave_SOURCES) \
	nubisave_vala.stamp \
	$(NULL)

EXTRA_DIST = \
	$(nubisave_VALASOURCES) \
	nubisave.vala.stamp \
	$(NULL)
