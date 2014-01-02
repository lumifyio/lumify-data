class repo::cloudera::cdh3 {
    yumrepo { 'cloudera-cdh3':
        descr    => "Cloudera's Distribution for Hadoop, Version 3",
        baseurl  => 'http://archive.cloudera.com/redhat/6/x86_64/cdh/3u6',
        gpgcheck => 1,
        gpgkey   => 'http://archive.cloudera.com/redhat/6/x86_64/cdh/RPM-GPG-KEY-cloudera',
        enabled  => 1,
    }
}