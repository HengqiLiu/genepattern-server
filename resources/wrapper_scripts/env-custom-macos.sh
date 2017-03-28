#!/usr/bin/env bash

#
# Custom runtime environment for MacOS X installation
#


#
# Initialize runtime environment. 
#     Usage: initEnv <runtime-env-name>
# initEnv will be called at module runtime for each required <runtime-env-name>
#
# See env-default.sh for canonical runtime environment names
#
function initEnv() {
    if ! [ -z ${GP_DEBUG+x} ]; then
        # only when the GP_DEBUG flag is set
        echo "loading $1 ..."
        echo R_LIBS_SITE=${R_LIBS_SITE}
    fi
    
    # set path for Java-1.7
    if [ "$1" = "Java-1.7" ]; then
        setjdk 1.7

    # set path for Java-1.8
    elif [ "$1" = "Java-1.8" ]; then
        setjdk 1.8

    # set path for R-3.3
    elif [ "$1" = "R-3.3" ]; then
        # add Rscript to path
        R_HOME=/Library/Frameworks/R.framework/Versions/3.3/Resources
        GP_SET_R_PATH=true;

    # set path for R-3.2
    elif [ "$1" = "R-3.2" ]; then
        # add Rscript to path
        R_HOME=/Library/Frameworks/R.framework/Versions/3.2/Resources
        GP_SET_R_PATH=true;

    # set path for R-3.1
    elif [ "$1" = "R-3.1" ]; then
        # add Rscript to path 
        export R_HOME=/Library/Frameworks/R.framework/Versions/3.1/Resources
        GP_SET_R_PATH=true;

        # note: you must edit your local R/3.1 installation 
        #   in order to work with multiple versions of R
        #   this is only strictly necessary if you install a newer (than 3.1)
        #   version of R on your Mac.
        #
        # use the include ./R/3.1/R_v3.1.3.patch file
        # 
        #   cd /Library/Frameworks/R.framework/Versions/3.1/Resources/bin
        #   # first, backup the existing R file
        #   cp R R.orig
        #   # then, apply the patch
        #   patch < R_v3.1.3.patch
        # these export statements have no effect ...
        #   export R_HOME_DIR=/Library/Frameworks/R.framework/Versions/3.1/Resources
        #   export R_SHARE_DIR="${R_HOME_DIR}/share"
        #   export R_INCLUDE_DIR="${R_HOME_DIR}/include"
        #   export R_DOC_DIR="${R_HOME_DIR}/doc"

    # set path for R-3.0
    elif [ "$1" = "R-3.0" ]; then
        # add Rscript to path
        R_HOME=/Library/Frameworks/R.framework/Versions/3.0/Resources
        GP_SET_R_PATH=true;

    # set path for R-2.15
    elif [ "$1" = "R-2.15" ]; then
        # add Rscript to path
        R_HOME=/Library/Frameworks/R.framework/Versions/2.15/Resources
        GP_SET_R_PATH=true;

    # set path for R-2.5
    elif [ "$1" = "R-2.5" ]; then
        # add Rscript to path
        R_HOME=/Library/Frameworks/R.framework/Versions/2.5/Resources
        GP_SET_R_PATH=true;
    
    # set path for R-2.0
    elif [ "$1" = "R-2.0" ]; then
        R_HOME=/Library/Frameworks/R.framework/Versions/2.0/Resources
        GP_SET_R_PATH=true;
    fi

    # add R_HOME/bin to the PATH
    if ! [ -z ${GP_SET_R_PATH+x} ]; then
        echo "adding '${R_HOME}/bin' to the PATH ..."
        export PATH=${R_HOME}/bin:${PATH}
    fi

}

#
# helper function to switch version of java
#
# Usage: setjdk 1.7 
# Usage: setjdk 1.8 
#
# thanks JayWay, http://www.jayway.com/2014/01/15/how-to-switch-jdk-version-on-mac-os-x-maverick/
#
function setjdk() {
    if [ $# -ne 0 ]; then
        removeFromPath '/System/Library/Frameworks/JavaVM.framework/Home/bin'
        if [ -n "${JAVA_HOME+x}" ]; then
            removeFromPath $JAVA_HOME
        fi
        export JAVA_HOME=`/usr/libexec/java_home -v $@`
        export PATH=$JAVA_HOME/bin:$PATH
    fi
}

function removeFromPath() {
    export PATH=$(echo $PATH | sed -E -e "s;:$1;;" -e "s;$1:?;;")
}

