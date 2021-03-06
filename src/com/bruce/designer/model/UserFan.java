package com.bruce.designer.model;

public class UserFan {
    
    private User fanUser;
    
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_user_fan.id
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    private Long id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_user_fan.user_id
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    private Integer userId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_user_fan.fan_id
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    private Integer fanId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_user_fan.create_time
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    private long createTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tb_user_fan.update_time
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    private long updateTime;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_user_fan.id
     *
     * @return the value of tb_user_fan.id
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_user_fan.id
     *
     * @param id the value for tb_user_fan.id
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_user_fan.user_id
     *
     * @return the value of tb_user_fan.user_id
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_user_fan.user_id
     *
     * @param userId the value for tb_user_fan.user_id
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_user_fan.fan_id
     *
     * @return the value of tb_user_fan.fan_id
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    public Integer getFanId() {
        return fanId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_user_fan.fan_id
     *
     * @param fanId the value for tb_user_fan.fan_id
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    public void setFanId(Integer fanId) {
        this.fanId = fanId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_user_fan.create_time
     *
     * @return the value of tb_user_fan.create_time
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_user_fan.create_time
     *
     * @param createTime the value for tb_user_fan.create_time
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tb_user_fan.update_time
     *
     * @return the value of tb_user_fan.update_time
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    public long getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tb_user_fan.update_time
     *
     * @param updateTime the value for tb_user_fan.update_time
     *
     * @mbggenerated Sat Oct 12 19:09:06 CST 2013
     */
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public User getFanUser() {
        return fanUser;
    }

    public void setFanUser(User fanUser) {
        this.fanUser = fanUser;
    }
    
    
}