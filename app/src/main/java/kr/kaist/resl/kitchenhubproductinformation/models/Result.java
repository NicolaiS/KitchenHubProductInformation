package kr.kaist.resl.kitchenhubproductinformation.models;

/**
 * Model of result from Product information service
 */

public class Result {

	private Information companyInformation = null;
	private Information itemInformation = null;
	private Information batchInformation = null;
	private Information uniqueInformation = null;

	public Result() {
	};

	public Result(Information companyInformation, Information itemInformation,
			Information batchInformation, Information uniqueInformation) {
		this.companyInformation = companyInformation;
		this.itemInformation = itemInformation;
		this.batchInformation = batchInformation;
		this.uniqueInformation = uniqueInformation;
	}

	public Information getCompanyInformation() {
		return companyInformation;
	}

	public void setCompanyInformation(Information companyInformation) {
		this.companyInformation = companyInformation;
	}

	public Information getItemInformation() {
		return itemInformation;
	}

	public void setItemInformation(Information itemInformation) {
		this.itemInformation = itemInformation;
	}

	public Information getBatchInformation() {
		return batchInformation;
	}

	public void setBatchInformation(Information batchInformation) {
		this.batchInformation = batchInformation;
	}

	public Information getUniqueInformation() {
		return uniqueInformation;
	}

	public void setUniqueInformation(Information uniqueInformation) {
		this.uniqueInformation = uniqueInformation;
	}

}
