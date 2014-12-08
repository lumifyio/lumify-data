class repo::elasticsearch {
  yumrepo { 'elasticsearch-1.4' :
    descr    => 'Elasticsearch repository for 1.4.x packages',
    baseurl  => 'http://packages.elasticsearch.org/elasticsearch/1.4/centos',
    gpgcheck => 1,
    gpgkey   => 'http://packages.elasticsearch.org/GPG-KEY-elasticsearch',
    enabled  => 1,
  }
}
