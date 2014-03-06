class Hiera
  module Backend
    class Facter_backend

      def initialize
        Hiera.debug("Hiera Facter backend starting")
        require 'facter'
      end

      def lookup(key, scope, order_override, resolution_type)
        Hiera.debug("Looking up #{key} in Facter backend")
        f = Facter[key]
        f ? f.value : nil
      end

    end
  end
end
