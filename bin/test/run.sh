cd lumify-public/lumify-web-war/src/main/webapp

if [ "${PROXY_URL}" ]; then
  npm config set registry http://registry.npmjs.org/
  npm config set proxy ${PROXY_URL}
else
  npm config delete registry
  npm config delete proxy
fi

npm install
npm_exit=$?
echo "npm_exit is: ${npm_exit}"

rm -rf test/reports/*

grunt test:functional --force
grunt_exit=$?
echo "grunt_exit is: ${grunt_exit}"
exit ${grunt_exit}
