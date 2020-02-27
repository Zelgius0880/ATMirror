package zelgius.com.networking.protocol

class StopDiscovery(): Protocol(
    Code.STOP_DISCOVERY.size){
    override val code: Code =
        Code.STOP_DISCOVERY
}