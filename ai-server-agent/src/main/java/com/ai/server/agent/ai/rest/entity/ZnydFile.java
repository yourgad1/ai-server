package com.ai.server.agent.ai.rest.entity;

import lombok.Data;

@Data
public class ZnydFile {
	/**
	 * 文件地址
	 */
	private String link;
	/**
	 * 域名地址
	 */
	private String domain;
	/**
	 * 文件名
	 */
	private String name;
	/**
	 * 初始文件名
	 */
	private String originalName;
	/**
	 * 附件表ID
	 */
	private String fileId;
	/**
	 * 存储位置
	 */
	private int location;

	/**
	 * 存储同
	 */
	private String fileBucket;
}