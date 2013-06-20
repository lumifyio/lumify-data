Postgress
=========

```
curl -O http://yum.postgresql.org/9.2/redhat/rhel-6-x86_64/pgdg-centos92-9.2-6.noarch.rpm
sudo rpm -ivh pgdg-centos92-9.2-6.noarch.rpm 
sudo yum install postgresql-server

service postgresql initdb
chkconfig postgresql on
service postgresql start

# TODO: move data files to the 2TB drive
```
