package zelgius.com.shared.entities.protocol

class StartDiscovery(): Protocol (Code.STOP_DISCOVERY.size){
    override val code: Code = Code.START_DISCOVERY
}