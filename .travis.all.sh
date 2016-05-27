#!/bin/bash

if [ -e mxbuild/mx ]; then
  cd mxbuild/mx
  git pull
else
  mkdir -p mxbuild
  cd mxbuild
  git clone ssh://ol-bitbucket.us.oracle.com:7999/~jaroslav.tulach_oracle.com/mx.git
  cd mx
fi
git checkout SanityCheck

PATH=`pwd`:$PATH
cd ../..

export SANITYCHECK_URL=ssh://git@ol-bitbucket.us.oracle.com:7999/g/graalvm.git
export DEFAULT_VM=server
#export MX_BINARY_SUITES=graal-enterprise,jvmci,graal-js-extensions,graal-core,graal-js,graal-avatarjs,fastr,jruby

mx sanitycheck -v --vm server --vmbuild product build
mx sanitycheck unittest interop.sieve

