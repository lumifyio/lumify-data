#!/usr/bin/expect

set instancename [lindex $argv 0]
set password [lindex $argv 1]

spawn /usr/lib/accumulo/bin/accumulo init

expect "Instance name :" { send "$instancename"; sleep 1 }
expect "Enter initial password for root:" { send "$password"; sleep 1; }
expect "Confirm initial password for root:" { send "$password"; sleep 1; }
