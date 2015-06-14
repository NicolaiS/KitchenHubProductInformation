package kr.kaist.resl.kitchenhubproductinformation;

import android.net.Uri;

import constants.ProviderConstants;
import constants.database.KHSchemaAttrContainer;
import constants.database.KHSchemaAttrContainerAttribute;
import constants.database.KHSchemaAttribute;
import constants.database.KHSchemaProduct;
import constants.database.KHSchemaProductInfoMeta;

/**
 * Created by nicolais on 4/23/15.
 *
 * URIs to access Shared Storage module
 */
public class Content_URIs {

    private static final Uri CONTENT_URI = Uri.parse("content://" + ProviderConstants.DB_AUTHORITY);

    public static final Uri CONTENT_URI_PRODUCT = Uri.withAppendedPath(CONTENT_URI, KHSchemaProduct.TABLE_NAME);
    public static final Uri CONTENT_URI_PRODUCT_INFO_META = Uri.withAppendedPath(CONTENT_URI, KHSchemaProductInfoMeta.TABLE_NAME);
    public static final Uri CONTENT_URI_ATTR_CONTAINER = Uri.withAppendedPath(CONTENT_URI, KHSchemaAttrContainer.TABLE_NAME);
    public static final Uri CONTENT_URI_ATTR_CONTAINER_ATTRIBUTES = Uri.withAppendedPath(CONTENT_URI, KHSchemaAttrContainerAttribute.TABLE_NAME);
    public static final Uri CONTENT_URI_ATTRIBUTE = Uri.withAppendedPath(CONTENT_URI, KHSchemaAttribute.TABLE_NAME);

    public static final Uri CONTENT_URI_ONS_ADDR = Uri.withAppendedPath(CONTENT_URI, ProviderConstants.ONS_ADDR);

}
