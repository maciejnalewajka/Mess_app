package pl.example.messenger.messenger.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.view.WindowManager
import com.example.messenger.messenger.R
import pl.example.messenger.messenger.login_register.RegisterActivity
import pl.example.messenger.messenger.messages.NewMessageActivity.Companion.USER_KEY
import pl.example.messenger.messenger.models.ChatMessage
import pl.example.messenger.messenger.models.User
import pl.example.messenger.messenger.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    private val adapter = GroupAdapter<ViewHolder>()
    val messMap = HashMap<String, ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_latest_messages)
        floatingActionButton.setOnClickListener {
            val intent = Intent(this, NewMessageActivity::class.java)
            startActivity(intent)
        }
        recyclerView_latest_messages.adapter = adapter
        recyclerView_latest_messages.addItemDecoration(DividerItemDecoration(this,  DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(this, ChatActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(USER_KEY, row.chatPartner)
            startActivity(intent)
        }

        fetchCurrentUser()
        verifyLogin()
        setupMessages()
    }

    private fun refreshRecyclerView(){
        adapter.clear()
        messMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun setupMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest_mess/$fromId")
        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                messMap[p0.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return
                messMap[p0.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)

            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    private fun verifyLogin(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}


