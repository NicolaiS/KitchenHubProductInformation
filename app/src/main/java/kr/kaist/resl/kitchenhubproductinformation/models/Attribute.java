package kr.kaist.resl.kitchenhubproductinformation.models;

/**
 * Model of attribute from Product information service
 */

public class Attribute {

	private int attrTypeId;
	private String attrKey = null;
	private String attrName = null;
	private String attrValue = null;
	private String valueFormat = null;
	private int sortOrder;

	public Attribute(int attrTypeId, String attrKey, String attrName,
			String attrValue, String valueFormat, int sortOrder) {
		this.attrTypeId = attrTypeId;
		this.attrKey = attrKey;
		this.attrName = attrName;
		this.attrValue = attrValue;
		this.valueFormat = valueFormat;
		this.sortOrder = sortOrder;
	}

	public int getAttrTypeId() {
		return attrTypeId;
	}

	public void setAttrTypeId(int attrTypeId) {
		this.attrTypeId = attrTypeId;
	}

	public String getAttrKey() {
		return attrKey;
	}

	public void setAttrKey(String attrKey) {
		this.attrKey = attrKey;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}

	public String getValueFormat() {
		return valueFormat;
	}

	public void setValueFormat(String valueFormat) {
		this.valueFormat = valueFormat;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

}
