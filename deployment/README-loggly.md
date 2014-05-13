1. lookup your Customer Token at [https://dsingley.loggly.com/tokens](https://dsingley.loggly.com/tokens)
1. create or edit `/etc/puppet/hiera/local.yaml` on the puppet master:

        ---
        loggly_token: <value from the website>
        loggly_tag: <identifier for the Lumify instance, e.g. 'lumify_demo_01'>

1. edit `/etc/puppet/manifests/site.pp` on the puppet master
  * find the node with:

        include env::cluster::syslog

  * and add:

        include env::cluster::loggly
