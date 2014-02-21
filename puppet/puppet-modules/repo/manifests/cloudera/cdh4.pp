class repo::cloudera::cdh4 {
    yumrepo { 'cloudera-cdh4-$basearch':
        descr    => "Cloudera's Distribution for Hadoop, Version 4",
        baseurl  => 'http://archive.cloudera.com/cdh4/redhat/6/$basearch/cdh/4.4.0',
        gpgcheck => 1,
        gpgkey   => 'http://archive.cloudera.com/cdh4/redhat/6/$basearch/cdh/RPM-GPG-KEY-cloudera',
        enabled  => 1,
    }
}
