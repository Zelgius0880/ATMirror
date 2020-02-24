# Protocols
## Base
Each protocol begins with a protocol number (b0) and a size (b1). The size if the size of the payload, starting at b2 

## Definition
| Number | Size | Direction | ack|
| ------------- | ------------- | ------------- | ------------- |
|the value of b0| The size of the payload| P -> M: Phone to Mirror <br>P <- M : Mirror to phone | &#9745;  if a ack message will be send as response
___
### ACK

| Number  |Size | Direction | ack|
| :-------------: | :-------------: | :-------------: | :-------------: |
|0x00|1|Both|

#### Frame 
| b0  | b1 | b2 |
| :-------------: | :-------------: | ------------- |
| 0x00 | 0x01 | - 0x00: if ack ok <br> - 0x01: if ack ko

---
### NEW SWITCH DISCOVERY
Message send to start discovering new switches

| Number  |Size | Direction | ack|
| :-------------: | :-------------: | :-------------: | :-------------: |
|0x01|0|P -> M|&#9745;|

#### Frame 
| b0  | b1 | 
| :-------------: | :-------------: | 
| 0x01 | 0x00 | 

---
### NEW SWITCH DETECTED
Call back when a new switch signal is received. Must be in NEW SWITCH DISCOVERY mode

| Number  |Size | Direction | ack|
| :-------------: | :-------------: | :-------------: | :-------------: |
|0x01|n|P <- M|&#9745;|

#### Frame 
| b0  | b1 | b2 .. bn|
| :-------------: | :-------------: | ------------- | 
| 0x01 | 0x00 | The signal of the switch
