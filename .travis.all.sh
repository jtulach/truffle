#!/bin/bash
mkdir -p mxbuild/all/truffle
cp -r .git * mxbuild/all/truffle/
cd mxbuild/all
hg clone http://graal.us.oracle.com/hg/graalvm/
cd graalvm
hg up -C mx4ruby

export DEFAULT_VM=server
export MX_BINARY_SUITES=graal-enterprise,jvmci,graal-js-extensions,graal-core,graal-js,graal-avatarjs,fastr,jruby

mx --vm server --vmbuild product build
mx unittest interop.sieve

