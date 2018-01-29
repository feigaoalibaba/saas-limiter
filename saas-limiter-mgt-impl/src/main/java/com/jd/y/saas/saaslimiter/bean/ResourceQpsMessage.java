package com.jd.y.saas.saaslimiter.bean;

/**
 * @author gaofei12
 * @Desc
 * @date 2018/1/17
 */
public class ResourceQpsMessage {


    //资源URI
    private String resourceName;

    //qps
    private long qps;

    //服务name
    private String serverName;

    private long createTime;

    private long updateTime;

    //是否生效 1-表示有效； 0- 表示无效
    private int isValid;

    //限流类型 单点、分布式
    private String type;

    //操作类型 del: 表示删除； 其它表示更新或新增
    private String operate;

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public long getQps() {
        return qps;
    }

    public void setQps(long qps) {
        this.qps = qps;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getIsValid() {
        return isValid;
    }

    public void setIsValid(int isValid) {
        this.isValid = isValid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ResourceQpsMessage{" +
                "resourceName='" + resourceName + '\'' +
                ", qps=" + qps +
                ", serverName='" + serverName + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isValid=" + isValid +
                ", type='" + type + '\'' +
                ", operate='" + operate + '\'' +
                '}';
    }
}
