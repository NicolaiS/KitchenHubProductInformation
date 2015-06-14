package kr.kaist.resl.kitchenhubproductinformation.models;

import android.content.Context;

import kr.kaist.resl.kitchenhubproductinformation.utils.UrnUtil;
import models.Product;

/**
 * Created by nicolais on 5/22/15.
 * <p/>
 * Creates and holds URNs of product
 */
public class UrnBatch {

    private String companyURN = null;
    private String itemURN = null;
    private String uniqueURN = null;
    private String batchURN = null;

    public UrnBatch(Context context, Product product) {
        companyURN = UrnUtil.getCompanyUrn(product);
        itemURN = UrnUtil.getItemUrn(product);
        batchURN = UrnUtil.getBatchUrn(context, product);
        uniqueURN = UrnUtil.getUniqueUrn(product);
    }

    public String getCompanyURN() {
        return companyURN;
    }

    public String getItemURN() {
        return itemURN;
    }

    public String getUniqueURN() {
        return uniqueURN;
    }

    public String getBatchURN() {
        return batchURN;
    }
}
