class role::rabbitmq::node {
  $rabbitmq_erlang_cookie = hiera('rabbitmq_erlang_cookie', '')
  class { '::rabbitmq' :
    erlang_cookie => $rabbitmq_erlang_cookie,
  }
}
