aws
===

Prerequisites
-------------
Ensure the EC2 tools are in your `PATH`. Export `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`,
and `EC2_URL` values. `setenv-EXAMPLE.sh` is provided as an example.

Install the `mime` Ruby gem: `sudo gem install mime --version 0.3.0`

VPC Setup
---------
Create a VPC, subnet, security group(s), and ssh keypair. Copy `spinup-EXAMPLE.yml`
as `spinup.yml` with values for your VPC environment.
