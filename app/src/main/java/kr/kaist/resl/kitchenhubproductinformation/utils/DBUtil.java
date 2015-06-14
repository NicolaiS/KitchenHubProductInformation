package kr.kaist.resl.kitchenhubproductinformation.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import constants.database.KHSchema;
import constants.database.KHSchemaAttrContainer;
import constants.database.KHSchemaAttrContainerAttribute;
import constants.database.KHSchemaAttribute;
import constants.database.KHSchemaProduct;
import constants.database.KHSchemaProductInfoMeta;
import kr.kaist.resl.kitchenhubproductinformation.Content_URIs;
import kr.kaist.resl.kitchenhubproductinformation.enums.EnumStatusMsg;
import kr.kaist.resl.kitchenhubproductinformation.models.Attribute;
import kr.kaist.resl.kitchenhubproductinformation.models.Container;
import kr.kaist.resl.kitchenhubproductinformation.models.Information;
import kr.kaist.resl.kitchenhubproductinformation.models.Result;
import models.Product;

/**
 * Created by nicolais on 5/1/15.
 * <p/>
 * Util to connect to Shared Storage module
 */
public class DBUtil {

    /**
     * Get all products marked "present"
     *
     * @param context context
     * @return list of present products
     */
    public static List<Product> getPresentProducts(Context context) {
        List<Product> results = new ArrayList<Product>();

        String selection = KHSchemaProduct.CN_PRESENT + " = ?";
        String[] selectionArgs = new String[]{Integer.toString(1)};

        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(Content_URIs.CONTENT_URI_PRODUCT, KHSchemaProduct.PROJECTION_ALL, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            do {
                Integer intPresent = c.getInt(7);
                Boolean present = intPresent > 0 ? true : false;
                Product i = new Product(c.getInt(0), c.getInt(1), c.getInt(2), c.getString(3), c.getString(4), c.getString(5), c.getInt(6), present, c.getLong(8));
                results.add(i);
            } while (c.moveToNext());
        }

        c.close();

        return results;
    }

    /**
     * Generate batch URN from Product
     * Batch number is gathered from attributes
     *
     * @param context context
     * @param p       product
     * @return batch URN
     */
    public static String getBatchNo(Context context, Product p) {
        ContentResolver resolver = context.getContentResolver();
        String urn = UrnUtil.getUniqueUrn(p);

        String selection = KHSchemaProductInfoMeta.CN_URN + " = ?";
        String[] selectionArgs = new String[]{urn};
        Cursor c = resolver.query(Content_URIs.CONTENT_URI_PRODUCT_INFO_META, new String[]{KHSchema.CN_ID}, selection, selectionArgs, null);
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        Integer pimId = c.getInt(0);
        c.close();

        selection = KHSchemaAttribute.CN_PIM_ID + " = ? AND " + KHSchemaAttribute.CN_ATTR_KEY + " = ?";
        selectionArgs = new String[]{Integer.toString(pimId), "attr_batch_no"};
        c = resolver.query(Content_URIs.CONTENT_URI_ATTRIBUTE, new String[]{KHSchemaAttribute.CN_ATTR_VALUE}, selection, selectionArgs, null);
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        String result = Integer.toString(c.getInt(0));
        c.close();

        return result;
    }

    /**
     * Get version number of previous product information from URN
     *
     * @param context context
     * @param urn     URN
     * @return Version number. null if none found.
     */
    public static Integer getInfoVersion(Context context, String urn) {
        ContentResolver resolver = context.getContentResolver();

        String selection = KHSchemaProductInfoMeta.CN_URN + " = ?";
        String[] selectionArgs = new String[]{urn};
        Cursor c = resolver.query(Content_URIs.CONTENT_URI_PRODUCT_INFO_META, new String[]{KHSchemaProductInfoMeta.CN_VERSION}, selection, selectionArgs, null);
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        Integer version = c.getInt(0);
        c.close();

        return version;
    }

