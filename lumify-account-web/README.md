This module is a web application for self-registration of user accounts in demonstration instances of [Lumify](http://lumify.io).


### Development

1. copy [docs/account.properties](docs/account.properties) to `/opt/lumify/config` with appropriate values

1. run `mvn jetty:run`

1. browse to [http://localhost:8080](http://localhost:8080)

1. use `-Djetty.port=PORT` to specify a different port