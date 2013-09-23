class env::cluster::node {
  include hadoop_slave
  include accumulo_node
  include elasticsearch_node
  include ::ffmpeg
  include ::ccextractor
  include ::tesseract
  include ::opencv
}
