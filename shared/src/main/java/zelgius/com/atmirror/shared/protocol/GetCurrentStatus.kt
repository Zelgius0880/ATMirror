package zelgius.com.atmirror.shared.protocol

class GetCurrentStatus(): Protocol(
    Code.GET_CURRENT_STATUS.size){
    override val code: Code =
        Code.GET_CURRENT_STATUS
}