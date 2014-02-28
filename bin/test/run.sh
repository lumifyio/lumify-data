cd lumify-public/lumify-web-war/src/main/webapp
if [ $(hostname) = 'sfeng-win7' ]; then
  npm config set registry http://registry.npmjs.org/
  npm config set proxy http://10.0.1.143:3128
fi
npm install
grunt test:functional
