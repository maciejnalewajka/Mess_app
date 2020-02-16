package pl.example.messenger.messenger.views

import com.example.messenger.messenger.R
import pl.example.messenger.messenger.models.ChatMessage
import pl.example.messenger.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_row_latest_message.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
    var chatPartner: User? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_message_row_latestmessage.text = chatMessage.text

        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        }
        else{
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartner = p0.getValue(User::class.java)
                viewHolder.itemView.textView_user_row_latestmessage.text = chatPartner?.username
                val imageTarget = viewHolder.itemView.imageView_row_latestmessage
                Picasso.get().load(chatPartner?.profileImageUrl).into(imageTarget)
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })


    }

    override fun getLayout(): Int {
        return R.layout.user_row_latest_message
    }
}