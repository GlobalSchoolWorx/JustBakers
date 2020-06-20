package com.ecom.justbakers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ecom.justbakers.Classes.ProductClass;
import com.ecom.justbakers.orders.OrderClass;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AdminActivity extends AppCompatActivity {

    Firebase CustomerRef = new Firebase("https://justbakers-285be.firebaseio.com/customers");
    private ArrayList<String> CustomerNameList = new ArrayList<String>();
    private ArrayList<List<String>> CustomerPlacedOrderList = new ArrayList<List<String>>();
    private ArrayList<String> order_arr = new ArrayList<String>();
    private CountDownLatch latch;
    private String selected_user;

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
                                if (pc.equals("pending"))
                                  SingleCustomerPlacedOrderList.add(ol);
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
                        (getApplicationContext() , android.R.layout.simple_list_item_1, CustomerNameList);
                // Create an ArrayAdapter from List
                final ArrayAdapter<String> arrayOrderAdapter = new ArrayAdapter<String>
                        (getApplicationContext() , android.R.layout.simple_list_item_1, order_arr);

                listView.setAdapter(arrayNameAdapter);
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


                        placedView.setAdapter(arrayOrderAdapter);
                    }
                });

                placedView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String order = order_arr.get(position);
                        Firebase CustomerChangeOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+selected_user+"/orders/placed/"+order);
                   //     Firebase CustomerAddToDelOrderRef = new Firebase("https://justbakers-285be.firebaseio.com/customers/"+selected_user+"/orders/delivered");

                        CustomerChangeOrderRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                OrderClass obj = (OrderClass) dataSnapshot.getValue(OrderClass.class);
                                if ( obj != null) {
                                    //OrderClass oc = new OrderClass(obj, "delivered");
                                    CustomerChangeOrderRef.child("status").setValue("delivered");

                                    //dataSnapshot.getRef().removeValue();
                                }

                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });




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
    }
}