package kr.kaist.resl.kitchenhubproductinformation.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.xbill.DNS.NAPTRRecord;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import constants.KHBroadcasts;
import kr.kaist.resl.kitchenhubproductinformation.R;
import kr.kaist.resl.kitchenhubproductinformation.models.Result;
import kr.kaist.resl.kitchenhubproductinformation.models.UrnBatch;
import kr.kaist.resl.kitchenhubproductinformation.utils.DBUtil;
import kr.kaist.resl.kitchenhubproductinformation.utils.ONSUtil;
import models.Product;

/**
 * Created by nicolais on 4/30/15.
 * <p/>
 * Service to retrive product information
 */
public class ProductInformationService extends Service {

    private static int FOREGROUND_ID = 4202;

    private BroadcastReceiver broadcastReceiver = null;

    private UpdateProductInfo thread = null;
    private boolean onceMore = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize broadcast receiver
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Start thread is none is running. Else queue another thread. Max one thread in queue.
                onceMore = true;
                if (thread == null || !thread.isAlive()) {
                    thread = new UpdateProductInfo();
                    thread.start();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(KHBroadcasts.PRODUCTS_UPDATED);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null) unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Keep service alive
        startForeground(FOREGROUND_ID, buildForegroundNotification());
        return START_STICKY;
    }

    class UpdateProductInfo extends Thread {

        @Override
        public void run() {
            while (onceMore) {
                Log.d(getClass().getName(), "- Product information requested");
                onceMore = false;
                List<Product> products = DBUtil.getPresentProducts(ProductInformationService.this);
                Log.d(getClass().getName(), "- Retrieved " + products.size() + " existing products.");
                Map<Product, List<NAPTRRecord>> productRecordMap = ONSUtil.getProductRecordMap(ProductInformationService.this, products);

                for (Product p : products) {
                    List<NAPTRRecord> records = productRecordMap.get(p);
                    if (records == null || records.isEmpty()) continue;

                    UrnBatch urnBatch = new UrnBatch(ProductInformationService.this, p);

                    try {
                        getInformation(ProductInformationService.this, urnBatch, records);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                Log.d(getClass().getName(), "- Sending broadcast: " + KHBroadcasts.PRODUCT_INFORMATION_UPDATED);
                sendBroadcast(new Intent(KHBroadcasts.PRODUCT_INFORMATION_UPDATED));
            }
        }
    }

    /**
     * Get product information from Product information service
     *
     * @param context  context
     * @param urnBatch URNs
     * @param records  Product information services
     * @throws UnsupportedEncodingException
     */
    private void getInformation(Context context, UrnBatch urnBatch, List<NAPTRRecord> records) throws UnsupportedEncodingException {
        // Load parameters
        String parameters = "?urn=" + URLEncoder.encode(urnBatch.getUniqueURN(), "UTF-8");
        Integer cVersion = DBUtil.getInfoVersion(context, urnBatch.getCompanyURN());
        if (cVersion != null) parameters += "&cVersion=" + cVersion;
        Integer iVersion = DBUtil.getInfoVersion(context, urnBatch.getItemURN());
        if (iVersion != null) parameters += "&iVersion=" + iVersion;
        Integer bVersion = DBUtil.getInfoVersion(context, urnBatch.getBatchURN());
        if (bVersion != null) parameters += "&bVersion=" + bVersion;
        Integer uVersion = DBUtil.getInfoVersion(context, urnBatch.getUniqueURN());
        if (uVersion != null) parameters += "&uVersion=" + uVersion;

        Log.d(getClass().getName(), "- Generating URNs and retrieving version numbers of stored information.");
        Log.d(getClass().getName(), "-- cInfo " + urnBatch.getCompanyURN() + " version " + cVersion);
        Log.d(getClass().getName(), "-- iInfo " + urnBatch.getItemURN() + " version " + iVersion);
        Log.d(getClass().getName(), "-- bInfo " + urnBatch.getBatchURN() + " version " + bVersion);
        Log.d(getClass().getName(), "-- uInfo " + urnBatch.getUniqueURN() + " version " + uVersion);

        Gson gson = new GsonBuilder().create();

        // Try to connect to Product information services
        for (NAPTRRecord record : records) {
            try {
                String[] splitFirst = record.getRegexp().split("!");
                String url = splitFirst[2] + parameters;

                Log.d(getClass().getName(), "- Sending request: " + url);

                // Connect to product information service
                ClientResource clientConnection = new ClientResource(url);
                Representation result = clientConnection.get();
                String strResult = result.getText();

                Result infoResult = gson.fromJson(strResult, Result.class);
                DBUtil.saveInformationResult(ProductInformationService.this, infoResult);

                // Finish method if successful
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Create notification
    private Notification buildForegroundNotification() {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this);

        b.setOngoing(true);
        b.setSmallIcon(R.drawable.ic_adb_white_24dp)
                .setContentTitle("Production Information")
                .setContentText("Running...");

        return (b.build());
    }
}
