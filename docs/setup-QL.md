# Lumify for QL

## Features / Limitations

* read-only data via custom [securegraph](http://securegraph.org/) implementation
* user and workspace data stored in MySQL via `SqlUserRepository` and `SqlWorkspaceRepository`
* ontology loaded from disk via `ReadOnlyInMemoryOntologyRepository`
* X.509 certificate and LDAP user authentication
* deployed at `/lumify` on Tomcat and accessed through HTTPd proxy


## Setup

1. run the `ql` VM that includes RabbitMQ and additional components including MySQL, LDAP, HTTPd, and Tomcat servers

        vagrant up ql

1. build and install the model-sql, x509ldap, and classification plugins

        sudo mkdir -p /opt/lumify/lib

        mvn package -pl lumify-public/core/plugins/model-sql -am
        sudo cp lumify-public/core/plugins/model-sql/target/lumify-model-sql-*-jar-with-dependencies.jar /opt/lumify/lib

        mvn package -pl lumify-web-auth-x509ldap -am
        sudo cp lumify-web-auth-x509ldap/target/lumify-web-auth-x509ldap-*-jar-with-dependencies.jar /opt/lumify/lib

        mvn package -pl lumify-classification -am
        sudo cp lumify-classification/target/lumify-classification-*.jar /opt/lumify/lib

1. configure Lumify via `/opt/lumify/config/lumify.properties`

        # use MySQL for user and workspace data
        repository.user=io.lumify.sql.model.user.SqlUserRepository
        repository.workspace=io.lumify.sql.model.workspace.SqlWorkspaceRepository
        repository.longRunningProcess=io.lumify.sql.model.workspace.SqlLongRunningProcessRepository

        # read the ontology from disk
        repository.ontology=io.lumify.core.model.ontology.ReadOnlyInMemoryOntologyRepository
        repository.ontology.owl.1.iri=http://lumify.io/dev
        repository.ontology.owl.1.dir=/opt/lumify/ontology/dev

        # standard work queue configuration
        repository.workQueue=io.lumify.model.rabbitmq.RabbitMQWorkQueueRepository
        rabbitmq.addr.0.host=192.168.33.10

        # no-op implementions for features not required for read-only use
        repository.authorization=io.lumify.core.model.user.NoOpAuthorizationRepository
        repository.audit=io.lumify.core.model.audit.NoOpAuditRepository
        repository.termMention=io.lumify.core.model.termMention.NoOpTermMentionRepository
        repository.detectedObject=io.lumify.core.model.detectedObjects.NoOpDetectedObjectRepository
        repository.artifactThumbnail=io.lumify.core.model.artifactThumbnails.NoOpArtifactThumbnailRepository

        # QL will use the custom implementation
        graph=org.securegraph.inmemory.InMemoryGraph
        graph.search=org.securegraph.search.DefaultSearchIndex

        # all users have read-only access
        newuser.privileges=READ

1. configure Hibernate, the ontology, and LDAP

        sudo cp lumify-public/core/plugins/model-sql/config/hibernate.cfg.xml /opt/lumify/config

        sudo mkdir -p /opt/lumify/ontology
        sudo ln -s lumify-public/examples/ontology-dev /opt/lumify/ontology/dev

        sudo cp lumify-web-auth-x509ldap/config/ldap.properties /opt/lumify/config

1. install one or more user certficates in your browser by importing `.pkcs12` files from the `config/ssl/users` directory


## Run in the VM

1. build and deploy the web app

        mvn package -P web-war -pl lumify-public/web/war -am -DskipTests
        sudo cp lumify-public/web/war/target/lumify-web-war-*.war /opt/tomcat/webapps/lumify.war
        sudo initctl restart tomcat

1. browse to [https://192.168.33.10/lumify](https://192.168.33.10/lumify)


## Run in IntelliJ

1. create a Run/Debug Configuration for an Application with the following settings:

    | Field      | Value                         |
    |------------|-------------------------------|
    | Main class | io.lumify.web.TomcatWebServer |
    |Program arguments | --port=8888 <br/> --httpsPort=8889 <br/> --keyStorePath=/Users/dsingley/Documents/NIC/lumify-all/config/ssl/lumify-vm.lumify.io.jks <br/> --keyStorePassword=password <br/> --trustStorePath=/Users/dsingley/Documents/NIC/lumify-all/config/ssl/lumify-ca.jks <br/> --trustStorePassword=password <br/> --requireClientCert <br/> --context-path=/lumify <br/> --webapp-dir=/Users/dsingley/Documents/NIC/lumify-all/lumify-public/web/war/src/main/webapp |
    | Use classpath of module | lumify-ql-tomcat-server |

1. browse to [https://localhost:8889/lumify](https://localhost:8889/lumify)

