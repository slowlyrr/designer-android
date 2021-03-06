package com.bruce.designer.model;

import java.io.Serializable;

public class Album extends AlbumBase implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.id
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private Integer id;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.title
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private String title;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.remark
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private String remark;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.price
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private Long price;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.link
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private String link;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.user_id
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private Integer userId;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.cover_large_img
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private String coverLargeImg;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.cover_medium_img
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private String coverMediumImg;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.cover_small_img
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private String coverSmallImg;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.status
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private Short status;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.create_time
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private long createTime;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column tb_album.update_time
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    private long updateTime;

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.id
     * @return  the value of tb_album.id
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.id
     * @param id  the value for tb_album.id
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.title
     * @return  the value of tb_album.title
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public String getTitle() {
        return title;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.title
     * @param title  the value for tb_album.title
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.remark
     * @return  the value of tb_album.remark
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public String getRemark() {
        return remark;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.remark
     * @param remark  the value for tb_album.remark
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.price
     * @return  the value of tb_album.price
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public Long getPrice() {
        return price;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.price
     * @param price  the value for tb_album.price
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setPrice(Long price) {
        this.price = price;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.link
     * @return  the value of tb_album.link
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public String getLink() {
        return link;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.link
     * @param link  the value for tb_album.link
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.user_id
     * @return  the value of tb_album.user_id
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.user_id
     * @param userId  the value for tb_album.user_id
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.cover_large_img
     * @return  the value of tb_album.cover_large_img
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public String getCoverLargeImg() {
        return coverLargeImg;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.cover_large_img
     * @param coverLargeImg  the value for tb_album.cover_large_img
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setCoverLargeImg(String coverLargeImg) {
        this.coverLargeImg = coverLargeImg;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.cover_medium_img
     * @return  the value of tb_album.cover_medium_img
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public String getCoverMediumImg() {
        return coverMediumImg;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.cover_medium_img
     * @param coverMediumImg  the value for tb_album.cover_medium_img
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setCoverMediumImg(String coverMediumImg) {
        this.coverMediumImg = coverMediumImg;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.cover_small_img
     * @return  the value of tb_album.cover_small_img
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public String getCoverSmallImg() {
        return coverSmallImg;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.cover_small_img
     * @param coverSmallImg  the value for tb_album.cover_small_img
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setCoverSmallImg(String coverSmallImg) {
        this.coverSmallImg = coverSmallImg;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.status
     * @return  the value of tb_album.status
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public Short getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.status
     * @param status  the value for tb_album.status
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setStatus(Short status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.create_time
     * @return  the value of tb_album.create_time
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.create_time
     * @param createTime  the value for tb_album.create_time
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column tb_album.update_time
     * @return  the value of tb_album.update_time
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public long getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column tb_album.update_time
     * @param updateTime  the value for tb_album.update_time
     * @mbggenerated  Tue Oct 15 10:29:05 CST 2013
     */
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}