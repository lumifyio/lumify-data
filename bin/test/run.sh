cd lumify-public/lumify-web-war/src/main/webapp

if [ "${PROXY_URL}" ]; then
  npm config set registry http://registry.npmjs.org/
  npm config set proxy ${PROXY_URL}
else
  npm config delete registry
  npm config delete proxy
fi

npm install

grunt test:functional
