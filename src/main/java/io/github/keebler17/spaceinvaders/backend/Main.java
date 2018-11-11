package io.github.keebler17.spaceinvaders.backend;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseReference.CompletionListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Main {

	static DatabaseReference refe;
	
	public static void main(String[] args) {
		try {
			boolean allDead = false;
			FileInputStream serviceAccount = new FileInputStream(
					"C:\\Users\\Brendan\\Downloads\\spaceinvaders-multiplayer-firebase-adminsdk-jzs7n-b646e1644c.json");

			// Initialize the app with a service account, granting admin privileges
			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://spaceinvaders-multiplayer.firebaseio.com").build();
			FirebaseApp.initializeApp(options);

			// As an admin, the app has access to read and write all data, regardless of
			// Security Rules
			DatabaseReference ref = FirebaseDatabase.getInstance().getReference("aliens");
			refe = ref;
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					Object document = dataSnapshot.getValue();
					System.out.println(document);
				}

				@Override
				public void onCancelled(DatabaseError error) {
				}

			});
			
			Map<String, Object> map = new HashMap<String, Object>();
			
			
			ref.getParent().addValueEventListener(new ValueEventListener() {

				@Override
				public void onDataChange(DataSnapshot snapshot) {
					for(DataSnapshot s : snapshot.getChildren()) {
						if(!s.getKey().equals("status") && !s.getKey().equals("aliens")) {
							if(s.child("dead").exists()) {
								if(Boolean.valueOf(s.child("dead").getValue().toString())) {
									s.getRef().removeValue(new CompletionListener() {

										@Override
										public void onComplete(DatabaseError error, DatabaseReference ref) {
											s.getRef().push();
										}
										
									});
								}
							}
						}
					}
				}

				@Override
				public void onCancelled(DatabaseError error) {
				}
				
			});
			
			ref.getParent().child("status").addValueEventListener(new ValueEventListener() {

				@Override
				public void onDataChange(DataSnapshot snapshot) {
					boolean[] values = new boolean[44];
					int i = 0;
					for(DataSnapshot s : snapshot.getChildren()) {
						values[i] = Boolean.getBoolean(s.getValue().toString());
						i++;
					}
					
					for(boolean b : values) {
						if(b) {
							//allDead = true;
						}
					}
				}

				@Override
				public void onCancelled(DatabaseError error) {
					
				}
				
			});
			
			resetAliens();
			
			int x = 110;
			boolean left = false;
			while(true) {
				map.clear();
				map.put("x", x);
				
				Thread.sleep(16);
				
				if(left) {
					x -= 2;
				} else {
					x += 2;
				}

				if(x >= 190) {
					left = true;
				}
				
				if(x <= 0) {
					left = false;
				}
				
				ref.setValueAsync(map);
				ref.push();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void resetAliens() {
		Map<String, Boolean> aliens = new HashMap<String, Boolean>();
		
		for(int i = 0; i <= 43; i++) {
			aliens.put("" + i, true); // true = alive
		}
		
		DatabaseReference alienstatus = refe.getParent().child("status");
		
		alienstatus.setValueAsync(aliens);
		alienstatus.push();
	}

}
