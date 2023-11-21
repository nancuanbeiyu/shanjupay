
package com.shanjupay.common.domain;


/**
 * 异常编码 0成功、-1熔断、 -2 标准参数校验不通过 -3会话超时
 * 前两位:服务标识
 * 中间两位:模块标识
 * 后两位:异常标识
 */
public enum CommonErrorCode implements ErrorCode {
	
	////////////////////////////////////公用异常编码 //////////////////////////

	E_200201(200101,"传入对象为空"),
	E_200202(200102,"参数为空"),
	E_200203(200103,"手机号已存在"),
	E_200204(200104,"租户为管理员，不可删除"),
	E_200205(200105,"账号被其他租户使用，不可删除"),
	E_200206(200106,"角色被使用中，不可删除"),
	E_200207(200107,"查询结果为空"),
	E_200208(200108,"账号不存在"),
	E_200209(200109,"支付渠道参数不能为空"),
	E_200210(200110,"应用未绑定平台渠道"),
	E_200211(200111,"商户下未设置根门店"),
	E_200212(200112,"商户应用支持的聚合平台支付渠道不能为空"),
	E_200213(200113,"支付金额不能为空"),
	E_200214(200114,"openId不能为空"),
	E_200215(200115,"获取支付状态超时"),
	E_200216(200116,"授权码为空"),
	E_200217(200117,"订单标题为空"),
	E_200218(200118,"订单金额为空"),
	E_200219(200119,"订单金额格式有误"),
	E_200220(200120,"授权码格式有误"),
	E_200221(200121,"角色为空"),
	E_200222(200122,"角色设置权限时，传入的权限为空"),
	E_200223(200123,"未查到权限为空"),
	E_200224(200124,"手机号格式不正确"),
	E_200225(200125,"用户名已存在"),
	E_200226(200126,"用户名或密码不正确"),
	E_200227(200127,"商户不存在"),
	E_200228(200128,"员工不存在"),
	E_200229(200129,"门店参数有误"),

	E_200230(200130,"手机号为空"),
	E_200231(200131,"用户名为空"),
	E_200232(200132,"密码为空"),
	E_200233(200133,"查询不到该门店"),
	E_200234(200134,"角色编码在同一租户中已存在，不可重复"),
	E_200235(200135,"企业名称不能为空"),
	E_200236(200136,"商户还未通过认证审核，不能创建应用"),
	E_200237(200137," 验证码错误"),
	E_200238(200138,"该手机没有发送过验证码"),
	E_200239(200139,"七牛云上传图片异常"),

	/**
	 * 传入参数与接口不匹配
	 */
	E_100101(100101,"传入参数与接口不匹配"),
	/**
	 * 验证码错误
	 */
	E_100102(100102,"验证码错误"),
	/**
	 * 验证码为空
	 */
	E_100103(100103,"验证码为空"),
	/**
     * 查询结果为空
     */
    E_100104(100104,"查询结果为空"),
    /**
     * ID格式不正确或超出Long存储范围
     */
    E_100105(100105,"ID格式不正确或超出Long存储范围"),
	/**
	 * 上传出错
	 */
	E_100106(100106,"上传错误"),
	E_100107(100107,"发送验证码错误"),

	////////////////////////////////////网关服务异常编码11//////////////////////////

	////////////////////////////////////UAA服务异常编码12 //////////////////////////

    
	////////////////////////////////////统一账号服务异常编码 13//////////////////////////


	////////////////////////////////////c端用户服务异常编码 14//////////////////////////

	////////////////////////////////////特殊异常编码/////////////////////////////////////
    E_999991(999991,"调用微服务-授权服务 被熔断"),
    E_999992(999992,"调用微服务-用户服务 被熔断"),
    E_999993(999993,"调用微服务-资源服务 被熔断"),
    E_999994(999994,"调用微服务-同步服务 被熔断"),

    E_999910(999910,"调用微服务-没有传tenantId租户Id"),
	E_999911(999911,"调用微服务-没有json-token令牌"),
	E_999912(999912,"调用微服务-json-token令牌解析有误"),
	E_999913(999913,"调用微服务-json-token令牌有误-没有当前租户信息"),
	E_999914(999914,"调用微服务-json-token令牌有误-该租户下没有权限信息"),

	E_NO_AUTHORITY(999997,"没有访问权限"),
	CUSTOM(999998,"自定义异常"),
	/**
	 * 未知错误
	 */
	UNKOWN(999999,"未知错误");
	

	private int code;
	private String desc;
		
	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String getDesc() {
		return desc;
	}

	private CommonErrorCode(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}


	public static CommonErrorCode setErrorCode(int code) {
       for (CommonErrorCode errorCode : CommonErrorCode.values()) {
           if (errorCode.getCode()==code) {
               return errorCode;
           }
       }
	       return null;
	}
}
