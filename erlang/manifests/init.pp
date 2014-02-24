class erlang {
  require buildtools::epel

  package { 'erlang' :
		ensure   => installed,
  }
  
}