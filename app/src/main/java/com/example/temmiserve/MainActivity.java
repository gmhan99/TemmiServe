package com.example.temmiserve;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements OnRobotReadyListener, OnGoToLocationStatusChangedListener {

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private Robot robot;

    RoboTemiListeners roboTemiListeners;
    DatabaseReference wRef1 = firebaseDatabase.getReference("number4").child("data");
    DatabaseReference serveRef2 = firebaseDatabase.getReference("test");

    int tmp=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Temi SDK 초기화
        robot = Robot.getInstance();

        roboTemiListeners = new RoboTemiListeners();
        roboTemiListeners.init();
        roboTemiListeners.getRobot().addOnGoToLocationStatusChangedListener(this);
        Button exitBtn = (Button)findViewById(R.id.btn1);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        DatabaseReference callRef = firebaseDatabase.getReference("call");
        callRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean flag = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                if (flag){
                    robot.startTelepresence(robot.getAdminInfo().getName(), robot.getAdminInfo().getUserId());
                }
                callRef.setValue(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference plantRef = firebaseDatabase.getReference("test");
        plantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int status = (int) snapshot.getValue(Integer.class);
                // 0 : 주방으로 1~6 : 각 테이블로 서빙
                switch (status) {
                    case 10:
                        tmp = 1;
                        robot.goTo("plant1");
                        break;
                    case 11:
                        tmp = 1;
                        robot.goTo("plant2");
                        break;
                    case 12:
                        tmp = 1;
                        robot.goTo("plant3");
                        break;
                    case 100:
                        robot.goTo("홈베이스");
                        tmp = 0;
                        break;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("이동완료 로봇준비완료");
        robot.addOnRobotReadyListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        robot.removeOnRobotReadyListener(this);
    }


    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {

            try {
                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
                robot.onStart(activityInfo);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void onGoToLocationStatusChanged(@NonNull String s, @NonNull String status, int i, @NonNull String s2) {
        switch (status) {
            case "complete":
                if(tmp == 1){
                    wRef1.setValue(1);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    wRef1.setValue(0);
                    tmp = 0;
                }
                else{
                    break;
                }
        }

    }
}