cd lumify-public/lumify-web-war/src/main/webapp
if [ "${PROXY_URL}" ]; then
  npm config set registry http://registry.npmjs.org/
  npm config set proxy ${PROXY_URL}
fi
npm install
grunt test:functional
