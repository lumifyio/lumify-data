class role::hadoop::datanode {
  include ::hadoop::tasktracker
  include ::hadoop::datanode
  include ::zookeeper
  include ::ffmpeg
  include ::ccextractor
  include ::tesseract
  include ::opencv
}