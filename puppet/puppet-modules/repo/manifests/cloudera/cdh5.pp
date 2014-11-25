class repo::cloudera::cdh5 {
    yumrepo { 'cloudera-cdh5' :
        descr    => "Cloudera's Distribution for Hadoop, Version 5",
        baseurl  => 'http://archive.cloudera.com/cdh5/redhat/6/x86_64/cdh/5/',
        gpgcheck => 1,
        gpgkey   => 'http://archive.cloudera.com/cdh5/redhat/6/x86_64/cdh/RPM-GPG-KEY-cloudera',
        enabled  => 1,
    }
}