    /**
     * Save all received information to storage
     *
     * @param context context
     * @param result  Result from Product information serivce
     */
    public static void saveInformationResult(Context context, Result result) {
        saveInformation(context, result.getCompanyInformation());
        saveInformation(context, result.getItemInformation());
        saveInformation(context, result.getBatchInformation());
        saveInformation(context, result.getUniqueInformation());
    }

    /**
     * Save all received information to storage
     *
     * @param context     context
     * @param information Information from Product information serivce
     */
    public static void saveInformation(Context context, Information information) {
        ContentResolver resolver = context.getContentResolver();

        Log.d(DBUtil.class.getName(), "- Handling info of: " + information.getUrn());

        String message = information.getMessage();
        if (EnumStatusMsg.OK.getName().equals(message)) {
            Log.d(DBUtil.class.getName(), "-- Clearing existing info of: " + information.getUrn());
            deleteExistingAttributes(context, information.getUrn());

            Log.d(DBUtil.class.getName(), "-- Insert info with version " + information.getVersion());
            ContentValues values = new ContentValues();
            values.put(KHSchemaProductInfoMeta.CN_URN, information.getUrn());
            values.put(KHSchemaProductInfoMeta.CN_VERSION, information.getVersion());
            Uri uri = resolver.insert(Content_URIs.CONTENT_URI_PRODUCT_INFO_META, values);

            Integer pimId = new Integer(uri.getLastPathSegment());

            // Insert attributes
            Log.d(DBUtil.class.getName(), "-- Saving " + information.getAttributes().size() + " attributes");
            ContentValues[] attributesToBeInserted = new ContentValues[information.getAttributes().size()];
            for (int i = 0; i < information.getAttributes().size(); i++) {
                Attribute attr = information.getAttributes().get(i);
                ContentValues attrValues = new ContentValues();
                attrValues.put(KHSchemaAttribute.CN_PIM_ID, pimId);
                attrValues.put(KHSchemaAttribute.CN_ATTR_KEY, attr.getAttrKey());
                attrValues.put(KHSchemaAttribute.CN_ATTR_NAME, attr.getAttrName());
                attrValues.put(KHSchemaAttribute.CN_ATTR_VALUE, attr.getAttrValue());
                attrValues.put(KHSchemaAttribute.CN_ATTR_TYPE_ID, attr.getAttrTypeId());
                attrValues.put(KHSchemaAttribute.CN_VALUE_FORMAT, attr.getValueFormat());
                attrValues.put(KHSchemaAttribute.CN_SORT_ORDER, attr.getSortOrder());
                attributesToBeInserted[i] = attrValues;
            }
            resolver.bulkInsert(Content_URIs.CONTENT_URI_ATTRIBUTE, attributesToBeInserted);

            // Insert Containers
            Log.d(DBUtil.class.getName(), "-- Saving " + information.getContainers().size() + " containers");
            for (Container container : information.getContainers()) {
                values = new ContentValues();
                values.put(KHSchemaAttrContainer.CN_PIM_ID, pimId);
                values.put(KHSchemaAttrContainer.CN_NAME, container.getName());
                values.put(KHSchemaAttrContainer.CN_SORT_ORDER, container.getSortOrder());
                uri = resolver.insert(Content_URIs.CONTENT_URI_ATTR_CONTAINER, values);
                Integer cId = new Integer(uri.getLastPathSegment());

                Log.d(DBUtil.class.getName(), "--- Saving " + container.getAttributes().size() + " attributes of container: \"" + container.getName() + "\"");
                ContentValues[] containerAttrToBeInserted = new ContentValues[container.getAttributes().size()];
                for (int i = 0; i < container.getAttributes().size(); i++) {
                    Attribute attr = container.getAttributes().get(i);
                    ContentValues attrValues = new ContentValues();
                    attrValues.put(KHSchemaAttrContainerAttribute.CN_ATTR_CONTAINER_ID, cId);
                    attrValues.put(KHSchemaAttrContainerAttribute.CN_ATTR_KEY, attr.getAttrKey());
                    attrValues.put(KHSchemaAttrContainerAttribute.CN_ATTR_NAME, attr.getAttrName());
                    attrValues.put(KHSchemaAttrContainerAttribute.CN_ATTR_VALUE, attr.getAttrValue());
                    attrValues.put(KHSchemaAttrContainerAttribute.CN_ATTR_TYPE_ID, attr.getAttrTypeId());
                    attrValues.put(KHSchemaAttrContainerAttribute.CN_VALUE_FORMAT, attr.getValueFormat());
                    attrValues.put(KHSchemaAttrContainerAttribute.CN_SORT_ORDER, attr.getSortOrder());
                    containerAttrToBeInserted[i] = attrValues;
                }
                resolver.bulkInsert(Content_URIs.CONTENT_URI_ATTR_CONTAINER_ATTRIBUTES, containerAttrToBeInserted);
            }
        } else if (EnumStatusMsg.UP_TO_DATE.getName().equals(message)) {
            Log.d(DBUtil.class.getName(), "-- Info up-to-date for: " + information.getUrn());
        } else if (EnumStatusMsg.NOT_FOUND.getName().equals(message)) {
            Log.d(DBUtil.class.getName(), "-- Info not found for: " + information.getUrn());
        } else {
            Log.d(DBUtil.class.getName(), "-- Unknown message: " + message);
        }
    }

