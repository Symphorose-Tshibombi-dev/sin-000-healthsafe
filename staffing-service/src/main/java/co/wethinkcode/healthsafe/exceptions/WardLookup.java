package co.wethinkcode.healthsafe.exceptions;

import co.wethinkcode.healthsafe.WardInfo;

public interface WardLookup {

    /**
     * @throws WardNotFoundException          if the ward does not exist
     * @throws DownstreamUnavailableException if ward-service cannot be used
     */
    WardInfo findWard(String wardId);
}