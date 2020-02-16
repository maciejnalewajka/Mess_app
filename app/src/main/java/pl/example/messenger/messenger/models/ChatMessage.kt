package pl.example.messenger.messenger.models

class ChatMessage(val ID: String, val text: String, val fromId: String, val toId: String, val timestamp: String){
    constructor(): this("","","","","")
}