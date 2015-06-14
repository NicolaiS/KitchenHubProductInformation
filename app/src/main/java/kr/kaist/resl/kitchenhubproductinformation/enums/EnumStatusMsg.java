package kr.kaist.resl.kitchenhubproductinformation.enums;

/**
 * Enumerator of status messages
 */

public enum EnumStatusMsg {

	OK("OK"), UP_TO_DATE("UP-TO-DATE"), NOT_FOUND("NOT-FOUND");

	private String name = null;

	EnumStatusMsg(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
