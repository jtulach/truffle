#!/bin/bash
mkdir -p mxbuild/all/truffle
cp -r .git * mxbuild/all/truffle/
cd mxbuild/all
hg clone http://graal.us.oracle.com/hg/graalvm/
cd graalvm

export DEFAULT_VM=server
export MX_BINARY_SUITES=graal-enterprise,jvmci,graal-js-extensions,graal-core,graal-js,graal-avatarjs,fastr

mx --vm server --vmbuild product build
mx unittest interop.sieve

