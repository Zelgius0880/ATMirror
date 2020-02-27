package zelgius.com.shared.protocol

class StopDiscovery(): Protocol(
    Code.STOP_DISCOVERY.size){
    override val code: Code =
        Code.STOP_DISCOVERY
}