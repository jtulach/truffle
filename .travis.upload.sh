#!/bin/bash
mkdir -p mxbuild/all/truffle
cp -r .git * mxbuild/all/truffle/
cd mxbuild/all
hg clone http://graal.us.oracle.com/hg/graalvm/
cd graalvm
export DEFAULT_VM=server

mx --vm server --vmbuild product build

mx deploy-binary --all-suites --skip-existing graal-us-snapshots http://graal.us.oracle.com/nexus/content/repositories/snapshots