    /**
     * Delete all existing attributes of URN
     *
     * @param context context
     * @param urn     URN
     */
    public static void deleteExistingAttributes(Context context, String urn) {
        ContentResolver resolver = context.getContentResolver();

        String selection = KHSchemaProductInfoMeta.CN_URN + " = ?";
        String[] selectionArgs = new String[]{urn};
        Cursor c = resolver.query(Content_URIs.CONTENT_URI_PRODUCT_INFO_META, new String[]{KHSchema.CN_ID}, selection, selectionArgs, null);

        // No information linked to URN. Return
        if (!c.moveToFirst()) {
            c.close();
            return;
        }

        // Get product_information_meta id
        Integer id = c.getInt(0);
        c.close();

        // Delete attributes
        selection = KHSchemaAttribute.CN_PIM_ID + " = ?";
        selectionArgs = new String[]{Integer.toString(id)};
        resolver.delete(Content_URIs.CONTENT_URI_ATTRIBUTE, selection, selectionArgs);

        // Delete container attributes
        selection = KHSchemaAttrContainer.CN_PIM_ID + " = ?";
        selectionArgs = new String[]{Integer.toString(id)};
        c = resolver.query(Content_URIs.CONTENT_URI_ATTR_CONTAINER, new String[]{KHSchema.CN_ID}, selection, selectionArgs, null);

        if (c.moveToFirst()) {
            selection = KHSchemaAttrContainerAttribute.CN_ATTR_CONTAINER_ID + " = ?";
            do {
                selectionArgs = new String[]{Integer.toString(c.getInt(0))};
                resolver.delete(Content_URIs.CONTENT_URI_ATTR_CONTAINER_ATTRIBUTES, selection, selectionArgs);
            } while (c.moveToNext());
        }
        c.close();

        // Delete containers
        selection = KHSchemaAttrContainer.CN_PIM_ID + " = ?";
        selectionArgs = new String[]{Integer.toString(id)};
        resolver.delete(Content_URIs.CONTENT_URI_ATTR_CONTAINER, selection, selectionArgs);

        // Delete product_information_meta entry
        selection = KHSchema.CN_ID + " = ?";
        selectionArgs = new String[]{Integer.toString(id)};
        resolver.delete(Content_URIs.CONTENT_URI_PRODUCT_INFO_META, selection, selectionArgs);
    }
}