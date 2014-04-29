class role::rabbitmq::node {
  $rabbitmq_nodes = hiera_array('rabbitmq_nodes', [])
  $rabbitmq_erlang_cookie = hiera('rabbitmq_erlang_cookie', '')
  class { '::rabbitmq' :
    cluster_nodes => $rabbitmq_nodes,
    erlang_cookie => $rabbitmq_erlang_cookie,
  }
}
