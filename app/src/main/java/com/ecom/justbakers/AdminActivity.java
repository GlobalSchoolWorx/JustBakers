package com.ecom.justbakers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.ecom.justbakers.Classes.ProductClass;
import com.ecom.justbakers.orders.OrderClass;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AdminActivity extends AppCompatActivity {

    Firebase CustomerRef = new Firebase("https://justbakers-285be.firebaseio.com/customers");
    private ArrayList<String> CustomerNameList = new ArrayList<String>();
    private ArrayList<String> CustomerDetailList = new ArrayList<String>();
    private ArrayList<List<String>> CustomerPlacedOrderList = new ArrayList<List<String>>();
    private ArrayList<String> order_arr = new ArrayList<String>();
    private CountDownLatch latch;
    private String selected_user;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CountDownLatch latch1 = new CountDownLatch(1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        CustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CustomerNameList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String namestr = postSnapshot.getKey();
                    CustomerNameList.add(namestr);
                  //  GenericTypeIndicator<ArrayList<OrderListClass>> t = new GenericTypeIndicator<ArrayList<OrderListClass>>()OrderListClass>() {};
                   // ArrayList<OrderListClass> co = postSnapshot.getValue(t);
                    //CustomerOrderList.add(co);
                }

                latch1.countDown();
                latch = new CountDownLatch(CustomerNameList.size());
                CustomerPlacedOrderList.clear();
                Button cntButton = findViewById(R.id.cntButton);

                cntButton.setText("Total Customer Count: "+ CustomerNameList.size());


                for (String str:  CustomerNameList) {

                    Firebase CustomerOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+str+"/orders/placed");

                    CustomerOrderRef.addValueEventListener(new ValueEventListener() {

                        ArrayList <String> SingleCustomerPlacedOrderList = new ArrayList<String>();
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String ss = dataSnapshot.getRef().getPath().toString();
                            String [] sp = ss.split("customers/");
                            String [] st = sp[1].split("/");
                            SingleCustomerPlacedOrderList.add(st[0]);
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                String ol = postSnapshot.getKey();
                                String pc = (String) postSnapshot.child("status").getValue();
                                String ordernumber = (String) postSnapshot.child("ordernumber").getValue();
                                double finalamount = (double) postSnapshot.child("receipt").child("finalamount").getValue();
                                if (pc.equals("pending"))
                                  SingleCustomerPlacedOrderList.add(ordernumber + "\n" + "Final Amount : " + finalamount);
                            }


                           // Collections.sort(SingleCustomerPlacedOrderList, OrderClass.getCompByName());
                            CustomerPlacedOrderList.add(SingleCustomerPlacedOrderList);
                            latch.countDown();
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }

                    });

                }

               // CustomerOrderListAdapter adapter = new CustomerOrderListAdapter(getApplicationContext(), CustomerNameList, CustomerPlacedOrderList);
                ListView listView = findViewById(R.id.listView);
                ListView placedView = findViewById(R.id.placedView);
                //listView.setAdapter(adapter);


                // Create an ArrayAdapter from List
                final ArrayAdapter<String> arrayNameAdapter = new ArrayAdapter<String>
                        (getApplicationContext() , android.R.layout.simple_list_item_1, CustomerDetailList);
                // Create an ArrayAdapter from List
                final ArrayAdapter<String> arrayOrderAdapter = new ArrayAdapter<String>
                        (getApplicationContext() , android.R.layout.simple_list_item_1, order_arr);

                listView.setAdapter(arrayNameAdapter);
                placedView.setAdapter(arrayOrderAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int order_position, long id) {
                       order_arr.clear();
                     //   placedView.setAdapter(null);
                        arrayOrderAdapter.notifyDataSetChanged();
                        selected_user = CustomerNameList.get(order_position);
                        for (List<String> oc : CustomerPlacedOrderList ) {
                            if(oc.get(0).equals(selected_user)) {
                                for ( String ss: oc) {
                                    order_arr.add(ss);
                                }
                                order_arr.remove(0);
                                break;
                            }
                            else
                                continue;
                        }


             //           placedView.setAdapter(arrayOrderAdapter);
                    }
                });

                placedView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String order = order_arr.get(position);
                        String spl_order_temp[] = order.split("#");
                        String spl_order[] = (spl_order_temp[1]).split("\n");
                        Firebase CustomerChangeOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+selected_user+"/orders/placed/"+spl_order[0]);
                   //     Firebase CustomerAddToDelOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+selected_user+"/orders/delivered");

                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder (context);

                        builder.setMessage("Are you sure you want to mark this order as Delivered.").setPositiveButton (android.R.string.yes, (dialog, whichButton) -> {
                            CustomerChangeOrderRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    OrderClass obj = (OrderClass) dataSnapshot.getValue(OrderClass.class);
                                    if (obj != null) {
                                        //OrderClass oc = new OrderClass(obj, "delivered");
                                        CustomerChangeOrderRef.child("status").setValue("delivered");

                                        //dataSnapshot.getRef().removeValue();
                                    }

                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });


                        }).setNegativeButton(android.R.string.no, (dialog, whichButton) -> {});

                        builder.show();
                    }
                });

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        });
/*
        try {
          latch1.await();
          latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

 */

        /** CODE FOR SIGN OUT IN NAVIGATION HEADER **/
        Button signOut = (Button) findViewById(R.id.signOutButton);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.getGoogleSignInClient().signOut()
                        .addOnCompleteListener((Activity) getApplicationContext(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                            }
                        });
                Intent signoutintent = new Intent(AdminActivity.this, LoginActivity.class);
                AdminActivity.this.startActivity(signoutintent);
            }
        });
    }
}