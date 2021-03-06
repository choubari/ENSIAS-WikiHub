package com.choubapp.ensias_wiki_hub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.choubapp.ensias_wiki_hub.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UserProfile extends AppCompatActivity {
    TextView name, email;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PostAdapter adapter;
    CollectionReference postsCollection = db.collection("Post");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        name = findViewById(R.id.user_name);
        email = findViewById(R.id.user_email);
        setUpRecyclerView();
        db.collection("User").document(user.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot==null){
                    Toast.makeText(UserProfile.this, "Error Try again", Toast.LENGTH_SHORT).show();
                }else{
                    name.setText(documentSnapshot.get("nom")+" "+documentSnapshot.get("prenom"));
                    email.setText(documentSnapshot.get("email").toString());
                }
            }
        });
    }
    private void setUpRecyclerView(){
        Query query = postsCollection.whereEqualTo("owner",FirebaseAuth.getInstance().getCurrentUser().getEmail()).orderBy("date", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<PostItem> options = new FirestoreRecyclerOptions.Builder<PostItem>()
                .setQuery(query, PostItem.class)
                .build();
        adapter= new PostAdapter(options);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                //PostItem post = documentSnapshot.toObject(PostItem.class);
                String id = documentSnapshot.getId();
                //String path = documentSnapshot.getReference().getPath();
                Toast.makeText(UserProfile.this,
                        "Position: " + position + " ID: " + id, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UserProfile.this,PostPage.class);
                intent.putExtra("POST_DOCUMENT_ID", id);
                startActivity(intent);
            }
        });
    }

    public void logout(View v){
        auth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
