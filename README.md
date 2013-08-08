aws
===

Prerequisites
-------------
Ensure the EC2 tools are in your `PATH` and export `AWS_ACCESS_KEY` and
`AWS_SECRET_KEY` values:

```bash
    export AWS_ACCESS_KEY=
    export AWS_SECRET_KEY=
    export EC2_URL=https://ec2.us-east-1.amazonaws.com
    # see http://docs.aws.amazon.com/general/latest/gr/rande.html#ec2_region

    export EC2_HOME=/path/to/ec2-api-tools-1.6.7.4
    export EC2_AMITOOL_HOME=/path/to/ec2-ami-tools-1.4.0.9
    # Amazon EC2 API Tools: http://aws.amazon.com/developertools/351
    # Amazon EC2 AMI Tools: http://aws.amazon.com/developertools/368

    export JAVA_HOME=$(readlink -f $(which java) | sed -E -e 's|/(jre)?/bin/java||')

    export PATH=$EC2_HOME/bin:$EC2_AMITOOL_HOME/bin:$JAVA_HOME/bin:$PATH
```
