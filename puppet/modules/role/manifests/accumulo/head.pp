class role::accumulo::head {
  include ::accumulo::master
  include ::accumulo::gc
  include ::accumulo::monitor
  include ::accumulo::tracer
}
