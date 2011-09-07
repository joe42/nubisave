AC_INIT([nubisave], [0.0.1])

AC_CONFIG_MACRO_DIR([m4])
AC_CONFIG_SRCDIR([.])
AC_CONFIG_HEADERS([config.h])

AM_INIT_AUTOMAKE([1.11 foreign tar-pax])

dnl pkg-config
AC_PATH_PROG(PKG_CONFIG, pkg-config, no)
if test "x$PKG_CONFIG" = "xno"; then
        AC_MSG_ERROR([You need to install pkg-config])
fi

AC_PROG_INSTALL
AC_PROG_CC
AM_PROG_CC_C_O
AC_PROG_LIBTOOL
AC_STDC_HEADERS

VALA_MIN_VERSION=0.12.0
GLIB_REQUIRED=2.12.0

AM_PROG_VALAC([$VALA_MIN_VERSION])
if test "x$VALAC" = "x"; then
	AC_MSG_ERROR([You need a Vala $VALA_MIN_VERSION compiler])
fi

AM_SILENT_RULES([yes])

PKG_CHECK_MODULES(NUBISAVE, 
	glib-2.0 >= $GLIB_REQUIRED
	gobject-2.0 >= $GLIB_REQUIRED
	gio-2.0
	gio-unix-2.0	
	gee-1.0
	fuse
	uuid
)
NUBISAVE_VALA_PKGS="--pkg gio-unix-2.0 \
                    --pkg gee-1.0 \
                    --pkg posix \
                    --pkg fuse"
AC_SUBST(NUBISAVE_VALA_PKGS)

AC_CONFIG_FILES([
	Makefile
	src/Makefile
])

AC_OUTPUT

cat <<EOF

${PACKAGE}-${VERSION}

  Build Environment
    Install Prefix:     ${prefix}

EOF
